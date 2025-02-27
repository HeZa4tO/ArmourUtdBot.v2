package Command;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;


import java.sql.*;
import java.sql.*;

import static javassist.bytecode.SyntheticAttribute.tag;
import static org.example.AdminChatBot.*;

public class CommandProcessor {
    private final CommandHandler commandHandler;

    private Connection connection;

    public CommandProcessor(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }


    /*
    public String processMYID(User user, Message repliedToMessage, String username) {
        if (repliedToMessage != null) {
            int userToBeId = Math.toIntExact(repliedToMessage.getFrom().getId());
            String repliedUserName = repliedToMessage.getFrom().getUserName();

            if (repliedUserName != null && !repliedUserName.isEmpty()) {
                return "ID пользователя " + repliedUserName + " — " + userToBeId;
            } else {
                return "ID пользователя — " + userToBeId;
            }
        } else {
            // Предположим, что у вас есть метод getUserByUsername для получения пользователя по его имени пользователя
            User requestedUser = getUserByUsername(username);
            if (requestedUser != null) {
                int requestedUserId = Math.toIntExact(requestedUser.getId());
                String requestedUserName = requestedUser.getUserName();

                if (requestedUserName != null && !requestedUserName.isEmpty()) {
                    return "ID пользователя " + requestedUserName + " — " + requestedUserId;
                } else {
                    return "ID пользователя — " + requestedUserId;
                }
            } else {
                return "Пользователь с именем " + username + " не найден.";
            }
        }
    }
    */
    public String processHelpAdm(int src_group) {
        if (src_group == 0) {
            return "Недостаточно прав!";
        }
        if (src_group >= 2) {
            return "Список доступных команд:\n\n" +
                    "*help — список всех команд\n" +
                    "*all — отмечает всех участников беседы\n" +
                    "*up — повышение уровня доступа\n" +
                    "*up_tag — повышение уровня доступа с помощью тега\n" +
                    "*dup — понижение уровня доступа\n" +
                    "*dup_tag — понижение уровня доступа с помощью тега\n" +
                    "*reset — сброс уровня доступа\n" +
                    "*reset_tag — сброс уровня доступа с помощью тега\n" +
                    "*src — просмотр уровней доступа участников.\n\n" +
                    "Если возникли какие-либо вопросы или нашли баг/опечатку обращайтесь! — https://t.me/heza4to\n\n" +

                    "Bot created by: rreallyhtturb & dissemblance";
        } else {
            return "Неизвестная команда.";
        }
    }

    public String processHelp() {
        return "Список доступных команд:\n\n" +
                "*help — список всех команд.\n" +
                "*bosses — список главных участников команды.\n" +

                "Если возникли какие-либо вопросы или нашли баг/опечатку обращайтесь! — https://t.me/heza4to\n\n" +

                "Bot created by: rreallyhtturb & dissemblance";
    }


    public String processlistUsers(int src_group) {
        if (src_group == 1) {
            return "Недостаточно прав!";
        }
        StringBuilder userList = new StringBuilder();
        try (Connection connection = commandHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT tg_tag FROM \"tags\"")) {
            ResultSet resultSet = statement.executeQuery();

            boolean foundUsers = false;
            while (resultSet.next()) {
                foundUsers = true;
                String userNickname = resultSet.getString("tg_tag");
                userList.append(" ").append(userNickname);
            }
            if (!foundUsers) {
                return "Никого не найдено";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Что-то пошло не так..";
        }
        return userList.toString();
    }

