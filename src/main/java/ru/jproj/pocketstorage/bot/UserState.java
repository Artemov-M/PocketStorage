package ru.jproj.pocketstorage.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.DeleteMessage;
import org.apache.commons.codec.digest.DigestUtils;
import ru.jproj.pocketstorage.bot.fsm.State;
import ru.jproj.pocketstorage.bot.fsm.Unregistered;
import ru.jproj.pocketstorage.dao.UserDao;

public class UserState {

    // todo при общении с ботом 1-1 chatId == userId (в id заносится именно userId)
    //  пока предполагаю что этот бот не нужен и не будет работать в групповых чатах
    //  в таком виде при обращении к боту в групповом чате он отвечает в личный
    private final Long id;
    private State state;
    private Integer lastMessageId = 0;


    public UserState(Long id, TelegramBot bot) {
        this.id = id;
        this.state = new Unregistered(this, bot);
    }

    // обновляем lastMessageId и пытаемся удалить предыдущие сообщения, чтобы не захламлять чат
    public void resetLastMessageId(Integer messageId, Long chatId, TelegramBot bot) {
        int minMessageId;
        if (lastMessageId.equals(messageId)) {
            return;
        } else if (lastMessageId == 0) {
            lastMessageId = messageId;
            // сюда попадаем после падения/перезапуска бота, поэтому не знаем сколько лишних сообщений
            // могло быть, число 10 взято случайно
            minMessageId = lastMessageId - 10;
        } else {
            minMessageId = Math.min(lastMessageId, messageId);
            lastMessageId = Math.max(lastMessageId, messageId);
        }
        while (minMessageId < lastMessageId) {
            bot.execute(new DeleteMessage(chatId, minMessageId));
            minMessageId += 1;
        }
    }

    public boolean isNotInDb(String userHash) {
        var userDao = UserDao.getInstance();
        var optionalUser = userDao.findByHash(userHash);
        return optionalUser.isEmpty();
    }

    public void addInDb() {
        var userHash = DigestUtils.sha256Hex(id.toString());
        if (isNotInDb(userHash)) {
            UserDao.getInstance().save(userHash);
        }
    }

    public Long getId() {
        return id;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Integer getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(Integer lastMessageId) {
        this.lastMessageId = lastMessageId;
    }
}
