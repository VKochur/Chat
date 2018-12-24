package kvv.education.khasang.java1.chat.model.storage.omd;

import kvv.education.khasang.java1.chat.model.basic_entity.Dialog;
import kvv.education.khasang.java1.chat.model.NonUniqueException;
import kvv.education.khasang.java1.chat.model.NonexistentEntitytException;
import kvv.education.khasang.java1.chat.model.StorageConnector;
import kvv.education.khasang.java1.chat.model.basic_entity.Message;
import kvv.education.khasang.java1.chat.model.basic_entity.User;
import kvv.education.khasang.java1.chat.model.crypt.DefaultEncoder;
import kvv.education.khasang.java1.chat.model.crypt.Encoder;
import kvv.education.khasang.java1.chat.model.multithreading.KeysForSynchronizedConnectors;

import java.util.*;

public class ConnectorOMDStorage implements StorageConnector {

    //шифратор паролей. если не указан, используется умолчательный
    protected Encoder encoder;

    protected StorageOMD storage;

    public ConnectorOMDStorage(StorageOMD storage) {
        this.storage = storage;
        //во множество ключей для синхронизации потоков добавим uuid хранилища
        KeysForSynchronizedConnectors.addKeyForSynchronized(this.getStorageId());
    }

    public StorageOMD getStorage() {
        return storage;
    }

    @Override
    public Set<Dialog> pullDialogs() {
        synchronized (getKeyForSynchronized()) {
            Map<Integer, TDialog> dialogs = storage.getDialogs();
            UUID idStorage = storage.getIdStorage();

            Set<Dialog> temp = new TreeSet<>();

            Set<Integer> keys = dialogs.keySet();
            for (Integer key : keys) {
                TDialog tdialog = dialogs.get(key);
                temp.add(new Dialog(key, idStorage, tdialog.getName()));
            }

            return temp;
        }
    }

    @Override
    public Set<User> pullUsers() {
        synchronized (getKeyForSynchronized()) {
            Map<Integer, TUser> users = storage.getUsers();
            UUID idStorage = storage.getIdStorage();

            Set<User> temp = new TreeSet<>();

            Set<Integer> keys = users.keySet();
            for (Integer key : keys) {
                TUser tuser = users.get(key);
                temp.add(new User(key, idStorage, tuser.getLogin()));
            }

            return temp;
        }
    }

    @Override
    public Dialog createDialog(String dialogName) {
        synchronized (getKeyForSynchronized()) {
            int key = storage.createDialog(dialogName);
            return new Dialog(key, storage.getIdStorage(), dialogName);
        }
    }

    /**
     * @param login
     * @param password пароль на вход для нового пользователя
     * @return
     * @throws NonUniqueException уже есть пользователь с таким логином
     */
    @Override
    public User createUser(String login, String password) throws NonUniqueException {
        synchronized (getKeyForSynchronized()) {
            Integer key;
            if (encoder == null) {
                key = storage.createUser(login, new DefaultEncoder().encode(password));
            } else {
                key = storage.createUser(login, encoder.encode(password));
            }
            return new User(key, storage.getIdStorage(), login);
        }
    }

    @Override
    public boolean checkIndentification(String login, String password) {
        synchronized (getKeyForSynchronized()) {
            if (encoder == null) {
                return storage.checkIndentification(login, new DefaultEncoder().encode(password));
            } else {
                return storage.checkIndentification(login, encoder.encode(password));
            }
        }
    }

    /**
     * @param text     текст сообщения
     * @param idDialog id диалог к которому относится сообщение
     * @param idAutor  id пользователя чата, написавшего сообщение
     * @return
     * @throws NonexistentEntitytException некорректные данные о диалоге или авторе
     */
    @Override
    public Message createMessage(String text, int idDialog, int idAutor) throws NonexistentEntitytException {
        synchronized (getKeyForSynchronized()) {
            int idMessage = storage.createMessage(text, idDialog, idAutor);
            TMessage tMessage = storage.getMessageById(idMessage);
            return new Message(idMessage, storage.getIdStorage(), text, idAutor, tMessage.getDateCreate());
        }
    }

    @Override
    public boolean addUserToDialog(int idDialog, int idUser) throws NonexistentEntitytException {
        synchronized (getKeyForSynchronized()) {
            return storage.addUserToDialog(idDialog, idUser);
        }
    }

