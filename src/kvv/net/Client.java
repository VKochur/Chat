package kvv.net;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;

/**
 * Тоже что и Clientable
 * Если не нужна иерархия наследования можно наследоваться от данного класса. Иначе реализовывать Clientable
 *
 * По умолчанию пишет лог действий в System.err
 * По умолчанию обработка исключений подразумевает вывод состояния стэка в System.err
 */
public abstract class Client implements Clientable {

    private String ipServer;
    private Integer port;
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private Thread threadListener;

    private PrintStream logWriter;
    private Boolean needWriteLog;

    public Client() {
        setNeedWriteLog(true);
        setLogWriter(System.err);
    }


    public abstract void processingPack(ServerPack serverPack);

    /**
     * Обработчик исключений
     * @param e
     */
    public void processingException(Exception e){
        e.printStackTrace();
    }

    //------------------------------------

    @Override
    public Socket getSocket() {
        return socket;
    }

    @Override
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    @Override
    public ObjectInputStream getObjectInputStream() {
        return objectInputStream;
    }

    @Override
    public void setObjectInputStream(ObjectInputStream objectInputStream) {
        this.objectInputStream = objectInputStream;
    }

    @Override
    public ObjectOutputStream getObjectOutputStream() {
        return objectOutputStream;
    }

    @Override
    public void setObjectOutputStream(ObjectOutputStream objectOutputStream) {
        this.objectOutputStream = objectOutputStream;
    }

    @Override
    public String getIpServer() {
        return ipServer;
    }

    @Override
    public void setIpServer(String ipServer) {
        this.ipServer = ipServer;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    @Override
    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public Thread getThreadListener() {
        return threadListener;
    }

    @Override
    public void setThreadListener(Thread threadListener) {
        this.threadListener = threadListener;
    }

    @Override
    public PrintStream getLogWriter() {
        return logWriter;
    }

    @Override
    public void setLogWriter(PrintStream logWriter) {
        this.logWriter = logWriter;
    }

    @Override
    public boolean isNeedWriteLog() {
        return needWriteLog;
    }

    @Override
    public void setNeedWriteLog(boolean needWriteLog) {
        this.needWriteLog = needWriteLog;
    }

}
