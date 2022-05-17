package ru.jproj.pocketstorage.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import ru.jproj.pocketstorage.util.PropertiesUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Bot {

    private static final String BOT_TOKEN_KEY = "bot.token";
    //    private static final String WEBHOOK_URL = "bot.webhook.url";
    private final TelegramBot bot = new TelegramBot(PropertiesUtil.get(BOT_TOKEN_KEY));
    private final Map<Long, UserState> cashUser = new HashMap<>();

    public void startup() {
        bot.setUpdatesListener(updates -> {
            updates.forEach(this::process);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void process(Update update) {

        Optional<Long> userId = findUserId(update);
        // todo тут пропускаю сообщения приходящие не от пользователя, а из канала
        //  сообщения из канала идут в update.channel_post и в мой обработчик не попадают
        //  но забивают очередь
        if (userId.isEmpty()) {
            System.out.println("Update: " + update.updateId() +
                    " -> Нет userId в message или callbackQuery неизвестным отправителем.");
            return;
        }

        var userState = cashUser.get(userId.get());
        if (userState == null) {
            userState = new UserState(userId.get(), bot);
            cashUser.put(userId.get(), userState);
        }
        userState.getState().handle(update);

    }

    private Optional<Long> findUserId(Update update) {
        Long id = null;
        if (update.message() != null) {
            id = update.message().from().id();
        } else if (update.callbackQuery() != null) {
            id = update.callbackQuery().from().id();
        }
        return Optional.ofNullable(id);
    }

}
