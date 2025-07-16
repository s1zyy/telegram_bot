package selfprojects.my_telegram_bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;

    public UpdateConsumer(@Value("${bot.token}") String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
    }

    @Override
    public void consume(Update update) {

        if(update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            var chatId = update.getMessage().getChatId();

            if(messageText.equals("/start")){
                sendMainMenu(chatId);
            }
            else if(messageText.equals("/keyboard")){
                sendReplyKeyboard(chatId);
            }
            else if(messageText.equals("Hello")){
                sendMyName(chatId,update.getMessage().getFrom());
            }
            else if(messageText.equals("Picture")){
                sendPicture(chatId);
            }

            else{
                sendMessage(chatId,"I don't understand that command!");

            }
        }
        else if(update.hasCallbackQuery()){
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void sendReplyKeyboard(Long chatId){
        SendMessage sendMessage = SendMessage
                .builder()
                .text("This is your reply keyboard")
                .chatId(chatId)
                .build();

        List<KeyboardRow> inlineKeyboardButtons = List.of(
                new KeyboardRow("Hello"),
                new KeyboardRow("Picture")
        );

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(inlineKeyboardButtons);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        try {
            telegramClient.execute(sendMessage);

        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMainMenu(Long chatId) {
        SendMessage sendMessage = SendMessage
                .builder()
                .text("Welcome, this is your main menu!")
                .chatId(chatId)
                .build();


        var button1 = InlineKeyboardButton
                .builder()
                .text("My name")
                .callbackData("name")
                .build();
        var button2 = InlineKeyboardButton
                .builder()
                .text("Random picture")
                .callbackData("long_process")
                .build();
        var button3 = InlineKeyboardButton
                .builder()
                .text("Random number")
                .callbackData("number")
                .build();


        List<InlineKeyboardRow> buttons = List.of(
                new InlineKeyboardRow(button1),
                new InlineKeyboardRow(button2),
                new InlineKeyboardRow(button3)
        );
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(buttons);

        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        var data =  callbackQuery.getData();
        var chatId = callbackQuery.getMessage().getChat().getId();
        var userId = callbackQuery.getFrom().getId();
        var user = callbackQuery.getFrom();
        switch (data){
            case "name" -> sendMyName(chatId, user);
            case "long_process" -> sendPicture(chatId);
            case "number" -> sendRandom(chatId);
            default -> sendMessage(chatId, "I dont know this command");
        }

    }

    private void sendMessage(Long chatId, String message) {
        SendMessage sendMessage = SendMessage
                .builder()
                .text(message)
                .chatId(chatId)
                .build();
        try{
            telegramClient.execute(sendMessage);
        }
        catch (TelegramApiException e){
            System.out.println(e.getMessage());
        }

    }

    private void sendRandom(Long chatId) {
        var randomInt = ThreadLocalRandom.current().nextInt(0,1000);
        sendMessage(chatId, "Your random number is: " + randomInt);
    }

    private void sendPicture(Long chatId) {
        sendMessage(chatId, "Started loading of the picture");
        new Thread(() -> {
            var imageUrl = "https://picsum.photos/200";

            try {
                URL url = new URL(imageUrl);

                var stream = url.openStream();

                SendPhoto sendPhoto = SendPhoto.builder()
                        .chatId(chatId)
                        .photo(new InputFile(stream,"randomPicture"))
                        .caption("Your random picture: ")
                        .build();
                telegramClient.execute(sendPhoto);

            } catch (IOException | TelegramApiException e) {
                throw new RuntimeException(e);
            }

        }).start();
    }

    private void sendMyName(Long chatId, User user) {
        String name = user.getFirstName();
        String lastName = user.getLastName();

        String fullName = lastName == null ? "Hello " + name : "Hello " + name + " " + lastName;
        sendMessage(chatId,fullName + "\nYour user id is: @" + user.getUserName());

//        if(lastName == null){
//            sendMessage(chatId, "Hello "+name);
//        }
//        else{
//            sendMessage(chatId, "Hello "+name+" "+lastName);
//        }
    }
    
    


}