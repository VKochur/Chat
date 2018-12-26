package kvv.education.khasang.java1.chat.model;

import kvv.education.khasang.java1.chat.model.basic_entity.Dialog;
import kvv.education.khasang.java1.chat.model.basic_entity.Message;
import kvv.education.khasang.java1.chat.model.basic_entity.User;

import java.io.IOException;
import java.util.*;

/**
 * Функции, которые должен реализовывать функционал являющийся чатом
 */
public interface Chatable {

    /**
     * Коннектор для связи с хранилищем данных
     * При реализации метода setStorageConnector, следует добавить метод makeExit() для сброса настроек текущего пользователя, диалога, списка сообщений
     * т.е. должно быть примерно:
     * ..
     * <p>
     * public void setStorageConnector(StorageConnector connector){
     * ...
     * connectorThere = connector;
     * makeExit();
     * }
     */
    void setStorageConnector(StorageConnector storageConnector);

    /**
     * При реализации метода следует выкинуть UndefinedConnectorException при connector == null
     *
     * @return
     * @throws UndefinedConnectorException
     */
    StorageConnector getStorageConnector() throws UndefinedConnectorException;

    /**
     * При реализации метода следует выкинуть UndefinedUserException при user == null
     *
     * @return
     * @throws UndefinedUserException
     */
    User getCurrentUser() throws UndefinedUserException;

    /**
     * Для переопределения, это сеттер.
     * Не следует вызывать данный метод в классах реализующий интерфейс для изменения текущего пользователя
     * Для изменения текущего пользователя стоит использовть changeUser(newUser)
     */
    void setCurrentUser(User user);

    /**
     * При реализации метода следует выкинуть UndefinedDialogException при dialog == null
     *
     * @return
     * @throws UndefinedDialogException
     */
    Dialog getCurrentDialog() throws UndefinedDialogException;

    /**
     * Данный метод предназначен для переопределения, это сеттер.
     * Не следует вызывать данный метод в классах реализующий интерфейс для изменения текущей беседы,
     * следует использовать changeDialog(newDialog)
     *
     * @param newDialog
     */
    void setCurrentDialog(Dialog newDialog);

    //вывод сообщения
    void outMessageForSee(Message message, String autor);

    //список сообщений показанных текущему пользователю
    List<Message> getMappedMessages();

    //устанавливает список показанных текущему пользователю сообщений
    void setMappedMessages(List<Message> mappedMessages);

    //----------------- что касается общих задач по чату --------------------------------

    /**
     * Создание нового пользователя чата
     *
     * @param login
     * @param password для входа в чат
     * @return созданный пользователь
     * @throws NonUniqueException          указанный пользователь уже существует
     * @throws UndefinedConnectorException не указан способ соединения с хранилищем
     */
    default User createUser(String login, String password) throws NonUniqueException, UndefinedConnectorException, IOException {
        User User = getStorageConnector().createUser(login, password);
        return User;
    }

    /**
     * Создание нового диалога. Должен предварительно указан текущий пользователь. В созданный диалог текущий пользователь будет добавлен как участник беседы.
     *
     * @param dialogName
     * @return созданный диалог
     * @throws NonexistentEntitytException при правильной реализации Chatable возникать исключения не должно.
     *                                     Исключение возможно, когда в качестве текущего пользователя данные указаны не из хранилища, т.е. метод setCurrentUser() переопределен так, что принимает в качестве своего аргумента
     *                                     пользователя не взятого из хранилища, или changeUser(user) в качестсве аргумента принял пользователя указанного не из хранилища
     * @throws UndefinedConnectorException не указан способ соединения с хранилищем
     */
    default Dialog createDialog(String dialogName) throws NonexistentEntitytException, UndefinedConnectorException, UndefinedUserException, IOException {
        //если текущего пользователя нет, кинет экспшн
        int idUser = getCurrentUser().getId();
        //в хранилище создаем диалог
        Dialog dialog = getStorageConnector().createDialog(dialogName);
        //в хранилище в диалог добавляем пользователя
        try {
            getStorageConnector().addUserToDialog(dialog.getId(), idUser);
        } catch (NonexistentEntitytException e) {
            String temp = new StringBuilder().
                    append("Не корректная реализация Chatable. В качестве текущего каталога или пользователя указаны данные не из хранилища. ").
                    append(e.getMessage()).toString();
            throw new NonexistentEntitytException(temp);
        }
        return dialog;
    }

