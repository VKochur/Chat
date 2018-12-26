package kvv.net.example;

import kvv.net.ClientPack;
import kvv.net.ServerPack;
import kvv.net.Serverable;
import kvv.net.SocketDispatcher;

import java.net.Socket;
import java.util.*;

/**
 * Диспетчер отработки соединения между клиентом принимающим/отправляющим смс и сервером
 */
public class Dispatcher extends SocketDispatcher {

    //сетевое имя клиента соединение с которым обрабатывается данным диспетчером
    String nik;

    public Dispatcher(Socket socket, Serverable<Dispatcher> serverChat) {
        super(socket, serverChat);
    }

    /**
     * Анализ получаемых от клиента пакетов
     * @param clientPack
     */
    @Override
    public void processingPack(ClientPack clientPack) {
        int cod = clientPack.getCod();
        Object[] context = clientPack.getContext();
        if (cod == CmdFromClient.GET_LIST_ABONENT.getCod()) {
            sendListAbonents(clientPack.getUuid());
        }
        if (cod == CmdFromClient.SEND_SMS.getCod()) {
            sendSms((String) context[0], (String) context[1]);
        }
        if (cod == CmdFromClient.DISCONNECT.getCod()) {
            disconnect();
        }
        if (cod == CmdFromClient.DISCONNECT_ALL.getCod()){
            disconnectAll();
        }
        if (cod == CmdFromClient.SET_NIK.getCod()){
            setNik((String)context[0]);
        }
    }

    /**
     * Разорвать соединения сервера со всеми абонентами
     */
    private void disconnectAll() {
        getServer().closeSocketDispatchers();
    }

    /**
     * Разорвать соединение с абонентом, обслуживаемым данным диспетчером
     */
    private void disconnect() {
        getServer().disconnect(this);
    }

    /**
     * Отправить смс
     * @param toNik сетевое имя клиента
     * @param text текст сообщения
     */
    private void sendSms(String toNik, String text) {
        Serverable<Dispatcher> serverSms = getServer();
        List<Dispatcher> dispatcherList = serverSms.getSocketDispatchersList();
        //сформировали пакет - код означающий что это смс, информация от кого смс, текст сообщения
        ServerPack serverPack = new ServerPack(CmdFromServer.TAKE_SMS.getCod(), getNik(), text);
        for (Dispatcher dispatcher : dispatcherList) {
            if (Objects.equals(dispatcher.getNik(),toNik)){
                //отправили пакет через диспетчера, связанного с нужным адресатом
                dispatcher.sendPack(serverPack);
            }
        }
    }

    /**
     * Отправить список имеющихся сетевых имен подключенных к серверу
     * @param uuidQuestion
     */
    private void sendListAbonents(UUID uuidQuestion) {
        //определили список подключенных
        List<Dispatcher> dispatcherList = Collections.unmodifiableList(getServer().getSocketDispatchersList());
        List<String> niks = new ArrayList<>();
        for (Dispatcher dispatcher : dispatcherList) {
            niks.add(dispatcher.getNik());
        }
        //сформировали пакет  - код означающий что это ответ, ид - ответ на что, содержание ответа
        ServerPack serverPack = new ServerPack(CmdFromServer.GET_ANSWER.getCod(), uuidQuestion, niks);
        //отправили пакет
        sendPack(serverPack);
    }

    /**
     * Установить сетевое имя для обслуживаемого клиента
     * @param nik
     */
    public void setNik(String nik) {
        this.nik = nik;
    }

    /**
     * Получить сетевое имя обслуживаемого абонента
     * @return
     */
    public String getNik() {
        return nik;
    }
}
