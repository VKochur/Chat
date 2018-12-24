package kvv.net;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * То, что может быть сервером в схеме клиент-сервер при передаче данных между процессами по сети
 * Передача данных происходит посредством передачи пакетов Pack. Пакет содержит инфо об уникальном uuid, кода команды(int) и контекста команды.
 * Передача проходит по ObjectOutputStream(ObjectInputStream), поэтому элементы контекста команды в передаваемых пакетах должен быть Serializable
 * <p>
 * При реализации конкретного сервера необходимо:
 * 1. реализовать класс extends SocketDispatcher, в котором реализовать processingPack(ClientPack clientPack) определяющий логику обработки получаемых от клиентов пакетов
 * 2. реализовать метод getSocketDispatcherInstance(Socket socket, Serverable<T> server), в котором необходимо вернуть экземляр класса реализованного в п.1
 */
public interface Serverable<T extends SocketDispatcher> extends Logable {

    //простые геттеры и сеттеры

    Thread getThreadServer();

    void setThreadServer(Thread threadServer);

    Integer getPort();

    void setPort(Integer port);

    ServerSocket getServerSocket();

    void setServerSocket(ServerSocket serverSocket);

    List<T> getSocketDispatchersList();

    void setSocketDispatchersList(List<T> socketDispatchersList);

    /**
     * Возвращает в данном методе экземпляр класса ? extends SocketDispatcher,
     * где необходимо реализовать processingPack(ClientPack clientPack) исходя из задач сервера
     */
    T getSocketDispatcherInstance(Socket socket, Serverable<T> server);

    /**
     * Обработка исключительных ситуаций
     *
     * @param e
     */
    void processingException(Exception e);

    /**
     * Указывает порт, который сервер прослушивает
     *
     * @param port В случае если сервер запущен throws IllegalStateException
     */
    default void configServer(Integer port) {
        if (!isServerStarted()) {
            setPort(port);
        } else {
            throw new IllegalStateException("Сервер уже запущен. Изменение настроек не возможно");
        }
    }

    /**
     * Стартует сервер в новом потоке.
     * Запущенный сервер может принимать и отправлять пакеты по имеющимся подключениям, а также принимать подключения
     *
     * @throws IOException в случае, если порт занят
     *                     В случае если сервер уже запущен throws IllegalStateException
     */
    default void startServer() throws IOException {
        if (isServerStarted()) {
            throw new IllegalStateException("Сервер уже запущен.");
        }

        Serverable<T> server = this;

        if (getPort() == null){
            throw new IllegalStateException("Не указан порт");
        }

        setServerSocket(new ServerSocket(getPort()));
        setSocketDispatchersList(Collections.synchronizedList(new ArrayList<T>()));
        writeToLog("Попытка стартануть сервер. Порт: "+getPort());
        Thread threadServer = new Thread(new Runnable() {
            @Override
            public void run() {
                //обвертываем в try, чтобы в любом случае закрыть открытые подключения
                try {
                    writeToLog("Сервер стартанул и ожидает подключений..");
                    while (true) {
                        Socket socket;
                        try {
                            socket = getServerSocket().accept();
                        } catch (IOException e) {
                            processingException(new IOException("Исключение при соединении с сервером", e));
                            break;
                        }

                        //если не получен флаг закругляться
                        if (!Thread.interrupted()) {
                            T socketDispatcher = server.getSocketDispatcherInstance(socket, server);
                            server.getSocketDispatchersList().add(socketDispatcher);
                            writeToLog("Подключение к серверу: " + socketDispatcher + " : " + socket);
                            new Thread(socketDispatcher).start();
                        } else {
                            break;
                        }
                    }
                    writeToLog("Выход из цикла ожидания подключений");
                } finally {
                    closeSocketDispatchers();
                }
            }
        });
        setThreadServer(threadServer);
        getThreadServer().start();
    }

    /**
     * Останавливает сервер, закрывает подключения
     * <p>
     * В случае если сервер не запущен throws IllegalStateException
     */
    default void stop() {
        writeToLog("Попытка остановить сервер");
        if (isServerStarted()) {
            //проставим флаг, что потоку по прослушиванию подключений к серверу пора закругляться
            getThreadServer().interrupt();
            //и сделаем подключение к серверу, чтобы поток вышел из ожидания текущего подключения
            try {
                new Socket("127.0.0.1", getPort());
            } catch (IOException e) {
                processingException(e);
            }
            //ожидаем завершения потока работы сервера
            try {
                getThreadServer().join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            writeToLog("Сервер остановлен");
        } else {
            throw new IllegalStateException("Сервер не запущен");
        }
    }

    /**
     * Закрывает имеющиеся подключения
     */
    default void closeSocketDispatchers() {
        writeToLog("Закрываются подключения");
        Iterator<T> iterator = getSocketDispatchersList().iterator();
        while (iterator.hasNext()) {
            iterator.next().close();
        }

        //надо подождать пока все закроется
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        iterator = getSocketDispatchersList().iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        writeToLog("Закрыты подключения");
    }

    /**
     * Закрывает соединение
     *
     * @param socketDispatcher
     * @return true если удачно, false если нет указанного соединения
     */
    default boolean disconnect(T socketDispatcher) {
        Boolean result = getSocketDispatchersList().remove(socketDispatcher);
        if (result) {
            //если есть такое соединение
            writeToLog("Отключение от сервера: " + socketDispatcher + " выполнено");

        } else {
            writeToLog("Отключение от сервера: " + socketDispatcher + ": отсутствует такое соединение");
        }
        socketDispatcher.close();
        return result;
    }

    /**
     * Отправка пакета через указанного диспетчера соединения
     *
     * @param socketDispatcher соединение
     * @param serverPack
     */
    default void sendPack(T socketDispatcher, ServerPack serverPack) {
        socketDispatcher.sendPack(serverPack);
    }

    /**
     * Проверка состояния сервера
     *
     * @return true сервер находится в состоянии ожидания приема подключений и может принимать и отправлять пакеты
     * по имеющимся соединениям
     * false сервер не находится в состоянии ожидания подключений и приема/отправления пакетов
     */
    default boolean isServerStarted() {
        if (getThreadServer() != null) {
            return getThreadServer().isAlive();
        } else {
            return false;
        }
    }
}
