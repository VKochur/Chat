package kvv.net;

import java.io.*;
import java.net.Socket;

/**
 * То, что может быть клиентом в схеме клиент-сервер при передаче данных между процессами по сети
 * Передача данных происходит посредством передачи пакетов Pack. Пакет содержит инфо об уникальном uuid, кода команды(int) и контекста команды.
 * Передача проходит по ObjectOutputStream(ObjectInputStream), поэтому элементы контекста команды в передаваемых пакетах должен быть Serializable
 *
 * При реализации следует переопределить метод processingPack(ServerPack serverPack) соответственно логике обработки пакетов получаемых с сервера
 */
public interface Clientable extends Closeable, Logable {

    //простые геттеры и сеттеры

    Socket getSocket();

    void setSocket(Socket socket);

    ObjectInputStream getObjectInputStream();

    void setObjectInputStream(ObjectInputStream objectInputStream);

    ObjectOutputStream getObjectOutputStream();

    void setObjectOutputStream(ObjectOutputStream objectOutputStream);

    String getIpServer();

    void setIpServer(String ipServer);

    Integer getPort();

    void setPort(Integer port);

    Thread getThreadListener();

    void setThreadListener(Thread threadListener);


    /**
     * Обработчик полученных пакетов от сервера
     * @param serverPack
     */
    void processingPack(ServerPack serverPack);

    /**
     * Обработчик исключений
     * @param e
     */
     void processingException(Exception e);


    /**
     * Конфигурирует соединение
     * @param ipServer
     * @param port
     *
     * Если клиент находится в состоянии подключения throws IllegalStateException
     */
    default void configConnection(String ipServer, Integer port){
        if (isClientStarted()){
            throw new IllegalStateException("Клиент запущен");
        }
        setIpServer(ipServer);
        setPort(port);
    }

    /**
     * Устанавливает соединение
     * Клиент при установленном соединении может принимать и отправлять пакеты
     *
     * @throws IOException при невозможности установить соединение
     *
     * Если клиент находится в состоянии подключения throws IllegalStateException
     */
    default void startConnect() throws IOException {
        if (isClientStarted()){
            throw new IllegalStateException("Соединение уже установлено");
        }
        setSocket(new Socket(getIpServer(), getPort())); //может throws IOException
        writeToLog(": cоединение с сервером установлено: " + getSocket().toString());
        try {
            setObjectOutputStream(new ObjectOutputStream(new BufferedOutputStream(getSocket().getOutputStream())));
            getObjectOutputStream().flush();
            writeToLog("выходной поток инициализирован");
        } catch (IOException e) {
            close();
            processingException(e);
        }
        startListenerMessage();
    }

    /**
     * Закрывает соединение
     * Если клиент находится не в состоянии подключения throws IllegalStateException
     */
    default void stopConnect(){
      if (isClientStarted()){
          close();
      } else{
          throw new IllegalStateException("Клиент не запущен");
      }
    }

    /**
     * Определяет находится ли клиент в состоянии соединения
     * @return true  находится в состоянии соединения и может отправлять/принимать пакеты
     * false не в состоянии соединения
     */
    default boolean isClientStarted(){
        if (getThreadListener() != null){
            return getThreadListener().isAlive();
        }else{
            return false;
        }
    }

    /**
     * Отправляет пакет серверу
     * @param clientPack
     * @return успешно ли отправлено
     * В случае невозможности отправления закрывается соединение
     */
    default boolean sendPack(ClientPack clientPack) {
        try {
            getObjectOutputStream().writeObject(clientPack);
            getObjectOutputStream().flush();
            writeToLog("отправлен " + clientPack);
            return true;
        } catch (IOException e) {
            close();
            processingException(e);
            return false;
        }
    }

    /**
     * Стартует новый поток для прослушивания входящих пакетов
     */
    default void startListenerMessage() {
        Thread threadListener = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    setObjectInputStream(new ObjectInputStream(new BufferedInputStream(getSocket().getInputStream())));
                    writeToLog("входной поток инициализирован");
                } catch (IOException e) {
                    processingException(e);
                }
                while (true) {
                    try {
                        ServerPack serverPack = (ServerPack) getObjectInputStream().readObject();
                        writeToLog("получен " + serverPack);
                        processingPack(serverPack);
                    } catch (ClassNotFoundException e) {
                        processingException(e);
                    } catch (IOException e) {
                        //здесь выход из цикла, исключение не обрабатываем, т.к. вероятно отвалился сервер
                        break;
                    }
                }
               close();
            }
        });
        setThreadListener(threadListener);
        getThreadListener().start();
    }

    /**
     * Закрывает соединение.
     * В отличии от stopConnect не тестирует наличие соединения. Если соединение есть, оно будет закрыто.
     * Если нет, ничего не будет сделано
     */
    @Override
    default void close() {
        writeToLog("Закрытие соединения");
       /* стримы закрываются при закрытии сокета
        */
        if (getSocket() != null) {
            try {
                if (!getSocket().isClosed()) {
                    getSocket().close();
                    writeToLog("Закрыто соединения");
                } else {
                    writeToLog("Соединение уже закрыто");
                }
            } catch (IOException e) {
                processingException(e);
            }
        }
    }
}
