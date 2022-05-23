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
import ru.jproj.pocketstorage.dao.ValueDao;
import ru.jproj.pocketstorage.entity.Value;

public class KeyGetValue extends State {

    private static final String KEY_GET_MESSAGE = """
            Введите ключ чтобы получить список сообщений.
            [cancel] - отказаться от ввода ключа.
            """;

    public KeyGetValue(UserState userState, TelegramBot bot) {
        super(userState, bot);
        changeKeyboard();
    }

    private void changeKeyboard() {
        var baseResponse = bot.execute(new EditMessageText(userState.getId(),
                userState.getLastMessageId(), KEY_GET_MESSAGE));
        if (!baseResponse.isOk()) {
            System.out.println("Ой! Нет сообщения для редактирования клавиатуры в " + this.getClass().getSimpleName());
            var sendResponse = bot.execute(new SendMessage(userState.getId(), "Запуск..."));
            userState.setLastMessageId(sendResponse.message().messageId());
            bot.execute(new EditMessageText(userState.getId(), userState.getLastMessageId(), KEY_GET_MESSAGE));
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
        var text = update.message().text();
        if (text.equals("/start")) {
            userState.resetLastMessageId(update.message().messageId(), update.message().chat().id(), bot);
            userState.setState(new Idle(userState, bot));
            return;
        }
        var keyHash = DigestUtils.sha256Hex(text);
        var userDao = UserDao.getInstance();
        var optionalUser = userDao.findByHash(DigestUtils.sha256Hex(userState.getId().toString()));

        StringBuilder sb = new StringBuilder();
        if (optionalUser.isPresent()) {
            var keyId = KeyDao.getInstance().findId(optionalUser.get().getId(), keyHash);
            if (keyId.isPresent()) {
                var values = ValueDao.getInstance().findByKeyId(keyId.get());
                // todo добавить удаление по таймауту,
                //  удалить сообщение при добавлении удаления по таймауту
                for (Value value : values) {
                    sb.append("---\n").append(value.getValue()).append("\n");
                }
                sb.append("""
                        ---
                        Используйте кнопку [скрыть] после прочтения.
                        """);
            }
        }
        if (sb.isEmpty()) {
            // todo добавить удаление по таймауту
            sb.append("""
                    Записей по такому ключу не существует.
                    ---
                    Используйте кнопку [скрыть] после прочтения.
                    """);
        }
        bot.execute(new SendMessage(update.message().chat().id(), sb.toString())
                .replyMarkup(BotKeyboard.hideMessageKeyboard));
    }

    private void handleCallback(Update update) {
        bot.execute(new AnswerCallbackQuery(update.callbackQuery().id()));
        var chatId = update.callbackQuery().message().chat().id();
        var messageId = update.callbackQuery().message().messageId();
        // todo
        System.out.println("# : " + update.callbackQuery().data() + " : в KeyGetValue, от messageId: " +
                messageId + ": при этом lastSendMessageId: " + userState.getLastMessageId());

        switch (update.callbackQuery().data()) {
            case "/cancel" -> userState.setState(new Idle(userState, bot));
            default -> {
                // если каким-то образом в чате были пропущены сообщения с кнопками, то они будут удалены
                bot.execute(new DeleteMessage(chatId, messageId));
                // todo заменить на логирование
                System.out.println(">>> Пришел неизвестный колбэк в KeyGetValue: " + update.callbackQuery().data() +
                        " от messageId: " + messageId + ": при этом lastSendMessageId: " +
                        userState.getLastMessageId());
            }
        }
    }

}
