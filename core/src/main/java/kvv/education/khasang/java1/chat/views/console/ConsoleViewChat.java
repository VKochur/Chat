package kvv.education.khasang.java1.chat.views.console;

import kvv.education.khasang.java1.chat.model.basic_entity.Dialog;
import kvv.education.khasang.java1.chat.model.basic_entity.Message;
import kvv.education.khasang.java1.chat.model.basic_entity.User;

import kvv.console.colors.*;

import java.util.Scanner;
import java.util.Set;

public interface ConsoleViewChat extends ViewChat {
    @Override
    default String getPreparedString() {
        ConsoleColors color = ConsoleColors.TEXT_GREEN;
        Util.printConsolColorText(String.format("Ввод сообщения: "), color);
        return new Scanner(System.in).nextLine();
    }

    @Override
    default void outMessageForSee(Message message, String autor) {
        String forOut = String.format("Окно сообщений | %s %s :  %s", message.getDate().toString(), autor, message.getText());
        ConsoleColors color = ConsoleColors.BG_GREEN;
        Util.printConsolColorText(String.format("%s%n", forOut), color);
    }

    @Override
    default void showMessageToUser(String text) {
        System.out.println(text);
    }

    @Override
    default void doEnter(String login, boolean result) {
        if (result) {
            showMessageToUser("Вход " + login);
        } else {
            showMessageToUser("Аутентификация не прошла");
        }
    }

    @Override
    default void doExit(String login) {
        System.out.println("Выход " + login);
    }

    @Override
    default void showMenu() {
        ConsoleColors color = ConsoleColors.TEXT_BLUE;
        Util.printConsolColorText(String.format("%s%n", "------------------- -----Меню программы-------------------------"), color);
        ConsoleControllerChat.Cmd[] cmds = ConsoleControllerChat.Cmd.values();
        for (int i = 0; i < cmds.length; i++) {
            ConsoleControllerChat.Cmd cmd = ConsoleControllerChat.Cmd.values()[i];
            Util.printConsolColorText(String.format("%d = %s%n", cmd.getCod(), cmd.getComment()), color);
        }
        Util.printConsolColorText(String.format("%n%s%n", "--------------------------------------------------------------"), color);
    }

    @Override
    default void showDialogs(Set<Dialog> availableDialogs) {
        for (Dialog availableDialog : availableDialogs) {
            System.out.println(availableDialog);
        }
    }

    @Override
    default int getInt() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            if (scanner.hasNextInt()) {
                return scanner.nextInt();
            } else {
                System.out.println("Введите целое число: ");
            }
            scanner.nextLine();
        }
    }

    @Override
    default String getString() {
        return new Scanner(System.in).nextLine();
    }

    @Override
    default void showUsers(Set<User> Users) {
        for (User User : Users) {
            System.out.println(User);
        }
    }

    @Override
    default void showState(String login, String nameDialog) {
        System.out.println();
        ConsoleColors color = ConsoleColors.TEXT_BLACK;
        Util.printConsolColorText(String.format("%s%n", "-----------------------ЧАТ----------------------------------------"), color);
        Util.printConsolColorText(String.format("Пользователь: %s  Диалог: %s  %n", login, nameDialog), color);
        Util.printConsolColorText(String.format("%s%n", "------------------------------------------------------------------"), color);
    }

    @Override
    default void showException(String text) {
        ConsoleColors color = ConsoleColors.TEXT_RED;
        Util.printConsolColorText(String.format("%s%n", text), color);
    }
}
