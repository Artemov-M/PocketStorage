package ru.jproj.pocketstorage.bot.fsm;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.BaseResponse;
import ru.jproj.pocketstorage.bot.UserState;
import ru.jproj.pocketstorage.bot.layout.BotKeyboard;
import ru.jproj.pocketstorage.dao.ValueDao;
import ru.jproj.pocketstorage.entity.Value;

public class AddValue extends State {

    private final Long keyId;
    private int count = 0;
    private static final String GET_VALUE_MESSAGE = """
            Вводите сообщения.
            Вы можете ввести до 10 сообщений за один раз, после чего сообщения будут сохранены, а вы будете \
            перенаправленны в основное меню. Вы можете продолжить вводить сообщения для того же ключа пройдя \
            процедуру проверки ключа снова.
            [cancel] - прекратить ввод сообщений, все раннее введенные сообщения будут сохранены.
            """;


    public AddValue(Long keyId, UserState userState, TelegramBot bot) {
        super(userState, bot);
        this.keyId = keyId;
        changeKeyboard();
    }

    private void changeKeyboard() {
        var baseResponse = bot.execute(new EditMessageText(userState.getId(),
                userState.getLastMessageId(), GET_VALUE_MESSAGE));
        if (!baseResponse.isOk()) {
            System.out.println("Ой! Нет сообщения для редактирования клавиатуры в " + this.getClass().getSimpleName());
            var sendResponse = bot.execute(new SendMessage(userState.getId(), "Запуск..."));
            userState.setLastMessageId(sendResponse.message().messageId());
            bot.execute(new EditMessageText(userState.getId(), userState.getLastMessageId(), GET_VALUE_MESSAGE));
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
        count += 1;
        var text = update.message().text();
        if (text.equals("/start")) {
            userState.resetLastMessageId(update.message().messageId(), update.message().chat().id(), bot);
            userState.setState(new Idle(userState, bot));
            return;
        }
        ValueDao.getInstance().save(new Value(-1L, keyId, text));
        if (count == 10) {
            userState.setState(new Idle(userState, bot));
        }
        // todo заменить на логирование
        System.out.println("--- Прошел AddValue.handleMessage. lastSendMessageId: " +
                userState.getLastMessageId());
    }

    private void handleCallback(Update update) {
        bot.execute(new AnswerCallbackQuery(update.callbackQuery().id()));
        var chatId = update.callbackQuery().message().chat().id();
        var messageId = update.callbackQuery().message().messageId();
        // todo заменить на логирование
        System.out.println("# : " + update.callbackQuery().data() + " : в AddValue, от messageId: " +
                messageId + ": при этом lastSendMessageId: " + userState.getLastMessageId());

        switch (update.callbackQuery().data()) {
            case "/cancel" -> userState.setState(new Idle(userState, bot));
            default -> {
                // если каким-то образом в чате были пропущены сообщения с кнопками, то они будут удалены
                bot.execute(new DeleteMessage(chatId, messageId));
                // todo заменить на логирование
                System.out.println(">>> Пришел неизвестный колбэк в AddValue: " + update.callbackQuery().data() +
                        " от messageId: " + messageId + ": при этом lastSendMessageId: " +
                        userState.getLastMessageId());
            }
        }
    }
}
