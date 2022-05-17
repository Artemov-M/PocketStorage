package ru.jproj.pocketstorage.bot.fsm;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import ru.jproj.pocketstorage.bot.UserState;

public abstract class State {
    UserState userState;
    TelegramBot bot;

    public State(UserState userState, TelegramBot bot) {
        this.userState = userState;
        this.bot = bot;
    }

    public abstract void handle(Update update);
}
