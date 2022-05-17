package ru.jproj.pocketstorage.bot.fsm;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.BaseResponse;
import org.apache.commons.codec.digest.DigestUtils;
import ru.jproj.pocketstorage.bot.UserState;
import ru.jproj.pocketstorage.bot.layout.BotKeyboard;
import ru.jproj.pocketstorage.dao.KeyDao;
import ru.jproj.pocketstorage.dao.UserDao;

public class KeyDeleteValue extends State {

    private static final String DELETE_MESSAGE = """
            Введите ключ списка сообщений, которые хотите удалить.
            Удаленные сообщения восстановить нельзя.
            [cancel] - отказаться от ввода ключа.
            """;

    public KeyDeleteValue(UserState userState, TelegramBot bot) {
        super(userState, bot);
        changeKeyboard();
    }

    private void changeKeyboard() {
        var baseResponse = bot.execute(new EditMessageText(userState.getId(),
                userState.getLastMessageId(), DELETE_MESSAGE));
        if (!baseResponse.isOk()) {
            System.out.println("Ой! Нет сообщения для редактирования клавиатуры в " + this.getClass().getSimpleName());
            var sendResponse = bot.execute(new SendMessage(userState.getId(), "Запуск..."));
            userState.setLastMessageId(sendResponse.message().messageId());
            bot.execute(new EditMessageText(userState.getId(), userState.getLastMessageId(), DELETE_MESSAGE));
        }
        bot.execute(new EditMessageReplyMarkup(userState.getId(), userState.getLastMessageId())
                .replyMarkup(BotKeyboard.cancelKeyboard));
    }

    @Override
    public void handle(Update update) {
        if (update.message() != null) {
            handleMessage(update);
        } else {
            handleCallback(update);
        }
    }

    private void handleMessage(Update update) {
        bot.execute(new DeleteMessage(update.message().chat().id(), update.message().messageId()));
        var keyHash = DigestUtils.sha256Hex(update.message().text());
        var optionalUser = UserDao.getInstance().findByHash(
                DigestUtils.sha256Hex(userState.getId().toString()));
        optionalUser.ifPresent(user -> KeyDao.getInstance().deleteByKeyHash(user.getId(), keyHash));
        // todo
        System.out.println("--- Прошел KeyDeleteValue.handleMessage. lastSendMessageId: " +
                userState.getLastMessageId());
    }

    private void handleCallback(Update update) {
        bot.execute(new AnswerCallbackQuery(update.callbackQuery().id()));
        var chatId = update.callbackQuery().message().chat().id();
        var messageId = update.callbackQuery().message().messageId();
        // todo
        System.out.println("# : " + update.callbackQuery().data() + " : в KeyDeleteValue, от messageId: " +
                messageId + ": при этом lastSendMessageId: " + userState.getLastMessageId());

        switch (update.callbackQuery().data()) {
            case "/cancel" -> userState.setState(new Idle(userState, bot));
            default -> {
                // если каким-то образом в чате были пропущены сообщения с кнопками, то они будут удалены
                bot.execute(new DeleteMessage(chatId, messageId));
                // todo заменить на логирование
                System.out.println(">>> Пришел неизвестный колбэк в KeyDeleteValue: " + update.callbackQuery().data() +
                        " от messageId: " + messageId + ": при этом lastSendMessageId: " +
                        userState.getLastMessageId());
            }
        }
    }
}
