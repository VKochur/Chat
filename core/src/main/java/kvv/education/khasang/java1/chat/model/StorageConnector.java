package kvv.education.khasang.java1.chat.model;

import kvv.education.khasang.java1.chat.model.basic_entity.Dialog;
import kvv.education.khasang.java1.chat.model.basic_entity.Message;
import kvv.education.khasang.java1.chat.model.basic_entity.User;
import kvv.education.khasang.java1.chat.model.crypt.Encoder;
import kvv.education.khasang.java1.chat.model.multithreading.KeysForSynchronizedConnectors;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Интерфейс по работе с источником данных (хранилищем)
 * <p>
 * Требования к хранилищу данных:
 * Хранилище должно иметь уникальный UUID индентификатор
 * Хранилище должно содержать информацию о множестве пользователей, множестве диалогов, множестве сообщений, их связях между собой
 * В хранилище должна быть информация достаточная для проверки корректности введенных паролей
 * Хранимая информация должна удовлетворять условиям:
 * - уникальность id для каждого пользователя
 * - уникальность id для каждого диалога
 * - уникальность id для каждого сообщения
 * - уникальность login для каждого пользователя
 * <p>
 * Классы реализующие данный интерфейс для синхронизации доступа к хранилищу должны в конструктор добавлять
 * KeysForSynchronizedConnectors.addKeyForSynchronized(this.getStorageId());
 * и содержание методов окружать  synchronized (getKeyForSynchronized()) {
 * } (кроме  UUID getStorageId())
 */
public interface StorageConnector {

    /**
     * Получить все диалоги из хранилища
     *
     * @return
     * @throws IOException
     */
    Set<Dialog> pullDialogs() throws IOException;

    /**
     * Получить всех пользователей из хранилища
     *
     * @return
     * @throws IOException
     */
    Set<User> pullUsers() throws IOException;

    /**
     * Создание нового диалога в хранилище
     *
     * @param dialogName
     * @return созданный диалог
     * @throws IOException
     */
    Dialog createDialog(String dialogName) throws IOException;

    /**
     * Создание нового пользователя в хранилище
     *
     * @param login
     * @param password пароль на вход для нового пользователя
     * @return
     * @throws NonUniqueException уже есть пользователь с таким логином
     */
    User createUser(String login, String password) throws NonUniqueException, IOException;

    /**
     * Проверка индентификации пользователя
     *
     * @param login
     * @param password
     * @return
     */
    boolean checkIndentification(String login, String password) throws IOException;

    /**
     * Создание в хранилище нового сообщения
     *
     * @param text     текст сообщения
     * @param idDialog id диалог к которому относится сообщение
     * @param idAutor  id пользователя чата, написавшего сообщение
     * @return
     * @throws IOException
     */
    Message createMessage(String text, int idDialog, int idAutor) throws NonexistentEntitytException, IOException;

    /**
     * Добавление в диалог участника беседы
     *
     * @param idDialog
     * @param idUser
     */
    boolean addUserToDialog(int idDialog, int idUser) throws NonexistentEntitytException, IOException;

    /**
     * Получить диалог из хранилища по id
     *
     * @param idDialog
     * @return null в случае ненахождения
     * @throws IOException
     */
    Dialog getDialogById(int idDialog) throws IOException;

    /**
     * Получить пользователя по id
     *
     * @param idUser
     * @return null в случае ненахождения
     * @throws IOException
     */
    User getUserById(int idUser) throws IOException;

    /**
     * Получение сообщения по id
     * @param idMessage
     * @return null в случае не нахождения
     * @throws Exception
     */
    //Message getMessageById(int idMessage);

    /**
     * Получить id пользователя по логину
     *
     * @param login
     * @return null в случае не нахождения
     */
    Integer getIdUserByLogin(String login) throws IOException;

    /**
     * Получить пользователя по логину
     *
     * @param login
     * @return null, если не найден
     */
    User getUserByLogin(String login) throws IOException;

    //получает из хранилища список сообщений в указанном каталоге в порядке их создания в хранилище
    List<Message> getMessages(Integer idDialog) throws NonexistentEntitytException, IOException;

    //получает индентификатор хранилища
    UUID getStorageId() throws FileNotFoundException;

    /**
     * Множество участников указанного диалога
     *
     * @param idDialog
     * @return null в случае ненахождения диалога
     */
    Set<User> getUserByDialog(int idDialog) throws NonexistentEntitytException, IOException;

    /**
     * Множество диалогов в которых принимает участие пользователь
     *
     * @param idUser
     * @return
     */
    Set<Dialog> getDialogByUsers(int idUser) throws NonexistentEntitytException, IOException;

    /**
     * Кодировщик паролей
     *
     * @return
     */
    Encoder getEncoder();

    void setEncoder(Encoder encoder);

    default UUID getKeyForSynchronized() {
        return KeysForSynchronizedConnectors.getKeyForSynchronized(this);
    }
}
