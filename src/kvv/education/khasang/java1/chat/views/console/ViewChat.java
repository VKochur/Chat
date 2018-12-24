package kvv.education.khasang.java1.chat.views.console;

import kvv.education.khasang.java1.chat.model.IOChat;
import kvv.education.khasang.java1.chat.model.basic_entity.Dialog;
import kvv.education.khasang.java1.chat.model.basic_entity.User;

import java.util.Set;

/**
 * Интерфейс взаимодействия программы с пользователем
 * Обеспечивает ввод/вывод информации для пользователя
 */
public interface ViewChat extends IOChat {

    /**
     * Вывод сообщения пользователю
     *
     * @param text
     */
    void showMessageToUser(String text);

    /**
     * Ввод пользователем числа
     *
     * @return
     */
    int getInt();

    /**
     * Ввод пользователем строки
     */
    String getString();

    /**
     * Отображение программы для пользователя в ответ на попытку входа
     *
     * @param login
     * @param result результат попытки
     */
    void doEnter(String login, boolean result);


    /**
     * Отображение программы для пользователя в ответ на выход пользователя
     *
     * @param login
     */
    void doExit(String login);


    /**
     * Меню программы
     */
    void showMenu();

    /**
     * Вывод информации по множеству диалогов
     *
     * @param availableDialogs
     */
    void showDialogs(Set<Dialog> availableDialogs);

    /**
     * Вывод информации по участникам беседы
     */
    void showUsers(Set<User> Users);


    /**
     * Вывод состояния (текущий пользователь, диалог)
     */
    void showState(String login, String nameDialog);

    /**
     * Вывод информации об исключении
     *
     * @param text
     */
    void showException(String text);
}
