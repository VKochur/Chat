package kvv.net;

import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * Тоже что и Serverable
 * Если не нужна иерархия наследования можно наследоваться от данного класса. Иначе реализовывать Serverable
 *
 * По умолчанию пишет лог действий в System.err
 * По умолчанию обработка исключений подразумевает вывод состояния стэка в System.err
 * @param <T>
 */
public abstract class Server<T extends SocketDispatcher> implements Serverable<T> {

    private Thread threadServer;
    private Integer port;
    private ServerSocket serverSocket;
    private List<T> socketDispatchersList;

    private PrintStream logWriter;
    private Boolean needWriteLog;

    public Server() {
        setNeedWriteLog(true);
        setLogWriter(System.err);
    }

    @Override
    public abstract T getSocketDispatcherInstance(Socket socket, Serverable<T> server);

    public void processingException(Exception e){
        e.printStackTrace();
    }

    //------------------------------------

    @Override
    public Thread getThreadServer() {
        return threadServer;
    }

    @Override
    public void setThreadServer(Thread threadServer) {
        this.threadServer = threadServer;
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
    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    @Override
    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public List<T> getSocketDispatchersList() {
        return socketDispatchersList;
    }

    @Override
    public void setSocketDispatchersList(List<T> socketDispatchersList) {
        this.socketDispatchersList = socketDispatchersList;
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
