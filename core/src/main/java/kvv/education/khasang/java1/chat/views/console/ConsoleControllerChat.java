package kvv.education.khasang.java1.chat.views.console;

import kvv.education.khasang.java1.chat.model.*;
import kvv.education.khasang.java1.chat.model.basic_entity.Dialog;
import kvv.education.khasang.java1.chat.model.basic_entity.User;

import java.io.IOException;
import java.util.Set;

public class ConsoleControllerChat {

    private ModelChat model;
    private ViewChat view;

    public ConsoleControllerChat() {

    }

    public void setView(ViewChat view) {
        //загрузим в модель используемый интерфейс пользователя. чтобы модель знала
        //как брать подготовленную строку сообщения для отправки и как выводить сообщения диалога чтобы пользователь их видел
        //т.е. будем в моделе в качестве IO интерфейса использовать views
        //вроде и нет связи между вью и моделью, а вроде и есть.. хотя связь все равно через контроллер, ведь данный метод - метод контроллера
        this.view = view;
        linkedModelView();
    }

    private void linkedModelView() {
        if (model != null) {
            model.setIOChatInterface(view);
        }
    }

    public void setModel(ModelChat modelChat) {
        this.model = modelChat;
        linkedModelView();
    }

    /**
     * Основной цикл интерактивной работы с чатом
     */
    public void interactiveWork() {
        if (model == null) {
            throw new NullPointerException("Модель не определенна");
        }
        if (view == null) {
            throw new NullPointerException("Пользовательский интерфейс не определен");
        }

        while (true) {
            this.showState();
            this.view.showMenu();
            this.view.showMessageToUser("Введите код команды: ");
            int cod = view.getInt();
            ConsoleControllerChat.Cmd cmd = ConsoleControllerChat.Cmd.getByCod(cod);
            if (cmd != null) {
                try {
                    if (processing(cmd) == -1) {
                        break;
                    }
                } catch (UndefinedConnectorException e) {
                    //в моделе не указан коннектор, не указан способ связи с хранилищем информации
                    view.showException(e.getMessage());
                } catch (UndefinedDialogException e) {
                    //попытка работы при не выбранном текущем диалоге
                    view.showException("Не выбран текущий диалог");
                } catch (UndefinedUserException e) {
                    //попытка работы при не выбранном текущем пользователе
                    view.showException("Не выбран текущий пользователь");


                } catch (NonexistentEntitytException e) {
                    //такого возникнуть не должно
                    //указаны данные для отработки, которые не существуют в хранилище
                    String temp = new StringBuilder().append(e.getMessage()).append(" Исключительная ситуация. Не должна возникнуть в следствии реализации ModelChat (текущий пользователь или диалог не из хранилища), ConsoleControllerChat (пользователь ввел не корректные данные)").toString();
                    NonexistentEntitytException tempE = new NonexistentEntitytException(temp);
                    this.processingException(tempE);
                    view.showException("Исключительная ситуация, обратитесь к разработчику");

                } catch (IOException e) {
                    this.processingException(e);
                }

            } else {
                view.showException("Не корректный код команды");
            }
        }
    }

    /**
     * Обработка исключений, с которыми не понятно что делать
     */
    private void processingException(Exception e) {
        //д.б.логирование
        e.printStackTrace();
    }

    /**
     * Выводит информацию о текущем пользователе и текущей беседе
     */
    private void showState() {
        String login;
        try {
            login = this.model.getCurrentUser().getLogin();
        } catch (UndefinedUserException e) {
            this.view.showState("--------------", "--------------");
            return;
        }

        String nameDialog;
        try {
            nameDialog = this.model.getCurrentDialog().getName();
        } catch (UndefinedDialogException e) {
            this.view.showState(login, "-----------------");
            return;
        }

        this.view.showState(login, nameDialog);
    }

    /**
     * @param cmd
     * @return -1 закончить
     */
    private int processing(Cmd cmd) throws UndefinedConnectorException, NonexistentEntitytException, UndefinedDialogException, UndefinedUserException, IOException {
        switch (cmd) {
            case EXIT_APP: {
                return -1;
            }
            case DO_ENTER: {
                doEnter();
                return 0;
            }
            case DO_EXIT: {
                doExit();
                return 0;
            }
            case CHOOSE_DLG: {
                doChooseDialog();
                return 0;
            }
            case UPDATE_MSG: {
                doUpdateWindowMsg();
                return 0;
            }
            case SEND_MSG: {
                doSendMessage();
                return 0;
            }
            case SHOW_DLGS: {
                doShowAvalableDlgs();
                return 0;
            }
            case SHOW_DLG_USERS: {
                doShowDlgUsers();
                return 0;
            }
            case CREATE_NEW_USER: {
                doCreateNewUser();
                return 0;
            }
            case CREATE_NEW_DIALOG: {
                doCreateNewDialog();
                return 0;
            }
            case ADD_USER_TO_DIALOG: {
                doAddUserToDialog();
                return 0;
            }
            case LIST_DLGS: {
                doShowAllDialogs();
                return 0;
            }
            case LIST_USERS: {
                doShowAllUsers();
                return 0;
            }
            default: {
                throw new IllegalArgumentException("Произошло невероятное.");
            }
        }
    }