    /**
     * Создание нового диалога чата с участниками.
     *
     * @param dialogName название создаваемого диалога
     * @param idUsers    id пользователей участников создаваемого диалога
     * @return созданный диалог
     * @throws NonexistentEntitytException не корректные id пользователей
     * @throws UndefinedConnectorException не указан способ соединения с хранилищем
     */
    default Dialog createDialog(String dialogName, Set<Integer> idUsers) throws NonexistentEntitytException, UndefinedConnectorException, IOException {
        Dialog dialog = getStorageConnector().createDialog(dialogName);
        for (Integer idUser : idUsers) {
            getStorageConnector().addUserToDialog(dialog.getId(), idUser);
        }
        return dialog;
    }

    /**
     * Добавление пользователя в текущую беседу
     *
     * @param idUser
     * @throws NonexistentEntitytException не корректные id пользователя или диалог текущий не из хранилища
     * @throws UndefinedConnectorException не указан способ соединения с хранилищем
     */
    default boolean addUserIntoDialog(int idUser) throws NonexistentEntitytException, UndefinedConnectorException, UndefinedDialogException, IOException {
        return getStorageConnector().addUserToDialog(getCurrentDialog().getId(), idUser);
    }

    /**
     * Множество диалогов, в которых текущий пользователь является участником
     *
     * @throws NonexistentEntitytException текущий пользователь не из базы
     * @throws UndefinedConnectorException не указан способ соединения с хранилищем
     */
    default Set<Dialog> getAvailableDialogs() throws NonexistentEntitytException, UndefinedConnectorException, UndefinedUserException, IOException {
        return getStorageConnector().getDialogByUsers(getCurrentUser().getId());
    }

    /**
     * Множество участников диалога текущего каталога
     *
     * @throws NonexistentEntitytException не корректно указан диалог
     * @throws UndefinedConnectorException не указан способ соединения с хранилищем
     */
    default Set<User> getDialogUsers() throws NonexistentEntitytException, UndefinedConnectorException, UndefinedDialogException, IOException {
        return getStorageConnector().getUserByDialog(getCurrentDialog().getId());
    }

    /**
     * Возвращает пользователя по логину
     *
     * @param login
     * @return null, если не найден
     * @throws UndefinedConnectorException не указан способ соединения с хранилищем
     */
    default User getUserByLogin(String login) throws UndefinedConnectorException, IOException {
        return getStorageConnector().getUserByLogin(login);
    }

    /**
     * @param id
     * @return
     * @throws UndefinedConnectorException не указан способ соединения с хранилищем
     * @throws IOException
     */
    default User getUserById(int id) throws UndefinedConnectorException, IOException {
        return getStorageConnector().getUserById(id);
    }

    /**
     * Возвращает диалог по id
     *
     * @param idDialog
     * @return null, если не найден
     * @throws UndefinedConnectorException не указан способ соединения с хранилищем
     */
    default Dialog getDialogById(int idDialog) throws UndefinedConnectorException, IOException {
        return getStorageConnector().getDialogById(idDialog);
    }

    /**
     * Проверка индентификации
     *
     * @param login
     * @param password
     * @return
     * @throws UndefinedConnectorException не указан способ соединения с хранилищем
     */
    default boolean checkIndentification(String login, String password) throws UndefinedConnectorException, IOException {
        return getStorageConnector().checkIndentification(login, password);
    }

    default boolean makeEnter(String login, String password) throws UndefinedConnectorException, IOException {
        makeExit();
        if (checkIndentification(login, password)) {
            User user = getUserByLogin(login);
            changeUser(user);
            return true;
        } else {
            return false;
        }
    }

    /**
     * В качестве newUser должны быть пользователи взятые из хранилища. Если в качестве аргумента будет
     * указан пользователь не из хранилища, это приведет к исключению NonexistentEntitytException при вызове других методов интерфейса
     *
     * @param newUser
     */
    default void changeUser(User newUser) {
        setCurrentUser(newUser);
        setCurrentDialog(null);
        setMappedMessages(new LinkedList<>());
    }

    default void makeExit() {
        setCurrentUser(null);
        setCurrentDialog(null);
        setMappedMessages(new LinkedList<>());
    }


