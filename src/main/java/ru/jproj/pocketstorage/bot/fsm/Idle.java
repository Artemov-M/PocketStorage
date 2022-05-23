package ru.jproj.pocketstorage.bot.fsm;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.BaseResponse;
import ru.jproj.pocketstorage.bot.UserState;
import ru.jproj.pocketstorage.bot.layout.BotKeyboard;

public class Idle extends State {

    // todo добавить описание
    private static final String IDLE_MESSAGE = """
            Бот сохраняет текстовые сообщения под ключом. \
            Эти сообщения ни видны в этом чате, до тех пор пока не будет введен правильный ключ.
            Вы можете использовать разные ключи для разных сообщений. Под одним ключом может быть \
            сохранено несколько сообщений, но отображаться при запросе они будут все одним сообщением.
            Удалять отдельные сообщения под одним ключом нельзя, только все разом удалив ключ. Ключи можно \
            удалять по одному. Редактировать сообщения нельзя, только удалить и создать новое. После удаления \
            восстановить сообщения нельзя.
            Описание каждой кнопки появится при нажатии на неё.
            """;

    public Idle(UserState userState, TelegramBot bot) {
        super(userState, bot);
        changeKeyboard();
    }

    private void changeKeyboard() {
        var baseResponse = bot.execute(new EditMessageText(userState.getId(),
                userState.getLastMessageId(), IDLE_MESSAGE));
        if (!baseResponse.isOk()) {
            System.out.println("Ой! Нет сообщения для редактирования клавиатуры в " + this.getClass().getSimpleName());
            var sendResponse = bot.execute(new SendMessage(userState.getId(), "Запуск..."));
            userState.setLastMessageId(sendResponse.message().messageId());
            bot.execute(new EditMessageText(userState.getId(), userState.getLastMessageId(), IDLE_MESSAGE));
        }
        bot.execute(new EditMessageReplyMarkup(userState.getId(), userState.getLastMessageId())
                .replyMarkup(BotKeyboard.idleKeyboard));
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
        System.out.println("# : " + update.callbackQuery().data() + " : в Idle, от messageId: " +
                messageId + ": при этом lastSendMessageId: " +
                userState.getLastMessageId());

        switch (update.callbackQuery().data()) {
            case "/delete" -> userState.setState(new KeyDeleteValue(userState, bot));
            case "/get" -> userState.setState(new KeyGetValue(userState, bot));
            case "/delete_all" -> userState.setState(new DeleteAll(userState, bot));
            case "/add" -> userState.setState(new KeyAddValue(userState, bot));
            case "/cancel", "/hideMessage" -> {
                bot.execute(new DeleteMessage(chatId, messageId));
                // todo заменить на логирование
                System.out.println(">>> Пришел колбэк в Idle: " + update.callbackQuery().data() +
                        " от messageId: " + messageId + ": при этом lastSendMessageId: " +
                        userState.getLastMessageId());
            }
            case "/getMenu" -> userState.setState(new Idle(userState, bot));
            default -> {
                // todo заменить на логирование
                System.out.println(">>> Попал в default колбэк в Idle: " + update.callbackQuery().data() +
                        " от messageId: " + messageId + ": при этом lastSendMessageId: " +
                        userState.getLastMessageId());
            }
        }
    }

    private void handleMessage(Update update) {
        var chatId = update.message().chat().id();
        var messageId = update.message().messageId();
        bot.execute(new DeleteMessage(chatId, messageId));
        if (update.message().text().equals("/start")) {
            userState.resetLastMessageId(update.message().messageId(), update.message().chat().id(), bot);
            userState.setState(new Idle(userState, bot));
            // todo
            System.out.println("-19- Прошел Idle.handleMessage команду /start. lastSendMessageId: " +
                    userState.getLastMessageId());
        } else {
            // todo технически такого случиться не должно, т.к. других команд нет, решить что делать
            var sendResponse = bot.execute(new SendMessage(chatId,
                    "Что-то пошло не так, попробуйте использовать [меню]")
                    .replyMarkup(BotKeyboard.getMenuKeyboard));
            userState.setLastMessageId(sendResponse.message().messageId());
            // todo заменить на логирование
            System.out.println("-15- Пришло неизвестное сообщение в Idle отправлено сообщение с меню: lastSendMessageId" +
                    " обновлен: " + userState.getLastMessageId());
        }
    }

}