    /**
     * @throws UndefinedConnectorException не указано соединение с хранилищем информации
     */
    private void doShowAllUsers() throws UndefinedConnectorException, IOException {
        Set<User> users = this.model.getUsers();
        this.view.showMessageToUser("Количество пользователей чата: " + users.size());
        this.view.showMessageToUser("Все пользователи чата:");
        this.view.showUsers(users);
    }

    /**
     * @throws UndefinedConnectorException не указано соединение с хранилищем информации
     */
    private void doShowAllDialogs() throws UndefinedConnectorException, IOException {
        Set<Dialog> dialogs = this.model.getDialogs();
        this.view.showMessageToUser("Количество бесед чата: " + dialogs.size());
        this.view.showMessageToUser("Все беседы чата:");
        this.view.showDialogs(dialogs);
    }

    /**
     * @throws UndefinedConnectorException не указано соединение с хранилищем
     * @throws UndefinedDialogException    не выбран текущий диалог
     * @throws NonexistentEntitytException текущий диалог взят не из хранилища
     */
    private void doAddUserToDialog() throws UndefinedConnectorException, UndefinedDialogException, NonexistentEntitytException, IOException {
        this.view.showMessageToUser("Добавление нового пользователя в диалог.");
        this.view.showMessageToUser("Логин: ");
        String login = this.view.getString();
        Integer idUser = this.model.getStorageConnector().getIdUserByLogin(login);
        if (idUser == null) {
            this.view.showMessageToUser("Пользователь с логином '" + login + "' не зарегистрирован");
            return;
        }
        if (this.model.addUserIntoDialog(idUser)) {
            this.view.showMessageToUser("Пользователь '" + login + "' добавлен в диалог");
        } else {
            this.view.showMessageToUser("Пользователь '" + login + "' уже является участником диалога");
        }
    }

    /**
     * @throws UndefinedUserException      не выбран текущий пользователь
     * @throws UndefinedConnectorException не указано соединение с хранилищем
     * @throws NonexistentEntitytException текущий каталог взят не из хранилища
     */
    private void doCreateNewDialog() throws UndefinedUserException, UndefinedConnectorException, NonexistentEntitytException, IOException {
        this.view.showMessageToUser("Добавление нового диалога в чат");
        this.view.showMessageToUser("Введите название нового диалога: ");
        String nameDialog = this.view.getString();
        Dialog dialog = this.model.createDialog(nameDialog);
        this.view.showMessageToUser("Диалог '" + nameDialog + "' создан. Его код: " + dialog.getId());
    }

    /**
     * @throws UndefinedConnectorException не указано соединение с хранилищем
     */
    private void doCreateNewUser() throws UndefinedConnectorException, IOException {
        this.view.showMessageToUser("Добавление нового пользователя в чат");
        this.view.showMessageToUser("Логин: ");
        String login = this.view.getString();
        this.view.showMessageToUser("Пароль: ");
        String password = this.view.getString();
        try {
            this.model.createUser(login, password);
            this.view.showMessageToUser("Пользователь '" + login + "' добавлен");
        } catch (NonUniqueException e) {
            this.view.showMessageToUser("Пользователь не создан. Логин '" + login + "' уже существует.");
        }
    }

    /**
     * @throws UndefinedConnectorException не указано соединение
     * @throws UndefinedDialogException    не выбран текущий диалог
     * @throws NonexistentEntitytException в качестве текущего диалог выбран не из хранилища
     */
    private void doShowDlgUsers() throws UndefinedConnectorException, UndefinedDialogException, NonexistentEntitytException, IOException {
        Set<User> users = this.model.getDialogUsers();
        this.view.showMessageToUser("Количество участников беседы: " + users.size());
        this.view.showMessageToUser("Участники беседы:");
        this.view.showUsers(users);

    }

