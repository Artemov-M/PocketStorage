package ru.jproj.pocketstorage;

import ru.jproj.pocketstorage.bot.Bot;

public class BotRunner {
    public static void main(String[] args) {
        new Bot().startup();
    }
}
