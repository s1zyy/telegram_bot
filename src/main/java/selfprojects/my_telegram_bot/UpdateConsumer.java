package selfprojects.my_telegram_bot;

import lombok.SneakyThrows;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;

    public UpdateConsumer(@Value("${bot.token}") String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
    }

    @Override
    public void consume(Update update) {
        var chatId = update.getMessage().getChatId();
        SendMessage sendMessage = SendMessage
                .builder()
                .text(update.getMessage().getText())
                .chatId(chatId)
                .build();
        try{
            telegramClient.execute(sendMessage);
        }
        catch (TelegramApiException e){
            System.out.println(e.getMessage());
        }
        System.out.printf("Update received from %s, message is: %s%n", update.getMessage().getText(), update.getMessage().getChatId());
    }
}