package Command;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

import static org.example.AdminChatBot.*;

//Прописываешь логику ретерна для каждой команды
public class CommandHandler {

    private CommandProcessor commandProcessor;
    private Connection connection;

    public String handleCommand(User user, String[] splitText, Message repliedToMessage, String command, int src_group, long userId) {

        String[] args = splitText.length > 1 ? Arrays.copyOfRange(splitText, 1, splitText.length) : new String[0];

        this.commandProcessor = new CommandProcessor(this);

        switch (command) {
            case "*add_user":
                return commandProcessor.processAddUser(args, src_group);
            case "*help":
                return commandProcessor.processHelp();
            case "*adm_help":
                return commandProcessor.processHelpAdm(src_group);
            case "*all":
                return commandProcessor.processlistUsers(src_group);
            case "*up":
                return commandProcessor.processUpGroup(args, userId, src_group);
            case "*up_tag":
                return commandProcessor.processUpGroupTag(args, userId, src_group);
            case "*dup":
                return commandProcessor.processDUpGroup(args, userId, src_group);
            case "*dup_tag":
                return commandProcessor.processDUpGroupTag(args, src_group);
            case "*reset":
                return commandProcessor.processResetGroup(args, userId, src_group);
            case "*reset_tag":
                return commandProcessor.processResetGroupTag(args, userId, src_group);
            case "*bosses":
                return commandProcessor.processBossList();
            case "*src":
                return commandProcessor.processShowUser(src_group);
            default:
                return null;
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        }
        return connection;
    }
}
