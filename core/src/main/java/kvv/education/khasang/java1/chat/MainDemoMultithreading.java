package kvv.education.khasang.java1.chat;

import kvv.education.khasang.java1.chat.model.ModelChat;
import kvv.education.khasang.java1.chat.model.NonUniqueException;
import kvv.education.khasang.java1.chat.model.NonexistentEntitytException;
import kvv.education.khasang.java1.chat.model.storage.omd.ConnectorOMDStorage;
import kvv.education.khasang.java1.chat.model.storage.omd.StorageOMD;
import kvv.education.khasang.java1.chat.model.storage.omd.TMessage;
import kvv.education.khasang.java1.chat.model.basic_entity.Dialog;
import kvv.education.khasang.java1.chat.model.basic_entity.Message;
import kvv.education.khasang.java1.chat.model.basic_entity.User;
import kvv.education.khasang.java1.chat.model.multithreading.Util;
import kvv.education.khasang.java1.chat.views.console.ConsoleControllerChat;
import kvv.education.khasang.java1.chat.views.console.ConsoleViewChat;
import kvv.education.khasang.java1.chat.views.gui.WindowControllerChat;
import kvv.education.khasang.java1.chat.views.gui.WindowViewChat;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Демонстрация наличия синхронизации методов коннекторов.
 * Методы коннекторов синхронизированы для многопоточности по ключю отражающему определенное хранилище (UUID хранилища)
 * В один момент времени действия над определенным хранилищем посредством коннектора может выполнять только один поток,
 * вне зависимости от того сколько экземпляров определенного класса (реализующего интерфейс "коннектор") инстанцировано для работы с указанным определенным хранилищем,
 * и являются ли инстанцированные коннекторы экземплярами одного класса (в общем случае для работы с определенным типом хранилища могут быть разработаны разные классы коннекторов)
 * <p>
 * При этом работа потоков над одним хранилищем не зависит от работы потоков над другим хранилищем, вне зависимости от одинаковости типов хранилищ,
 * одинаковости класса которому принадлежат экземпляры коннекторов к хранилищам.
 * <p>
 * Для демонстрации синхронизации создается два хранилища StorageOMD
 * в каждом хранилище по 2 пользователя: user1, user2, пароли пользователей пустые = ""
 * <p>
 * В разных 5 потоках запускаются 5 чатов к этим 2м хранилищам:
 * 1. Два чата с gui к StorageOMD1
 * 2. Два чата с gui к StorageOMD2 + один консольный чат (main поток) к StorageOMD
 * <p>
 * Модели чатов используют коннектор SlowConnectorOMDStorage extends СonnectorOMDStorage
 * в котором переопределен createMessage. В данном методе вставлена функция засыпания потока.
 * <p>
 * Отправка сообщения в любом из чатов (консольного или gui) оказывает приостановку потока действий с хранилищем* из любого чата (консольного или gui) связанного с хранилищем*,
 * но не блокирует действия чатов связанных с другим хранилищем.
 * <p>
 * При оценке наличия приостановки потока действий следует учесть:
 * при выбранном в чате диалоге генерятся события прослушивающие наличие новых сообщений текущей беседы.
 * Метод определения новых сообщений синхронизирован и обработка события опроса новых сообщений также ждет очереди в потоках одновременно занимая место в контролере,
 * т.е. контролер может не принимать новых событий от вьюхи по причине наличия на обработке события прослушивания новых сообщений.
 * Если в одном чате отправить сообщение в какой-нить диалог, и в другом чате отправить сообщение в диалог, то по одному отправленному сообщению поток уснет внутри synchronized
 * в момент времени между отправками сообщений сгенерится событие определения новых сообщений в вьюхе, передастся в контролер,
 * а там и моделе в коннектор где перед synhronized будет ждать своей очереди.
 * Отправка второго сообщения во вьхе сгенерит событие, но контролер его не примет, т.к. на обработке событие обновления списка сообщений находится.
 * В итоге это выгдялит как отправление первого сообщения с задержкой (поток спит), и не отправление второго (контролер не принял сообщение об отправке сообщения, место было занято
 * событием получения обновленного списка сообщений.)
 * <p>
 * Приостановка потока заметна, когда один чат отправил сообщения, а в другом не был выбран диалог (т.е. не генерятся события прослушивания обновленных сообщений, не "забивая" контролер)
 * и происходит выбор диалога или текущего пользователя.
 */
