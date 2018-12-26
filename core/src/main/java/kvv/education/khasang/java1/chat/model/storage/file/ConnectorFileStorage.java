package kvv.education.khasang.java1.chat.model.storage.file;

import kvv.education.khasang.java1.chat.model.NonUniqueException;
import kvv.education.khasang.java1.chat.model.StorageConnector;
import kvv.education.khasang.java1.chat.model.basic_entity.Dialog;
import kvv.education.khasang.java1.chat.model.basic_entity.Message;
import kvv.education.khasang.java1.chat.model.crypt.DefaultEncoder;
import kvv.education.khasang.java1.chat.model.crypt.Encoder;
import kvv.education.khasang.java1.chat.model.NonexistentEntitytException;
import kvv.education.khasang.java1.chat.model.basic_entity.User;
import kvv.education.khasang.java1.chat.model.multithreading.KeysForSynchronizedConnectors;

import java.io.*;
import java.util.*;

/**
 * Коннектор к хранилищу, информация в котором хранится в файлах определенной структуры
 * структура каталога и файлов:
 * ..../pathWorking
 * ..../pathWorking/chatData/users
 * ..../pathWorking/chatData/dialogs
 * ..../pathWorking/chatData/links
 * ..../pathWorking/chatData/settings
 * ..../pathWorking/chatData/dialogsContent/1
 * ..../pathWorking/chatData/dialogsContent/2
 * <p>
 * Список пользователей чата users:
 * id
 * login
 * password
 * id
 * login
 * password
 * <p>
 * Список бесед чата dialogs:
 * id
 * nameDialog
 * id
 * nameDialog
 * <p>
 * Сопоставление участников чата по беседам links:
 * idUser-idDialog
 * idUser-idDialog
 * <p>
 * Общие сведения о хранилище setting: значения счетчиков id для пользователей, диалогов, сообщений. id хранилища
 * <p>
 * Содержание беседы с id = 2 :   ..../pathWorking/chatData/dialogsContent/2
 * id сообщения
 * text сообщения
 * idAutor автор сообщения
 * long (дата создания в милисекундах с 01.01.1970)
 * id
 * text
 * idAutor
 * long (дата создания)
 */
public class ConnectorFileStorage implements StorageConnector {
    //структура каталога чата, с которым работает коннектор
    private static final String PATH_DATA_USERS = "chatData/users";
    private static final String PATH_DATA_DIALOGS = "chatData/dialogs";
    private static final String PATH_DATA_LINKS_USERDIALOG = "chatData/links";
    private static final String PATH_DATA_DIALOGS_CONTENT = "chatData/dialogsContent/";
    private static final String PATH_SETTING_FILE = "chatData/settings";
    //тэги в файле настроек
    private static final String TAG_USER_ID = "USER_ID";
    private static final String TAG_DIALOG_ID = "DIALOG_ID";
    private static final String TAG_MESSAGE_ID = "MESSAGE_ID";
    private static final String TAG_ID_STORAGE = "STORAGE_ID";
    //разделитель в файле связей пользователь - диалог
    private static final char SEPARATOR_LINK = '-';

    private Encoder encoder;

    //каталог хранения данных чата, передается в конструкторе
    private String pathWorking;
    //---расчитываемые параметры по хранению данных чата-----
    //данные о пользователях
    private String pathUsersFile;
    //данные о диалогах
    private String pathDialogsFile;
    //данные о связях бесед и пользователей
    private String pathLinksFile;
    //каталог с файлами содержаний диалогов
    private String pathDialogsContent;
    //инфомация по счеткикам id пользователей и диалогов
    private String pathSettingFile;

    private UUID storageId;

