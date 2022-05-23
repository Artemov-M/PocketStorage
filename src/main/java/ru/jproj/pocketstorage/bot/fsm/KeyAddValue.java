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
import ru.jproj.pocketstorage.entity.Key;

public class KeyAddValue extends State {

    private static final String KEY_ADD_MESSAGE = """
            Введите ключ списка, в который хотите добавить сообщение.
            Если введённый ключ уже существует - то сообщение будет добавлено ниже по списку, \
            если нет - то будет создан новый список.
            [cancel] - отказаться от ввода ключа.
            """;

    public KeyAddValue(UserState userState, TelegramBot bot) {
        super(userState, bot);
        changeKeyboard();
    }

    private void changeKeyboard() {
        var baseResponse = bot.execute(new EditMessageText(userState.getId(),
                userState.getLastMessageId(), KEY_ADD_MESSAGE));
        if (!baseResponse.isOk()) {
            System.out.println("Ой! Нет сообщения для редактирования клавиатуры в " + this.getClass().getSimpleName());
            var sendResponse = bot.execute(new SendMessage(userState.getId(), "Запуск..."));
            userState.setLastMessageId(sendResponse.message().messageId());
            bot.execute(new EditMessageText(userState.getId(), userState.getLastMessageId(), KEY_ADD_MESSAGE));
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
        // todo если придет пустой Optional, то это значит что проблемы с БД или каким то образом была пропущена
        //  команда start в Unregistered когда создается пользователь в БД, нужно сделать обработку такого события
        //  так же проверить в остальных классах состояний
        var optionalUser = userDao.findByHash(DigestUtils.sha256Hex(userState.getId().toString()));
        Long keyId = null;
        if (optionalUser.isPresent()) {
            Long userId = optionalUser.get().getId();
            keyId = KeyDao.getInstance().findId(userId, keyHash).orElseGet(
                    () -> KeyDao.getInstance().save(new Key(-1L, userId, keyHash)).getId()
            );
            userState.setState(new AddValue(keyId, userState, bot));
        }
        // todo
        System.out.println("--- Прошел KeyAddValue.handleMessage. lastSendMessageId: " +
                userState.getLastMessageId());
    }

    private void handleCallback(Update update) {
        bot.execute(new AnswerCallbackQuery(update.callbackQuery().id()));
        var chatId = update.callbackQuery().message().chat().id();
        var messageId = update.callbackQuery().message().messageId();
        // todo
        System.out.println("# : " + update.callbackQuery().data() + " : в KeyAddValue, от messageId: " +
                messageId + ": при этом lastSendMessageId: " + userState.getLastMessageId());

        switch (update.callbackQuery().data()) {
            case "/cancel" -> userState.setState(new Idle(userState, bot));
            default -> {
                // если каким-то образом в чате были пропущены сообщения с кнопками, то они будут удалены
                bot.execute(new DeleteMessage(chatId, messageId));
                // todo заменить на логирование
                System.out.println(">>> Пришел неизвестный колбэк в KeyAddValue: " + update.callbackQuery().data() +
                        " от messageId: " + messageId + ": при этом lastSendMessageId: " +
                        userState.getLastMessageId());
            }
        }
    }
}
