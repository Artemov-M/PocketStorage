package ru.jproj.pocketstorage.bot.layout;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;

public final class BotKeyboard {

    private static final InlineKeyboardButton deleteButton = new InlineKeyboardButton("\uD83D\uDDD1️ delete")
            .callbackData("/delete");
    private static final InlineKeyboardButton getButton = new InlineKeyboardButton("\uD83D\uDCD6 get")
            .callbackData("/get");
    private static final InlineKeyboardButton deleteAllButton = new InlineKeyboardButton("\uD83E\uDDE8 delete all")
            .callbackData("/delete_all");
    private static final InlineKeyboardButton addButton = new InlineKeyboardButton("✍️️ add")
            .callbackData("/add");
    private static final InlineKeyboardButton cancelButton = new InlineKeyboardButton("❌️️ cancel").
            callbackData("/cancel");
    private static final InlineKeyboardButton hideMessageButton = new InlineKeyboardButton("\uD83D\uDE48 Скрыть")
            .callbackData("/hideMessage");
    private static final InlineKeyboardButton getMenuButton = new InlineKeyboardButton("\uD83D\uDCCB Меню")
            .callbackData("/getMenu");

    public static final InlineKeyboardMarkup idleKeyboard = new InlineKeyboardMarkup(
            new InlineKeyboardButton[]{deleteButton, getButton},
            new InlineKeyboardButton[]{deleteAllButton, addButton});

    public static final InlineKeyboardMarkup cancelKeyboard = new InlineKeyboardMarkup(cancelButton);

    public static final InlineKeyboardMarkup reallyDeleteKeyboard = new InlineKeyboardMarkup(
            cancelButton, deleteAllButton);

    public static final InlineKeyboardMarkup hideMessageKeyboard = new InlineKeyboardMarkup(hideMessageButton);

    public static final InlineKeyboardMarkup getMenuKeyboard = new InlineKeyboardMarkup(getMenuButton);

    private BotKeyboard() {
    }

}