    /**
     * Можно создать хранилище по указанному адресу
     *
     * @param workPath
     * @return true если по указанному адресу нет ни файла, ни каталога
     */
    public static boolean canCreateStorage(String workPath) {
        if (!new File(workPath).exists()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Коннектор к не существующему по workPath хранилищу. Хранилище будет создано с указанным пользователем и назначенным ему паролем
     *
     * @param pathWorking throw IllegalArgumentException если по указанному пути уже существует каталог или файл
     */
    public ConnectorFileStorage(String pathWorking, boolean needCreateNewStorage) throws IOException {
        this.pathWorking = pathWorking;
        definePaths();
        storageId = null;
        if (needCreateNewStorage) {
            createNewFileStorage(pathWorking);
        }
        //в множество ключей для синхронизации потоков добавим uuid хранилища
        KeysForSynchronizedConnectors.addKeyForSynchronized(this.getStorageId());
    }

    private void createNewFileStorage(String pathWorking) throws IOException {
        if (ConnectorFileStorage.canCreateStorage(pathWorking)) {
            //создание структуры папок в каталогах
            this.createFolders();
            //задание начальных настроек
            this.defineNewSettings();
        } else {
            throw new IllegalArgumentException("Некорректный адрес для создания нового хранилища. По пути уже существует файл или каталог: " + pathWorking);
        }
    }

    private UUID defineStoreId() throws FileNotFoundException {
        //если когда-то определили индентификатор хранилища, то ок
        if (storageId != null) {
            return storageId;
        }
        //иначе считаем его значение из файла общих настроек
        String value;
        try {
            value = FileUtil.pullValue(pathSettingFile, TAG_ID_STORAGE);
            try {
                storageId = UUID.fromString(value);
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Некорректный формат файла общих настроек. " + e.getMessage());
            }
        } catch (IOException e) {
            throw new FileNotFoundException("Неверная структура хранилища. Файл с общими настройками не найден " + e.getMessage());
        }
        return storageId;
    }

    @Override
    public UUID getStorageId() throws FileNotFoundException {
        return defineStoreId();
    }

    /**
     * Рассчитываем пути к данным чата, зная значение рабочего каталога
     */
    private void definePaths() {
        //добиваемся корректного вида рабочего каталога
        pathWorking += pathWorking.endsWith("/") ? "" : "/";
        //определяем пути к файлам с данными
        pathUsersFile = pathWorking + ConnectorFileStorage.PATH_DATA_USERS;
        pathDialogsFile = pathWorking + ConnectorFileStorage.PATH_DATA_DIALOGS;
        pathDialogsContent = pathWorking + ConnectorFileStorage.PATH_DATA_DIALOGS_CONTENT;
        pathSettingFile = pathWorking + ConnectorFileStorage.PATH_SETTING_FILE;
        pathLinksFile = pathWorking + ConnectorFileStorage.PATH_DATA_LINKS_USERDIALOG;
    }

    private void defineNewSettings() throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put(ConnectorFileStorage.TAG_ID_STORAGE, UUID.randomUUID().toString());
        map.put(ConnectorFileStorage.TAG_USER_ID, String.valueOf(0));
        map.put(ConnectorFileStorage.TAG_DIALOG_ID, String.valueOf(0));
        map.put(ConnectorFileStorage.TAG_MESSAGE_ID, String.valueOf(0));
        FileUtil.saveSettings(pathSettingFile, map);
    }

    private void createFolders() throws IOException {
        FileUtil.createFile(pathUsersFile);
        FileUtil.createFile(pathDialogsFile);
        FileUtil.createFile(pathLinksFile);
        FileUtil.createFile(pathSettingFile);
    }

    /**
     * Создание нового пользователя в хранилище
     *
     * @param login
     * @param password пароль на вход для нового пользователя
     * @return
     * @throws NonUniqueException пользователь с указанным логино уже есть
     */
    @Override
    public User createUser(String login, String password) throws IOException, NonUniqueException {
        synchronized (getKeyForSynchronized()) {
            if (this.getUserByLogin(login) != null) {
                throw new NonUniqueException("Пользователь с указаннам логином уже есть: " + login);
            }
            int idUser;
            try {
                idUser = defineCounterIdUser();
            } catch (FileNotFoundException e) {
                throw new FileNotFoundException("Некорректная структура хранилища. Отсутствует файл с общей информацией о хранилище. " + e.getMessage());
            }
            idUser++;
            incrementUserId(idUser);
            try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(this.pathUsersFile, true)))) {
                out.writeInt(idUser);
                out.writeUTF(login);

                if (encoder == null) {
                    out.writeUTF(new DefaultEncoder().encode(password));
                } else {
                    out.writeUTF(encoder.encode(password));
                }
            }
            User user = new User(idUser, defineStoreId(), login);
            return user;
        }
    }

    /**
     * Установить новое значения счетчика id для участников чата
     *
     * @param newValue
     * @throws IOException
     */
    private void incrementUserId(int newValue) throws IOException {
        synchronized (getKeyForSynchronized()) {
            FileUtil.saveValue(this.pathSettingFile, this.TAG_USER_ID, String.valueOf(newValue));
        }
    }

    /**
     * Определяет текущее значение счетчика id для участников чата
     *
     * @return
     * @throws FileNotFoundException
     */
    private int defineCounterIdUser() throws IOException {
        synchronized (getKeyForSynchronized()) {
            String value;
            try {
                value = FileUtil.pullValue(this.pathSettingFile, this.TAG_USER_ID);
            } catch (IOException e) {
                throw new IOException("Ошибка при считывании значения счетчика пользователей");
            }

            if (value != null) {
                try {
                    return Integer.valueOf(value);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Не удалось определить id пользователя. Неверный формат файла" + this.pathSettingFile);
                }
            } else {
                throw new IllegalArgumentException("Не удалось определить id для пользователя. Неверный формат файла" + this.pathSettingFile);
            }
        }
    }

    /**
     * Получить пользователя по логину
     *
     * @param login
     * @return null, если не найден
     */
    //   @Override
    public User getUserByLogin(String login) throws IOException {
        synchronized (getKeyForSynchronized()) {
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(this.pathUsersFile)))) {
                Integer idCurrent;
                String loginCurrent;
                while (in.available() > 0) {
                    idCurrent = in.readInt();
                    loginCurrent = in.readUTF();
                    //password
                    in.readUTF();
                    if (login.equals(loginCurrent)) {
                        User user = new User(idCurrent, defineStoreId(), login);
                        return user;
                    }
                }
            }
            return null;
        }
    }

    /**
     * Создает новый диалог с указанным названием
     *
     * @param dialogName
     * @return
     * @throws IOException
     */
    @Override
    public Dialog createDialog(String dialogName) throws IOException {
        synchronized (getKeyForSynchronized()) {
            int idDialog;
            try {
                idDialog = defineCounterIdDialog();
            } catch (FileNotFoundException e) {
                throw new FileNotFoundException("Некорректная структура хранилища. Отсутствует файл с общей информацией о хранилище. " + e.getMessage());
            }
            idDialog++;
            incrementDialogId(idDialog);
            String pathContext = definePathContext(idDialog);

            if (new File(pathContext).exists()) {
                throw new IllegalArgumentException("Создаваемый файл для содержания диалога уже существует " + pathContext);
            } else {
                FileUtil.createFile(pathContext);
            }

            try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(this.pathDialogsFile, true)))) {
                out.writeInt(idDialog);
                out.writeUTF(dialogName);
            }

            return new Dialog(idDialog, defineStoreId(), dialogName);
        }
    }

    /**
     * Устанавливает значение счетчика id для диалогов
     *
     * @param newValue
     * @throws IOException
     */
    private void incrementDialogId(int newValue) throws IOException {
        synchronized (getKeyForSynchronized()) {
            FileUtil.saveValue(this.pathSettingFile, this.TAG_DIALOG_ID, String.valueOf(newValue));
        }
    }

    /**
     * Определяет значение счетчиков id для диалогов
     *
     * @return
     * @throws FileNotFoundException
     */
    private int defineCounterIdDialog() throws IOException {
        synchronized (getKeyForSynchronized()) {
            String value;
            try {
                value = FileUtil.pullValue(this.pathSettingFile, this.TAG_DIALOG_ID);
            } catch (IOException e) {
                throw new IOException("Ошибка при считывании значения счетчика диалогов");
            }

            if (value != null) {
                try {
                    return Integer.valueOf(value);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Не удалось определить id диалога. Неверный формат файла" + this.pathSettingFile);
                }
            } else {
                throw new IllegalArgumentException("Не удалось определить id для диалога. Неверный формат файла" + this.pathSettingFile);
            }
        }
    }

    /**
     * Создает сообщение в хранилище
     *
     * @param text     текст сообщения
     * @param idDialog id диалог к которому относится сообщение
     * @param idAutor  id пользователя чата, написавшего сообщение
     * @return
     * @throws NonexistentEntitytException
     * @throws IOException
     */
    @Override
    public Message createMessage(String text, int idDialog, int idAutor) throws NonexistentEntitytException, IOException {
        synchronized (getKeyForSynchronized()) {
            String pathContext = definePathContext(idDialog);

            int idMessage;
            try {
                idMessage = defineCounterIdMessage();
            } catch (FileNotFoundException e) {
                throw new FileNotFoundException("Некорректная структура хранилища. Отсутствует файл с общей информацией о хранилище. " + e.getMessage());
            }
            idMessage++;
            incrementMessageId(idMessage);

            Calendar calendar = new GregorianCalendar();
            try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(pathContext, true)))) {
                out.writeInt(idMessage);
                out.writeUTF(text);
                out.writeInt(idAutor);
                out.writeLong(calendar.getTimeInMillis());
            }
            return new Message(idMessage, defineStoreId(), text, idAutor, calendar.getTime());
        }
    }

    /**
     * Устанавливает значение счетчика id для сообщений
     *
     * @param newValue
     * @throws IOException
     */
    private void incrementMessageId(int newValue) throws IOException {
        synchronized (getKeyForSynchronized()) {
            FileUtil.saveValue(this.pathSettingFile, this.TAG_MESSAGE_ID, String.valueOf(newValue));
        }
    }

    /**
     * Определяет значение счетчика id для сообщений
     *
     * @return
     * @throws FileNotFoundException
     */
    private int defineCounterIdMessage() throws IOException {
        synchronized (getKeyForSynchronized()) {
            String value;

            try {
                value = FileUtil.pullValue(this.pathSettingFile, this.TAG_MESSAGE_ID);
            } catch (IOException e) {
                throw new IOException("Ошибка при считывании значения счетчика сообщений");
            }

            if (value != null) {
                try {
                    return Integer.valueOf(value);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Не удалось определить id сообщений. Неверный формат файла" + this.pathSettingFile);
                }
            } else {
                throw new IllegalArgumentException("Не удалось определить id для сообщений. Неверный формат файла" + this.pathSettingFile);
            }

        }
    }

    /**
     * Определяет путь к файлу с содержанием диалога по id диалога
     *
     * @param idDialog
     * @return
     */
    private String definePathContext(int idDialog) {
        synchronized (getKeyForSynchronized()) {
            return pathDialogsContent + String.valueOf(idDialog);
        }
    }

    /**
     * @param idUser
     * @return null, если не нашел
     */
    @Override
    public User getUserById(int idUser) throws IOException {
        synchronized (getKeyForSynchronized()) {
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(this.pathUsersFile)))) {
                while (in.available() > 0) {
                    int idCurrent = in.readInt();
                    String loginCurrent = in.readUTF();
                    //password
                    in.readUTF();
                    if (idCurrent == idUser) {
                        return new User(idUser, defineStoreId(), loginCurrent);
                    }
                }
            }
            return null;
        }
    }

    /**
     * @param login
     * @return null, если не найдем
     * @throws IOException
     */
    @Override
    public Integer getIdUserByLogin(String login) throws IOException {
        synchronized (getKeyForSynchronized()) {
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(this.pathUsersFile)))) {
                while (in.available() > 0) {
                    int idCurrent = in.readInt();
                    String loginCurrent = in.readUTF();
                    //password
                    in.readUTF();
                    if (login.equals(loginCurrent)) {
                        return Integer.valueOf(idCurrent);
                    }
                }
            }
            return null;
        }
    }

    /**
     * @param login
     * @param password
     * @return null, если не нашли
     * @throws IOException
     */
    @Override
    public boolean checkIndentification(String login, String password) throws IOException {
        synchronized (getKeyForSynchronized()) {
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(this.pathUsersFile)))) {
                while (in.available() > 0) {
                    //id
                    in.readInt();
                    String loginCurrent = in.readUTF();
                    //password
                    String passwordValid = in.readUTF();
                    if (login.equals(loginCurrent)) {
                        String temp;
                        if (encoder == null) {
                            temp = new DefaultEncoder().encode(password);
                        } else {
                            temp = encoder.encode(password);
                        }
                        return passwordValid.equals(temp);
                    }
                }
            }
            return false;
        }
    }

    /**
     * @param idDialog
     * @return
     * @throws IOException
     */
    @Override
    public Dialog getDialogById(int idDialog) throws IOException {
        synchronized (getKeyForSynchronized()) {
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(this.pathDialogsFile)))) {
                while (in.available() > 0) {
                    int idCurrent = in.readInt();
                    String dialogName = in.readUTF();
                    if (idDialog == idCurrent) {
                        return new Dialog(idCurrent, defineStoreId(), dialogName);
                    }
                }
            }
            return null;
        }
    }

    /**
     * Добавляет в указанную беседу указанного участника
     *
     * @param idDialog
     * @param idUser
     * @return
     * @throws NonexistentEntitytException если указанного диалога или пользователя нет в хранилище
     * @throws IOException
     */
    @Override
    public boolean addUserToDialog(int idDialog, int idUser) throws NonexistentEntitytException, IOException {
        synchronized (getKeyForSynchronized()) {
            if (getUserById(idUser) == null) {
                throw new NonexistentEntitytException("Отсутсвует пользователь с id = " + idUser);
            }
            if (getDialogById(idDialog) == null) {
                throw new NonexistentEntitytException("Отсутсвует диалог с id = " + idDialog);
            }
            if (!existsLink(idUser, idDialog)) {
                try (PrintWriter wr = new PrintWriter(new BufferedWriter(new FileWriter(this.pathLinksFile, true)))) {
                    wr.println(String.format("%s%c%s", idUser, ConnectorFileStorage.SEPARATOR_LINK, idDialog));
                    return true;
                }
            } else {
                return false;
            }
        }
    }

    private boolean existsLink(int idUser, int idDialog) throws FileNotFoundException {
        synchronized (getKeyForSynchronized()) {
            try (Scanner scanner = new Scanner(new BufferedInputStream(new FileInputStream(this.pathLinksFile)))) {
                while (scanner.hasNext()) {
                    String temp = scanner.nextLine();
                    int index = temp.indexOf(ConnectorFileStorage.SEPARATOR_LINK);
                    int idU = Integer.valueOf(temp.substring(0, index));
                    int idD = Integer.valueOf(temp.substring(index + 1, temp.length()));
                    if ((idUser == idU) && (idD == idDialog)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    /**
     * Множество участников указанного диалога
     *
     * @param idDialog
     * @return
     * @throws NonexistentEntitytException если указанного диалога нет в хранилище
     * @throws IOException
     * @throws NonexistentEntitytException в случае отсутствия указанного диалога
     */
    @Override
    public Set<User> getUserByDialog(int idDialog) throws IOException, NonexistentEntitytException {
        synchronized (getKeyForSynchronized()) {
            if (this.getDialogById(idDialog) == null) {
                throw new NonexistentEntitytException("Отсутствует диалог с id = " + String.valueOf(idDialog));
            }
            Set<Integer> idUsers = new HashSet<>();
            try (Scanner scanner = new Scanner(new FileInputStream(this.pathLinksFile))) {
                while (scanner.hasNext()) {
                    String temp = scanner.nextLine();
                    int index = temp.indexOf(ConnectorFileStorage.SEPARATOR_LINK);
                    if (Integer.valueOf(temp.substring(index + 1, temp.length())).equals(Integer.valueOf(idDialog))) {
                        idUsers.add(Integer.valueOf(temp.substring(0, index)));
                    }
                }
            }
            Set<User> users = new HashSet<>();
            for (Integer idUser : idUsers) {
                users.add(getUserById(idUser));
            }
            return users;
        }
    }

    /**
     * Множество бесед, в которых участвует пользователь
     *
     * @param idUser
     * @return
     * @throws NonexistentEntitytException если указанного пользователя нет в хранилище
     * @throws IOException
     */
    @Override
    public Set<Dialog> getDialogByUsers(int idUser) throws NonexistentEntitytException, IOException {
        synchronized (getKeyForSynchronized()) {
            if (this.getUserById(idUser) == null) {
                throw new NonexistentEntitytException("Отсутствует пользователь с id = " + String.valueOf(idUser));
            }
            Set<Integer> idDialogs = new HashSet<>();
            try (Scanner scanner = new Scanner(new FileInputStream(this.pathLinksFile))) {
                while (scanner.hasNext()) {
                    String temp = scanner.nextLine();
                    int index = temp.indexOf(ConnectorFileStorage.SEPARATOR_LINK);
                    if (Integer.valueOf(temp.substring(0, index)).equals(Integer.valueOf(idUser))) {
                        idDialogs.add(Integer.valueOf(temp.substring(index + 1, temp.length())));
                    }
                }
            }
            Set<Dialog> dialogs = new HashSet<>();
            for (Integer idDialog : idDialogs) {
                dialogs.add(getDialogById(idDialog));
            }
            return dialogs;
        }
    }

    /**
     * Список сообщений в диалоге
     *
     * @param idDialog
     * @return
     * @throws NonexistentEntitytException нет указанного диалога в хранилище
     * @throws IOException
     */
    @Override
    public List<Message> getMessages(Integer idDialog) throws IOException, NonexistentEntitytException {
        synchronized (getKeyForSynchronized()) {
            if (this.getDialogById(idDialog) == null) {
                throw new NonexistentEntitytException("Отсутствует диалог с id = " + String.valueOf(idDialog));
            }
            List<Message> messages = new LinkedList<>();
            String pathDialog = definePathContext(idDialog);
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(pathDialog)))) {
                while (in.available() > 0) {
                    int id = in.readInt();
                    String text = in.readUTF();
                    int idAutor = in.readInt();
                    long t = in.readLong();
                    Message message = new Message(id, defineStoreId(), text, idAutor, new Date(t));
                    messages.add(message);
                }
            }
            return messages;
        }
    }

    /**
     * Множество всех участвников чата
     *
     * @return
     * @throws IOException
     */
    @Override
    public Set<User> pullUsers() throws IOException {
        synchronized (getKeyForSynchronized()) {
            Set<User> users = new HashSet<>();
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(this.pathUsersFile)))) {
                while (in.available() > 0) {
                    int idCurrent = in.readInt();
                    String loginCurrent = in.readUTF();
                    //password
                    in.readUTF();
                    users.add(new User(idCurrent, defineStoreId(), loginCurrent));
                }
            }
            return users;
        }
    }

    /**
     * Множество всех бесед чата
     *
     * @return
     * @throws IOException
     */
    @Override
    public Set<Dialog> pullDialogs() throws IOException {
        synchronized (getKeyForSynchronized()) {
            Set<Dialog> dialogs = new HashSet<>();
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(this.pathDialogsFile)))) {
                while (in.available() > 0) {
                    int idDialog = in.readInt();
                    String name = in.readUTF();
                    dialogs.add(new Dialog(idDialog, defineStoreId(), name));
                }
            }
            return dialogs;
        }
    }

    @Override
    public Encoder getEncoder() {
        return encoder;
    }

    @Override
    public void setEncoder(Encoder encoder) {
        this.encoder = encoder;
    }

}