    public String processBossList() {
        StringBuilder userList = new StringBuilder();
        String sql = "SELECT tg_tag, names FROM \"tags\" WHERE names IS NOT NULL AND src_group >= 2";
        try (Connection connection = commandHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();

            boolean foundUsers = false;
            while (resultSet.next()) {
                foundUsers = true;
                String userNickname = resultSet.getString("tg_tag");
                String userName = resultSet.getString("names");
                userList.append(" Tag: ").append(userNickname).append(" | Name: ").append(userName).append("\n");
            }
            if (!foundUsers) {
                return "Никого не найдено";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Что-то пошло не так...";
        }
        return userList.toString();
    }


    public String processAddUser(String[] args, int src_group) {
        // Проверка наличия правильного количества аргументов
        if (args.length != 2) {
            return "Неверный формат команды *add_user. Используйте *add_user [@user_name] [name].";
        }

        // Извлекаем тег и имя пользователя из аргументов
        String tag = args[0];
        String name = args[1];

        // Проверка, что тег начинается с "@"
        if (!tag.startsWith("@")) {
            return "Тег пользователя должен начинаться с символа @.";
        }

        // Проверка прав доступа
        if (src_group != 3) {
            return "Недостаточно прав для выполнения операции.";
        }

        // Выполняем вставку в базу данных
        try (Connection connection = commandHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO tags (tg_tag, names) VALUES (?, ?)")) {
            statement.setString(1, tag);   // Тег пользователя
            statement.setString(2, name);  // Имя пользователя
            statement.executeUpdate();
            return "Пользователь добавлен в базу данных.";
        } catch (SQLException e) {
            return "Ошибка при добавлении пользователя: " + e.getMessage();
        }
    }


    public String processUpGroup(String[] args, long userId, int src_group) {
        if (args.length == 1) {
            long userToBeUpgradedId;
            try {
                userToBeUpgradedId = Long.parseLong(args[0]);
            } catch (NumberFormatException e) {
                return "ID пользователя должен быть числом";
            }

            if (src_group >= 3) {
                // Проверяем уровень доступа пользователя, которому хотим повысить уровень
                long userAccessLevel = getUserAccessLevel(userToBeUpgradedId);
                if (userAccessLevel == 3) {
                    return "Уровень доступа этого пользователя уже максимальный.";
                }
                // Если уровень доступа не достиг максимального значения, то повышаем его
                upgradeGroup(userToBeUpgradedId);
                return "Уровень доступа пользователя повышен.";
            } else {
                return "Тебе такая команда недоступна.";
            }
        } else {
            return "Неверный формат команды *up. Используйте *up [ID]";
        }
    }

    private void upgradeGroup(long userToBeUpgradedId) {
        try (Connection connection = commandHandler.getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE \"tags\" SET src_group = src_group + 1 WHERE tg_id = ?")) {
            statement.setLong(1, userToBeUpgradedId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String processUpGroupTag(String[] args, long userId, int src_group) {
        if (args.length == 1) {
            String userToBeUpgradedTag = args[0]; // Тег пользователя теперь String
            if (src_group >= 2) {
                int userAccessLevel = getUserAccessLevelTag(userToBeUpgradedTag); // Используем метод для проверки доступа
                if (userAccessLevel == 3) {
                    return "Уровень доступа этого пользователя уже максимальный.";
                }
                upgradeGroupTag(userToBeUpgradedTag); // Понижаем уровень доступа
                return "Уровень доступа пользователя повышен.";
            } else {
                return "Тебе такая команда недоступна.";
            }
        } else {
            return "Неверный формат команды *dup. Используйте *up_tag [user TAG]";
        }
    }


    private void upgradeGroupTag(String userToBeUpgradedId) {
        try (Connection connection = commandHandler.getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE \"tags\" SET src_group = src_group + 1 WHERE tg_tag = ?")) {
            statement.setString(1, userToBeUpgradedId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private long getUserAccessLevel(long userId) {
        try (Connection connection = commandHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT src_group FROM \"tags\" WHERE tg_id = ?")) {
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("src_group");
            } else {
                // Возвращаем значение по умолчанию, если пользователь не найден
                return 0; // или любое другое значение, которое обозначает отсутствие уровня доступа
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Возвращаем значение по умолчанию в случае ошибки
            return 0;
        }
    }


    public String processResetGroup(String[] args, long userId, int src_group) {
        if (args.length == 1) {
            long userToBeUpgradedId; // Используем long вместо int
            try {
                userToBeUpgradedId = Long.parseLong(args[0]); // Используем Long.parseLong для парсинга long
            } catch (NumberFormatException e) {
                return "ID пользователя должен быть числом";
            }
            if (src_group >= 3) {
                resetGroup(userToBeUpgradedId);
                return "Уровень доступа пользователя был сброшен.";
            } else {
                return "Тебе такая команда недоступна.";
            }
        } else {
            return "Неверный формат команды *reset. Используйте *reset [ID]";
        }
    }

    private void resetGroup(long userToBeUpgradedId) {
        try (Connection connection = commandHandler.getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE \"tags\" SET src_group = 1 WHERE tg_tag = ?")) {
            statement.setLong(1, userToBeUpgradedId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public String processResetGroupTag(String[] args, long userId, int src_group) {
        if (args.length == 1) {
            String userToBeUpgradedTag = args[0]; // Тег пользователя теперь String
            if (src_group >= 3) {
                resetGroupTag(userToBeUpgradedTag); // Сбрасываем уровень доступа
                return "Уровень доступа пользователя сброшен.";
            } else {
                return "Тебе такая команда недоступна.";
            }
        } else {
            return "Неверный формат команды *dup. Используйте *reset_tag [user TAG]";
        }
    }

    private void resetGroupTag(String userToBeUpgradedId) {
        try (Connection connection = commandHandler.getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE \"tags\" SET src_group = 1 WHERE tg_tag = ?")) {
            statement.setString(1, userToBeUpgradedId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public String processDUpGroup(String[] args, long userId, int src_group) {
        if (args.length == 1) {
            long userToBeUpgradedId;
            try {
                userToBeUpgradedId = Long.parseLong(args[0]);
            } catch (NumberFormatException e) {
                return "ID пользователя должен быть числом";
            }
            if (src_group >= 3) {
                // Проверяем уровень доступа пользователя, которому хотим повысить уровень
                int userAccessLevel = getUserAccessLevelId(userToBeUpgradedId);
                if (userAccessLevel == 0) {
                    return "Уровень доступа этого пользователя уже минимальный.";
                }
                // Если уровень доступа не достиг максимального значения, то повышаем его
                dupgradeGroup(userToBeUpgradedId);
                return "Уровень доступа пользователя понижен.";
            } else {
                return "Тебе такая команда недоступна.";
            }
        } else {
            return "Неверный формат команды *dup. Используйте *dup [ID]";
        }
    }

    public String processDUpGroupTag(String[] args, int src_group) {
        if (args.length == 1) {
            String userToBeUpgradedTag = args[0]; // Тег пользователя теперь String
            if (src_group >= 2) {
                int userAccessLevel = getUserAccessLevelTag(userToBeUpgradedTag); // Используем метод для проверки доступа
                if (userAccessLevel == 0) {
                    return "Уровень доступа этого пользователя уже минимальный.";
                }
                dupgradeGroupTag(userToBeUpgradedTag); // Понижаем уровень доступа
                return "Уровень доступа пользователя понижен.";
            } else {
                return "Тебе такая команда недоступна.";
            }
        } else {
            return "Неверный формат команды *dup. Используйте *dup_tag [user TAG]";
        }
    }



    private int getUserAccessLevelId(long userTag) {
        try (Connection connection = commandHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT src_group FROM \"tags\" WHERE tg_id = ?")) {
            statement.setLong(1, userTag);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("src_group");
            } else {
                // Возвращаем значение по умолчанию, если пользователь не найден
                return 0; // или любое другое значение, которое обозначает отсутствие уровня доступа
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Возвращаем значение по умолчанию в случае ошибки
            return 0;
        }
    }

    private int getUserAccessLevelTag(String userTag) {
        try (Connection connection = commandHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT src_group FROM \"tags\" WHERE tg_tag = ?")) {
            statement.setString(1, userTag);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("src_group");
            } else {
                // Возвращаем значение по умолчанию, если пользователь не найден
                return 0; // или любое другое значение, которое обозначает отсутствие уровня доступа
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Возвращаем значение по умолчанию в случае ошибки
            return 0;
        }
    }

    private void dupgradeGroupTag(String userToBeUpgradedId) {
        try (Connection connection = commandHandler.getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE \"tags\" SET src_group = src_group - 1 WHERE tg_tag = ?")) {
            statement.setString(1, userToBeUpgradedId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void dupgradeGroup(long userToBeUpgradedId) {
        try (Connection connection = commandHandler.getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE \"tags\" SET src_group = src_group - 1 WHERE tg_id = ?")) {
            statement.setLong(1, userToBeUpgradedId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String processShowUser(int src_group) {
//        if (src_group <= 1) {
//            return "Недостаточно прав!";
//        }

        StringBuilder userList = new StringBuilder("Список всех пользователей: \n\n");

        try (Connection connection = commandHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT src_group, tg_tag, tg_id, names FROM tags"
             )) {

            ResultSet resultSet = statement.executeQuery();
            boolean foundUsers = false;

            while (resultSet.next()) {
                foundUsers = true;
                userList.append("Tag: ").append(resultSet.getString("tg_tag"))
                        .append(" | Name: ").append(resultSet.getString("names"))
                        .append(" | ID: ").append(resultSet.getString("tg_id"))
                        .append(" | Group: ").append(resultSet.getString("src_group"))
                        .append("\n");
            }

            if (!foundUsers) {
                return "Никого не найдено";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Что-то пошло не так..";
        }

        return userList.toString();
    }


    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        }
        return connection;
    }
}