package selfprojects.my_telegram_bot;

import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;

@Component
public class MyTelegramBot implements SpringLongPollingBot {

    private final UpdateConsumer updateConsumer;

    public MyTelegramBot(UpdateConsumer updateConsumer) {
        this.updateConsumer = updateConsumer;
    }

    @Value("${bot.token}")
    private String botToken;
    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return updateConsumer;
    }
}
