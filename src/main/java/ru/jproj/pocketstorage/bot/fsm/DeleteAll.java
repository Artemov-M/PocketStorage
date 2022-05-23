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

public class DeleteAll extends State {

    private static final String ALL_DELETE_MESSAGE = """
            Это действие удалит все записи без возможности восстановления.
            [delete all] - чтобы удалить все сообщения и ключи.
            [cancel] - отказаться от удаления.
            """;

    public DeleteAll(UserState userState, TelegramBot bot) {
        super(userState, bot);
        changeKeyboard();
    }

    private void changeKeyboard() {
        var baseResponse = bot.execute(new EditMessageText(userState.getId(),
                userState.getLastMessageId(), ALL_DELETE_MESSAGE));
        if (!baseResponse.isOk()) {
            System.out.println("Ой! Нет сообщения для редактирования клавиатуры в " + this.getClass().getSimpleName());
            var sendResponse = bot.execute(new SendMessage(userState.getId(), "Запуск..."));
            userState.setLastMessageId(sendResponse.message().messageId());
            bot.execute(new EditMessageText(userState.getId(), userState.getLastMessageId(), ALL_DELETE_MESSAGE));
        }
        bot.execute(new EditMessageReplyMarkup(userState.getId(), userState.getLastMessageId())
                .replyMarkup(BotKeyboard.reallyDeleteKeyboard));
    }

    @Override
    public void handle(Update update) {
        if (update.callbackQuery() != null) {
            handleCallback(update);
        } else {
            handleMessage(update);
        }
    }

    private void handleCallback(Update update) {
        bot.execute(new AnswerCallbackQuery(update.callbackQuery().id()));
        var chatId = update.callbackQuery().message().chat().id();
        var messageId = update.callbackQuery().message().messageId();
        // todo
        System.out.println("# : " + update.callbackQuery().data() + " : в DeleteAll, от messageId: " +
                messageId + ": при этом lastSendMessageId: " + userState.getLastMessageId());

        switch (update.callbackQuery().data()) {
            case "/cancel" -> userState.setState(new Idle(userState, bot));
            case "/delete_all" -> {
                var optionalUser = UserDao.getInstance().findByHash(
                        DigestUtils.sha256Hex(userState.getId().toString()));
                optionalUser.ifPresent(user -> KeyDao.getInstance().deleteAllForUser(user));
                userState.setState(new Idle(userState, bot));
            }
            default -> {
                // если каким-то образом в чате были пропущены сообщения с кнопками, то они будут удалены
                bot.execute(new DeleteMessage(chatId, messageId));
                // todo заменить на логирование
                System.out.println(">>> Пришел неизвестный колбэк в DeleteAll: " + update.callbackQuery().data() +
                        " от messageId: " + messageId + ": при этом lastSendMessageId: " +
                        userState.getLastMessageId());
            }
        }
    }

    private void handleMessage(Update update) {
        bot.execute(new DeleteMessage(update.message().chat().id(), update.message().messageId()));
        var text = update.message().text();
        userState.resetLastMessageId(update.message().messageId(), update.message().chat().id(), bot);
        userState.setState(new Idle(userState, bot));
//        bot.execute(new EditMessageText(update.message().chat().id(), userState.getLastMessageId(),
//                "***\nДля выполнения других действий нажмите [cancel]\n***\n" + ALL_DELETE_MESSAGE));
//        bot.execute(new EditMessageReplyMarkup(update.message().chat().id(), userState.getLastMessageId())
//                .replyMarkup(BotKeyboard.reallyDeleteKeyboard));
        // todo заменить на логирование
        System.out.println("-@- Пришло неизвестное сообщение в DeleteAll: " +
                "lastSendMessageId обновлен: " + userState.getLastMessageId());
    }

}
