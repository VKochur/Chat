package kvv.education.khasang.java1.chat.model.storage.omd;

import kvv.education.khasang.java1.chat.model.storage.file.FileUtil;
import kvv.education.khasang.java1.chat.model.NonUniqueException;
import kvv.education.khasang.java1.chat.model.NonexistentEntitytException;

import java.io.*;
import java.util.*;

/**
 * Хранилище в ОЗУ
 */
public class StorageOMD implements Serializable {

    private static final long serialVersionUID = 1L;

    public static StorageOMD getInstance(String path) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(path)))) {
            return (StorageOMD) in.readObject();
        }
    }

    /**
     * Сохранить экземпляр хранилища в файле
     *
     * @param path
     * @throws IOException
     */
    public static void saveInstance(StorageOMD storageOMD, String path) throws IOException {
        //если файла нет, то создадим
        FileUtil.createFile(path);
        try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(path)))) {
            out.writeObject(storageOMD);
        }
    }

    //id пользователя -> пользователь
    private Map<Integer, TUser> users;
    //id диалога -> диалог
    private Map<Integer, TDialog> dialogs;
    //id сообщения -> сообщение
    private Map<Integer, TMessage> messages;
    //id пользователя -> пароль
    private Map<Integer, String> passwords;
    //логин -> id пользователя
    private Map<String, Integer> logins;

    private Set<Link> userLinkDialog;
    private Set<Link> dialogLinkMessage;

    private UUID idStorage;

    private int counterUsers;
    private int counterDialogs;
    private int counterMessages;

    public StorageOMD() {
        //уникальный индентификатор хранилища
        idStorage = UUID.randomUUID();
        //для реализации функций вставки, поиска log(n)
        this.users = new TreeMap<>();
        this.dialogs = new TreeMap<>();
        this.messages = new TreeMap<>();
        this.passwords = new TreeMap<>();
        this.logins = new TreeMap<>();

        userLinkDialog = new TreeSet<>();
        dialogLinkMessage = new TreeSet<>();

        counterUsers = 0;
        counterDialogs = 0;
        counterMessages = 0;
    }

    public Map<Integer, TUser> getUsers() {
        return Collections.unmodifiableMap(users);
    }

    public Map<Integer, TDialog> getDialogs() {
        return Collections.unmodifiableMap(dialogs);
    }

    /**
     * @param login
     * @param password
     * @return id нового пользователя
     * <p>
     * Выкидывает NonUniqueException в случае если указанный логин уже зарегистрирован
     */
    public Integer createUser(String login, String password) throws NonUniqueException {
        if (logins.get(login) != null) {
            throw new NonUniqueException("Пользователь '" + login + "' уже зарегистрирован");
        } else {
            counterUsers++;
            TUser user = new TUser(this.idStorage, login);
            users.put(counterUsers, user);

            passwords.put(counterUsers, password);
            logins.put(login, counterUsers);
            return counterUsers;
        }
    }

    /**
     * @param dialogName
     * @return id нового диалога
     */
    public Integer createDialog(String dialogName) {
        counterDialogs++;
        TDialog dialog = new TDialog(this.idStorage, dialogName);
        dialogs.put(counterDialogs, dialog);
        return counterDialogs;
    }

    public boolean checkIndentification(String login, String password) {
        Integer idUser = logins.get(login);
        if (idUser != null) {
            String valid = passwords.get(idUser);
            return password.equals(valid);
        } else {
            return false;
        }
    }

    /**
     * Создает новое сообщение
     *
     * @param text
     * @param idDialog
     * @param idAutor
     * @return id созданного сообщения
     * @throws NonexistentEntitytException при не корректных данных о диалоге или авторе
     */
    public Integer createMessage(String text, int idDialog, int idAutor) throws NonexistentEntitytException {
        checkArg(idDialog, idAutor);
        counterMessages++;
        TMessage message = new TMessage(this.idStorage, text, idAutor, new Date());
        messages.put(counterMessages, message);
        dialogLinkMessage.add(new Link(idDialog, counterMessages));
        return counterMessages;
    }

    private void checkArg(int idDialog, int idAutor) throws NonexistentEntitytException {
        if (!users.containsKey(idAutor)) {
            throw new NonexistentEntitytException("Отсутствует пользователь с id = " + String.valueOf(idAutor));
        }
        if (!dialogs.containsKey(idDialog)) {
            throw new NonexistentEntitytException("Отсутствует диалог с id = " + String.valueOf(idDialog));
        }
    }

    /**
     * @param idDialog
     * @return null при ненахождении
     */
    public TDialog getDialogById(int idDialog) {
        return dialogs.get(idDialog);
    }

    /**
     * @param idUser
     * @return null при ненахождении
     */
    public TUser getUserById(int idUser) {
        return users.get(idUser);
    }

    /**
     * @param idMessage
     * @return null при ненахождении
     */
    public TMessage getMessageById(int idMessage) {
        return messages.get(idMessage);
    }

    /**
     * Добавить пользователя в диалог
     *
     * @param idDialog
     * @param idUser
     * @return
     * @throws NonexistentEntitytException при не корректных данных о диалоге или пользователе
     */
    public boolean addUserToDialog(int idDialog, int idUser) throws NonexistentEntitytException {
        checkArg(idDialog, idUser);
        return userLinkDialog.add(new Link(idUser, idDialog));
    }

    /**
     * @param login
     * @return null при ненахождении
     */
    public Integer getIdUserByLogin(String login) {
        return logins.get(login);
    }

    /**
     * @param login
     * @return null при ненахождении
     */
    public TUser getUserByLogin(String login) {
        Integer key = logins.get(login);
        if (key == null) {
            //если не нашли логин
            return null;
        }
        return users.get(key);
    }

    public UUID getIdStorage() {
        return idStorage;
    }

    /**
     * Список сообщений указанного диалога
     *
     * @param idDialog
     * @return
     * @throws NonexistentEntitytException не корректные данные о диалоге
     */
    public List<TMessage> getMessages(int idDialog) throws NonexistentEntitytException {
        if (!dialogs.containsKey(idDialog)) {
            throw new NonexistentEntitytException("Отсутствует диалог с id = " + String.valueOf(idDialog));
        }

        Set<TMessage> messages = new HashSet<>();
        for (Link link : dialogLinkMessage) {
            if (link.getKey1() == idDialog) {
                messages.add(getMessageById(link.getKey2()));
            }
        }
        List<TMessage> temp = new LinkedList<>(messages);
        Comparator<TMessage> comparator = new Comparator<TMessage>() {
            @Override
            public int compare(TMessage o1, TMessage o2) {
                return o1.getDateCreate().compareTo(o2.getDateCreate());
            }
        };
        temp.sort(comparator);
        return temp;
    }

    public Set<Link> getUserLinkDialog() {
        return Collections.unmodifiableSet(userLinkDialog);
    }

    public Map<Integer, TMessage> getMessages() {
        return messages;
    }
}