    /**
     * @throws UndefinedUserException      не выбран текущий пользователь
     * @throws NonexistentEntitytException в качестве текущего пользователя выбран пользователь не из хранилища
     * @throws UndefinedConnectorException не указано соединение с хранилищем
     */
    private void doShowAvalableDlgs() throws UndefinedUserException, NonexistentEntitytException, UndefinedConnectorException, IOException {
        Set<Dialog> dialogs = this.model.getAvailableDialogs();
        this.view.showMessageToUser("Количество доступных бесед: " + dialogs.size());
        this.view.showMessageToUser("Беседы, в которых принимает участие " + model.getCurrentUser().getLogin() + ":");
        this.view.showDialogs(dialogs);
    }

    /**
     * @throws UndefinedDialogException    не выбран текущий диалог
     * @throws UndefinedUserException      не выбран текущий пользователь
     * @throws NonexistentEntitytException в качестве текущих указанны не из хранилища
     * @throws UndefinedConnectorException не указано соединение
     */
    private void doSendMessage() throws UndefinedDialogException, UndefinedUserException, NonexistentEntitytException, UndefinedConnectorException, IOException {
        this.model.sendMessageFromUser();
    }

    /**
     * @throws NonexistentEntitytException в качестве текущих указаны не из хранилища
     * @throws UndefinedConnectorException не указано соединеие
     * @throws UndefinedDialogException    указан диалог не из хранилища
     */
    private void doUpdateWindowMsg() throws NonexistentEntitytException, UndefinedConnectorException, UndefinedDialogException, IOException {
        this.model.updateMappedMessages();
    }

    /**
     * @throws UndefinedUserException      не указан пользователь
     * @throws NonexistentEntitytException в качестве текущих не из хранилища
     * @throws UndefinedConnectorException не указано соединение
     * @throws UndefinedDialogException    указаный диалог не из хранилища
     */
    private void doChooseDialog() throws UndefinedUserException, UndefinedConnectorException, UndefinedDialogException, NonexistentEntitytException, IOException {
        Set<Dialog> dialogs = this.model.getAvailableDialogs();
        this.view.showMessageToUser("Доступные беседы:");
        for (Dialog dialog : dialogs) {
            this.view.showMessageToUser(String.format("%s. Код: %d", dialog.getName(), dialog.getId()));
        }
        this.view.showMessageToUser("Введите код диалога:");
        int cod = this.view.getInt();
        Dialog newDialog = this.model.getDialogById(cod);
        if (this.model.changeDialog(newDialog)) {
            this.view.showMessageToUser("Выбран диалог: " + newDialog.getName());
        } else {
            this.view.showMessageToUser("Текущий диалог не изменен");
        }
    }


    private void doExit() {
        String login;
        try {
            login = model.getCurrentUser().getLogin();
            this.model.makeExit();
            this.view.doExit(login);
        } catch (UndefinedUserException e) {
            //текущего пользователя не было, ничего не делать
        }
    }

    /**
     * @return
     * @throws UndefinedConnectorException не указано соединение
     */
    private boolean doEnter() throws UndefinedConnectorException, IOException {
        doExit();
        this.view.showMessageToUser("Введите логин:");
        String login = this.view.getString();
        this.view.showMessageToUser("Введите пароль:");
        String password = this.view.getString();
        boolean result = this.model.makeEnter(login, password);
        this.view.doEnter(login, result);
        return result;
    }

    public enum Cmd {
        EXIT_APP(0, "Закончить работу чата"),
        DO_ENTER(1, "Войти в чат"),
        DO_EXIT(2, "Выйти из чата"),
        CHOOSE_DLG(3, "Выбрать диалог"),
        UPDATE_MSG(4, "Обновить окно сообщений текущего диалога"),
        SEND_MSG(5, "Отправить сообщение"),
        SHOW_DLGS(6, "Показать доступные диалоги"),
        SHOW_DLG_USERS(7, "Показать участников диалога"),

        CREATE_NEW_USER(11, "Создать нового пользователя чата"),
        CREATE_NEW_DIALOG(12, "Создать новый диалог в чате"),
        ADD_USER_TO_DIALOG(13, "Добавить в текущий диалог пользователя"),
        LIST_DLGS(14, "Посмотреть список диалогов чата "),
        LIST_USERS(15, "Посмотреть пользователей чата");

        int cod;
        String comment;

        Cmd(int cod, String comment) {
            this.cod = cod;
            this.comment = comment;
        }

        public int getCod() {
            return cod;
        }

        public String getComment() {
            return comment;
        }

        static ConsoleControllerChat.Cmd getByCod(int cod) {
            ConsoleControllerChat.Cmd.values();
            for (int i = 0; i < Cmd.values().length; i++) {
                if (ConsoleControllerChat.Cmd.values()[i].getCod() == cod) {
                    return ConsoleControllerChat.Cmd.values()[i];
                }
            }
            return null;
        }
    }

    public ModelChat getModel() {
        return model;
    }
}