    /**
     * Изменяет текущий каталог на указанный,
     * Показывает сообщения указанного каталога, взятые из хранилища
     * <p>
     * Все происходит при условии что текущий пользователь является участником
     * указанного диалога, и dialog != null.
     * В качестве dialog следует указывать диалог взятый из хранилища
     *
     * @param dialog
     * @return
     * @throws NonexistentEntitytException в случае если указанный диалог не из хранилища
     * @throws UndefinedConnectorException не указан способ соединения с хранилищем
     */
    default boolean changeDialog(Dialog dialog) throws NonexistentEntitytException, UndefinedConnectorException, UndefinedUserException, UndefinedDialogException, IOException {
        if (dialog == null) return false;
        Set<User> users = getStorageConnector().getUserByDialog(dialog.getId());
        //текущим диалогом может стать только диалог в котором текущий пользователь участвует
        if (users.contains(getCurrentUser())) {
            setMappedMessages(new LinkedList<>());
            setCurrentDialog(dialog);
            //показываем сообщения диалога
            List<Message> fromStrore = getMessagesFromStore();
            outListMessagesForUserCanSee(fromStrore);
            return true;
        } else {
            return false;
        }
    }

    //----------- что касается отображения сообщений диалога для пользователя -------------------------

    /**
     * получает из хранилища весь список сообщений текущего диалога в порядке их добавления
     *
     * @return
     * @throws NonexistentEntitytException если текущий диалог не из хранилища
     * @throws UndefinedConnectorException не указан способ соединения с хранилищем
     */
    default List<Message> getMessagesFromStore() throws NonexistentEntitytException, UndefinedConnectorException, UndefinedDialogException, IOException {
        return getStorageConnector().getMessages(getCurrentDialog().getId());
    }

    /**
     * Обновляет показание сообщений текущего каталога исходя из уже показанных пользователю сообщений и сообщений в источнике данных:
     * если в источнике данных появились сообщения еще не показанные, то показывает их
     *
     * @throws NonexistentEntitytException в случае если в качестве текущего каталога указан не каталог из хранилища
     * @throws UndefinedConnectorException не указан способ соединения с хранилищем
     *                                     <p>
     *                                     возвращает список новых сообщений
     */
    default List<Message> updateMappedMessages() throws NonexistentEntitytException, UndefinedConnectorException, UndefinedDialogException, IOException {
        List<Message> newMessages = new LinkedList<>();
        //получаем максимальный список сообщений из источника
        newMessages.addAll(getMessagesFromStore());
        //список не отображенных сообщений
        newMessages.removeAll(getMappedMessages());
        //отобразим
        outListMessagesForUserCanSee(newMessages);
        return newMessages;
    }


    /**
     * Отображает сообщение пользователю
     *
     * @param message
     * @throws UndefinedConnectorException
     */
    default void outMessageForUserCanSee(Message message) throws UndefinedConnectorException, IOException {
        List<Message> temp = getMappedMessages();
        temp.add(message);
        String autor = getStorageConnector().getUserById(message.getIdAuthor()).getLogin();
        outMessageForSee(message, autor);
    }

    //отображает список сообщений пользователю
    default void outListMessagesForUserCanSee(List<Message> messages) throws UndefinedConnectorException, IOException {
        for (Message message : messages) {
            outMessageForUserCanSee(message);
        }
    }


    //--------------------------- что касается отправки сообщений пользователем -------------------------------------

    //подготовленный текст сообщения
    String getPreparedString();

    //отправляет в текущую беседу сообщение от текущего пользователя

    /**
     * Отправляет сообщение пользователя:
     * Текст сообщения берется из getPreparedString(), беседа - текущая, автор- текущий пользователь
     * Сообщение сохраняется в хранилище
     *
     * @throws NonexistentEntitytException текущая беседа, или пользователь взяты не из хранилища
     * @throws UndefinedConnectorException не указан вариант доступа к хранилищу
     *                                     <p>
     *                                     возвращает список новых выведенных сообщений
     */
    default List<Message> sendMessageFromUser() throws NonexistentEntitytException, UndefinedConnectorException, UndefinedDialogException, UndefinedUserException, IOException {
        List<Message> newMessages = new ArrayList<>();
        String text = getPreparedString();
        //отправляем только не пустые сообщения
        if ((text != null) && (!Objects.equals(text, ""))) {
            //создать сообщение в хранилище
            getStorageConnector().createMessage(text, getCurrentDialog().getId(), getCurrentUser().getId());
            //добавить недостающие сообщения в окно чата (возможно с момента последнего обновления кто-то что-то написал + последнее отправленное сообщение)
            newMessages = updateMappedMessages();
        }
        return newMessages;
    }
}
