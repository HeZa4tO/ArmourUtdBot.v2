package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

//Просто запускаешь отсюда
public class main {
    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            AdminChatBot adminChatBot = new AdminChatBot();
            botsApi.registerBot(adminChatBot);
            System.out.println("я запустился)");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}