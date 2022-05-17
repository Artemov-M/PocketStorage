package ru.jproj.pocketstorage.bot.fsm;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import ru.jproj.pocketstorage.bot.UserState;
import ru.jproj.pocketstorage.bot.layout.BotKeyboard;

public class Unregistered extends State {

    public Unregistered(UserState userState, TelegramBot bot) {
        super(userState, bot);
    }

    @Override
    public void handle(Update update) {
        if (update.message() != null) {
            handleMessage(update);
        } else {
            // todo технически такого случиться не должно, т.к. на этом этапе еще нет клавиатуры,
            //  решить что делать
            handleCallback(update);
        }
    }

    private void handleMessage(Update update) {
        bot.execute(new DeleteMessage(update.message().chat().id(), update.message().messageId()));
        userState.addInDb();
        if (update.message().text().equals("/start")) {
            var sendResponse = bot.execute(new SendMessage(update.message().chat().id(), "Запуск..."));
            userState.setLastMessageId(sendResponse.message().messageId());
            userState.setState(new Idle(userState, bot));
            // todo заменить на логирование
            System.out.println("-1- Прошёл Unregistered.handleMessage, получил lastSendMessageId: " +
                    userState.getLastMessageId());
        } else {
            // сюда попадаем только если было падение/перезапуск бота
            bot.execute(new DeleteMessage(update.message().chat().id(), update.message().messageId()));
            bot.execute(new SendMessage(update.message().chat().id(),
                    "Что-то пошло не так, попробуйте использовать [меню]")
                    .replyMarkup(BotKeyboard.getMenuKeyboard));
            // todo заменить на логирование
            System.out.println("--- Пришло неизвестное сообщение в Unregistered: ");
        }
    }

    private void handleCallback(Update update) {
        // сюда попадаем только если было падение/перезапуск бота
        bot.execute(new AnswerCallbackQuery(update.callbackQuery().id()));
        userState.addInDb();
        var chatId = update.callbackQuery().message().chat().id();
        var messageId = update.callbackQuery().message().messageId();

        if (update.callbackQuery().data().equals("/getMenu")) {
            userState.resetLastMessageId(messageId, chatId, bot);
            userState.setState(new Idle(userState, bot));
            // todo отладка
            System.out.println("-4- Прошёл Unregistered.handleCallback, getMenu. MessageId: " +
                    messageId + " lastSendMessageId: " +
                    userState.getLastMessageId());
        } else {
            bot.execute(new DeleteMessage(chatId, messageId));
            bot.execute(new SendMessage(chatId, "Что-то пошло не так, попробуйте использовать [меню]")
                    .replyMarkup(BotKeyboard.getMenuKeyboard));
            // todo заменить на логирование
            System.out.println("-3- Пришел колбэк в Unregistere от кнопки " + update.callbackQuery().data() +
                    ": messageId: " + messageId);
        }
    }

}
