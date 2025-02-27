package org.example;

import Command.CommandHandler;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.*;

import static java.sql.DriverManager.getConnection;

public class AdminChatBot extends TelegramLongPollingBot {
    // Используем уже созданный объект CommandHandler
    private final CommandHandler commandHandler = new CommandHandler();

    // Параметры подключения к базе данных
    public static final String DB_URL = "jdbc:postgresql://localhost:5432/ArmourUtd";
    public static final String USER = "postgres";
    public static final String PASSWORD = "qwerty1378AA";
    private Connection connection;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String text = message.getText();

            if (text.startsWith("*")) {
                Long chatId = message.getChatId();
                User user = message.getFrom();

                int tg_id = Math.toIntExact(user.getId());
                int src_group = getUsersrc_group(tg_id);

                String[] commandParts = text.split(" ");

                // Используем уже созданный объект commandHandler
                String response = commandHandler.handleCommand(user, commandParts, null, commandParts[0], src_group, tg_id);

                if (response != null) {
                    sendMessage(String.valueOf(chatId), response);
                } else {
                    sendMessage(String.valueOf(chatId), "Команда не найдена.");
                }
            }
        }
    }

    public void sendMessage(String chatId, String text) {
        if (text != null) {
            SendMessage message = new SendMessage(chatId, text);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("типа чет написал");
        }
    }

    public int getUsersrc_group(int tg_id) {
        int src_group = 0;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT src_group FROM \"tags\" WHERE tg_id = ?")) {
            statement.setInt(1, tg_id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                src_group = resultSet.getInt("src_group");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return src_group;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        }
        return connection;
    }

    @Override
    public String getBotUsername() {
        return "ArmUtdBot";
    }

    @Override
    public String getBotToken() {
        return "bot_token";
    }
}