    /**
     * @param idDialog
     * @return может вернуть null в случае ненахождения
     */
    @Override
    public Dialog getDialogById(int idDialog) {
        synchronized (getKeyForSynchronized()) {
            TDialog tDialog = storage.getDialogById(idDialog);
            if (tDialog != null) {
                return new Dialog(idDialog, storage.getIdStorage(), tDialog.getName());
            } else {
                return null;
            }
        }
    }

    /**
     * @param idUser
     * @return может вернуть null в случае ненахождения
     */
    @Override
    public User getUserById(int idUser) {
        synchronized (getKeyForSynchronized()) {
            TUser tUser = storage.getUserById(idUser);
            if (tUser != null) {
                return new User(idUser, storage.getIdStorage(), tUser.getLogin());
            } else {
                return null;
            }
        }
    }

    /**
     * @param login
     * @return может вернуть null в случае не нахождения
     */
    @Override
    public Integer getIdUserByLogin(String login) {
        synchronized (getKeyForSynchronized()) {
            return storage.getIdUserByLogin(login);
        }
    }

    /**
     * @param login
     * @return может вернуть  null в случае не нахождения
     */
    @Override
    public User getUserByLogin(String login) {
        synchronized (getKeyForSynchronized()) {
            TUser tUser = storage.getUserByLogin(login);
            Integer key = getIdUserByLogin(login);
            if (tUser != null) {
                return new User(key, storage.getIdStorage(), tUser.getLogin());
            } else {
                return null;
            }
        }
    }

    /**
     * Список сообщений в определенном каталоге
     *
     * @param idDialog
     * @return
     * @throws NonexistentEntitytException не корректный диалог
     */
    @Override
    public List<Message> getMessages(Integer idDialog) throws NonexistentEntitytException {
        synchronized (getKeyForSynchronized()) {
            UUID idStorage = storage.getIdStorage();
            List<TMessage> tMessages = storage.getMessages(idDialog);
            Set<Map.Entry<Integer, TMessage>> entry = storage.getMessages().entrySet();
            List<Message> temp = new LinkedList<>();
            //перебираем сообщения в хранилище
            for (TMessage tMessage : tMessages) {
                //определяем id для текущего
                Integer key = null;
                for (Map.Entry<Integer, TMessage> integerTMessageEntry : entry) {
                    if (integerTMessageEntry.getValue() == tMessage) {
                        key = integerTMessageEntry.getKey();
                        break;
                    }
                }

                if (key != null) {
                    //добавляем в список отображение текущего сообщения
                    temp.add(new Message(key, idStorage, tMessage.getText(), tMessage.getIdAutor(), tMessage.getDateCreate()));
                } else {
                    //такого случиться не должно
                    throw new IllegalStateException("При создании списка сообщений диалога произошло невероятное: для " + tMessage.toString() + " не нашлось id");
                }
            }
            return temp;
        }
    }


    @Override
    public UUID getStorageId() {
        return storage.getIdStorage();
    }

    /**
     * Множество участников беседы
     *
     * @param idDialog
     * @return
     * @throws NonexistentEntitytException
     */
    @Override
    public Set<User> getUserByDialog(int idDialog) throws NonexistentEntitytException {
        synchronized (getKeyForSynchronized()) {
            if (this.getDialogById(idDialog) == null) {
                throw new NonexistentEntitytException("Отсутствует диалог с id = " + String.valueOf(idDialog));
            }
            Set<User> users = new TreeSet<>();
            Set<Link> links = storage.getUserLinkDialog();
            for (Link link : links) {
                if (link.getKey2() == idDialog) {
                    User user = this.getUserById(link.getKey1());
                    users.add(user);
                }
            }
            return users;
        }
    }

    /**
     * Множество диалогов, в которых пользователь участвует
     *
     * @param idUser
     * @return
     * @throws NonexistentEntitytException
     */
    @Override
    public Set<Dialog> getDialogByUsers(int idUser) throws NonexistentEntitytException {
        synchronized (getKeyForSynchronized()) {
            if (this.getUserById(idUser) == null) {
                throw new NonexistentEntitytException("Отсутствует пользователь с id = " + String.valueOf(idUser));
            }
            Set<Dialog> dialogs = new TreeSet<>();
            Set<Link> links = storage.getUserLinkDialog();
            for (Link link : links) {
                if (link.getKey1() == idUser) {
                    Dialog dialog = this.getDialogById(link.getKey2());
                    dialogs.add(dialog);
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
