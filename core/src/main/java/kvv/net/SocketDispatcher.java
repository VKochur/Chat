package kvv.net;

import kvv.education.khasang.java1.chat.model.basic_entity.User;
import kvv.education.khasang.java1.chat.views.gui.UserStatus;

import java.io.*;
import java.net.Socket;

/**
 * Диспетчер обработки соединения определенного клиента с сервером
 */
public abstract class SocketDispatcher implements Runnable, Closeable {
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private Serverable server;


    public SocketDispatcher(Socket socket, Serverable serverChat) {
        this.socket = socket;
        this.server = serverChat;
    }

    /**
     * Метод в котором должна быть определена логика отработки получаемых от клиента пакетов
     *
     * @param clientPack
     */
    public abstract void processingPack(ClientPack clientPack);

    public Serverable getServer() {
        return server;
    }

    /**
     * Метод запускаемый в новом потоке.
     * Открывает входящий и выходящий stream.
     * Организует прослушивание входящего stream в новом потоке.
     */
    @Override
    public void run() {
        try {
            objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            objectOutputStream.flush();
        } catch (IOException e) {
            server.processingException(new IOException("Ошибка создания выходного потока", e));
            server.disconnect(this);
        }

        //запускаем поток слушания inputStream
        new Thread(new Runnable() {
            @Override
            public void run() {
                listen();
            }
        }).start();
    }

    /**
     * Отправляет пакет обслуживаемому клиенту
     *
     * @param serverPack
     */
    public void sendPack(ServerPack serverPack) {
        try {
            //отправка пакета возможна как потоком в котором диспетчерПодключений слушает, так и поток извне, вызвавшим метод sendPark у serverable
            synchronized (this) {
                objectOutputStream.writeObject(serverPack);
                objectOutputStream.flush();
                server.writeToLog("отправлено " + serverPack + " в направлении " + socket);
            }
        } catch (IOException e) {
            getServer().processingException(e);
            //удаляем текущее подключение из списка подключений сервера
            server.disconnect(this);
        }
    }

    /**
     * Прослушивание входящего stream
     */
    private void listen() {
        try {
            objectInputStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        } catch (IOException e) {
            server.processingException(new IOException("Ошибка создания входного потока", e));
            server.disconnect(this);
        }

        while (!socket.isClosed()) {
            bodyListener();
        }
    }

    private void bodyListener() {
        try {
            ClientPack clientPack = (ClientPack) objectInputStream.readObject();
            server.writeToLog("получено " + clientPack + " от " + socket);
            processingPack(clientPack);
        } catch (ClassNotFoundException e) {
            server.processingException(e);
        } catch (IOException e) {
            //удаляем текущее подключение из списка подкючений сервера
            server.disconnect(this);
        }
    }

    public void close() {
/*
        стримы закроются при закрытии сокета
*/
        if (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
