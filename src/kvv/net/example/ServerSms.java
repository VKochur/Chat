package kvv.net.example;

import kvv.net.Server;
import kvv.net.Serverable;

import java.net.Socket;

/**
 * Сервер в схеме отправки смс между клиентами
 */
public class ServerSms extends Server<Dispatcher>{
    @Override
    public Dispatcher getSocketDispatcherInstance(Socket socket, Serverable<Dispatcher> server) {
        return new Dispatcher(socket, server);
    }
}
