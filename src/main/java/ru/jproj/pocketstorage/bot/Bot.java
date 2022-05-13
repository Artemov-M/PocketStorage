package ru.jproj.pocketstorage.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import ru.jproj.pocketstorage.util.PropertiesUtil;

public class Bot {

    private static final String BOT_TOKEN_KEY = "bot.token";
    //    private static final String WEBHOOK_URL = "bot.webhook.url";
    private static final Object CHAT_ID = "838251670";
    private final TelegramBot bot = new TelegramBot(PropertiesUtil.get(BOT_TOKEN_KEY));
    private final ReplyKeyboardMarkup mainKeyboard = new ReplyKeyboardMarkup("\uD83D\uDCD6 read", "✍️️ add")
            .addRow("❌ delete all", "\uD83D\uDDD1️ delete", "\u2139 help")
            .resizeKeyboard(true);


    /*=====================*/

//    private final InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(
//            new InlineKeyboardButton[][]{
//                    {new InlineKeyboardButton("\uD83D\uDDD1️ delete"), new InlineKeyboardButton("\uD83D\uDCD6 get")},
//                    {new InlineKeyboardButton("❌ all delete"), new InlineKeyboardButton("✍️️ add")}
//            }
//    );

    private final InlineKeyboardButton deleteButton = new InlineKeyboardButton("\uD83D\uDDD1️ delete").callbackData("/delete");
    private final InlineKeyboardButton getButton = new InlineKeyboardButton("\uD83D\uDCD6 get").callbackData("/get");
    private final InlineKeyboardButton deleteAllButton = new InlineKeyboardButton("\uD83E\uDDE8 delete all").callbackData("/delete_all");
    private final InlineKeyboardButton addButton = new InlineKeyboardButton("✍️️ add").callbackData("/add");
    private final InlineKeyboardButton cancelButton = new InlineKeyboardButton("❌️️ cancel").callbackData("/cancel");
    private final InlineKeyboardButton reallyDeleteButton = new InlineKeyboardButton("\uD83E\uDDE8 delete all")
            .callbackData("/really_delete");
    private final InlineKeyboardMarkup startKeyboard = new InlineKeyboardMarkup(
            new InlineKeyboardButton[] {deleteButton, getButton},
            new InlineKeyboardButton[] {deleteAllButton, addButton});
    private final InlineKeyboardMarkup cancelKeyboard = new InlineKeyboardMarkup(cancelButton);
    private final InlineKeyboardMarkup reallyDeleteKeyboard = new InlineKeyboardMarkup(reallyDeleteButton);

    SendResponse previousSendResponse = null;

    /*=====================*/


    public void startup() {

//        var sendResponse = bot.execute(new SendMessage(CHAT_ID, "menu").replyMarkup(mainKeyboard));

        bot.setUpdatesListener(updates -> {
            updates.forEach(this::process);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });


    }

    private void process(Update update) {
        var getMessage = update.message();
        var callbackQuery = update.callbackQuery();

        BaseRequest request = null;
        if (callbackQuery != null) {
            var chatId = callbackQuery.message().chat().id();
            var messageId = callbackQuery.message().messageId();
            String data = callbackQuery.data();

            switch (data) {
                case "/get":
                    bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(callbackQuery.data()));
                    bot.execute(new EditMessageText(chatId, messageId,
                            "Чтобы получить заметки введите ключ"));
                    bot.execute(new EditMessageReplyMarkup(chatId, messageId).replyMarkup(cancelKeyboard));
                    break;
                case "/add":
                    bot.execute(new AnswerCallbackQuery(callbackQuery.id()));
                    bot.execute(new EditMessageText(chatId, messageId,
                            "Чтобы добавить заметку введите ключ"));
                    bot.execute(new EditMessageReplyMarkup(chatId, messageId).replyMarkup(cancelKeyboard));
                    break;
                case "/delete":
                    bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(callbackQuery.data()).showAlert(true));
                    bot.execute(new EditMessageText(chatId, messageId,
                            "Чтобы удалить заметку введите ключ"));
                    bot.execute(new EditMessageReplyMarkup(chatId, messageId).replyMarkup(cancelKeyboard));
                    break;
                case "/delete_all":
                    bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text("Это действие удалит все заметки")
                            .showAlert(true));
                    bot.execute(new EditMessageText(chatId, messageId,
                            "Подтвердите удаление всх заметок"));
                    bot.execute(new EditMessageReplyMarkup(chatId, messageId).replyMarkup(reallyDeleteKeyboard));
                    break;
                case "/really_delete":
                case "/cancel":
                default:
                    bot.execute(new AnswerCallbackQuery(callbackQuery.id()));
                    bot.execute(new EditMessageText(chatId, messageId,
                            "Выберите действие которое хотите совершить"));
                    bot.execute(new EditMessageReplyMarkup(chatId, messageId).replyMarkup(startKeyboard));
                    break;
            }
        }

        if (getMessage != null) {
            var chatId = getMessage.chat().id();
            var getMessageId = getMessage.messageId();
            var text = getMessage.text();
//
//            if (previousSendResponse != null) {
//                bot.execute(new DeleteMessage(previousSendResponse.message().chat().id(),
//                        previousSendResponse.message().messageId()));
//                previousSendResponse = null;
//            }

            switch (text) {
                case "/start":
                    request = new SendMessage(chatId, "Выберите действие которое хотите совершить").entities().replyMarkup(startKeyboard);
                    break;
                default:
                    break;
            }


            bot.execute(new DeleteMessage(chatId, getMessageId));
        }

        if (request != null) {
            BaseResponse response = bot.execute(request);
            if (response instanceof SendResponse) {
                System.out.println(((SendResponse) response).message().text());
            }
            System.out.println("------");
        }
    }
}