public class MainDemoMultithreading {

    private static WindowViewChat windowViewChat;
    private static int x = 100;
    private static int y = 100;

    public static void main(String[] args) throws IOException {
        method();
    }

    private static void method() {
        StorageOMD storageOMD1 = createExampleStorage();
        StorageOMD storageOMD2 = createExampleStorage();
        //запустим два чата с GUI использующие одно хранилище в разных потоках
        startGuiChatsInDifferentThreads(storageOMD1, 2);
        //запустим два чата с GUI использующие другое одно хранилище в разных потоках
        startGuiChatsInDifferentThreads(storageOMD2, 2);

        //и еще запустим консольный чат для одного из хранилищ
        ConsoleControllerChat consoleChat = new ConsoleControllerChat();
        //модель создаем на основе задумчивого коннектора, чтобы можно было оценить наличие синхронизации
        ModelChat modelChat = new ModelChat();
        SlowConnectorOMDStorage slowConnectorOMDStorage = new SlowConnectorOMDStorage(storageOMD2);
        modelChat.setStorageConnector(slowConnectorOMDStorage);
        consoleChat.setModel(modelChat);
        consoleChat.setView(new ConsoleViewChat() {
        });
        System.out.println("Используемое в консольном чате хранилище: " + storageOMD2.getIdStorage());
        consoleChat.interactiveWork();
    }

    private static StorageOMD createExampleStorage() {
        StorageOMD storageOMD = new StorageOMD();
        ConnectorOMDStorage connectorOMDStorage = new ConnectorOMDStorage(storageOMD);
        try {
            User user1 = connectorOMDStorage.createUser("user1", "");
            User user2 = connectorOMDStorage.createUser("user2", "");
            Dialog dialog = connectorOMDStorage.createDialog("Dialog");
            connectorOMDStorage.addUserToDialog(dialog.getId(), user1.getId());
            connectorOMDStorage.addUserToDialog(dialog.getId(), user2.getId());

        } catch (NonexistentEntitytException e) {
            e.printStackTrace();

        } catch (NonUniqueException e) {
            e.printStackTrace();
        }
        return storageOMD;
    }

    /**
     * Запускает чаты c GUI, каждый в своем потоке.
     * <p>
     * Все создаваемые чаты используют одно хранилище
     * Количество создаваемых чатов равно количеству потоков countThreads
     * <p>
     * Для каждого чата создается гуи, свой экземпляр коннектора к хранилищу (модель), гуи с моделью объединяются посредством контролера (являющегося презентером) и
     * контроллер стартует и существляет действия с хранилищем в своем новом потоке.
     *
     * @param storageOMD
     * @param countThreads
     */
    private static void startGuiChatsInDifferentThreads(StorageOMD storageOMD, int countThreads) {

        for (int i = 0; i < countThreads; i++) {

            //запускаем вьюху
            try {
                runView();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ModelChat modelChat = null;
            //модель создаем на основе задумчивого коннектора, чтобы можно было оценить наличие синхронизации
            SlowConnectorOMDStorage slowConnectorOMDStorage = new SlowConnectorOMDStorage(storageOMD);
            modelChat = new ModelChat();
            modelChat.setStorageConnector(slowConnectorOMDStorage);

            WindowControllerChat windowControllerChat = new WindowControllerChat();
            windowControllerChat.setViewChat(windowViewChat);
            windowControllerChat.setModelChat(modelChat);

            Thread thread = Util.startGuiChatInNewThread(windowControllerChat);
            windowViewChat.setTitleSuffix(windowControllerChat.getIdStorage().toString() + " " + thread.currentThread().getName());
        }
    }

    private static void runView() throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                windowViewChat = new WindowViewChat();
                windowViewChat.setLocation(x += 25, y += 40);
            }
        });
    }

    /**
     * Медленный коннектор для демонстрации синхронизации
     */
    private static class SlowConnectorOMDStorage extends ConnectorOMDStorage {

        private static int TIME_PAUSE = 5000;

        public SlowConnectorOMDStorage(StorageOMD storage) {
            super(storage);
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
                pause(TIME_PAUSE);
                int idMessage = storage.createMessage(text, idDialog, idAutor);
                TMessage tMessage = storage.getMessageById(idMessage);
                return new Message(idMessage, storage.getIdStorage(), text, idAutor, tMessage.getDateCreate());
            }
        }

        private void pause(int millsec) {
            try {
                Thread.sleep(millsec);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
