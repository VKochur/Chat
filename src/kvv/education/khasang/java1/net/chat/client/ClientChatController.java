package kvv.education.khasang.java1.net.chat.client;

import kvv.education.khasang.java1.chat.views.gui.Event;
import kvv.education.khasang.java1.chat.views.gui.WindowControllerChat;
import kvv.education.khasang.java1.chat.views.gui.WindowViewChat;
import kvv.net.Client;
import kvv.net.ClientPack;
import kvv.net.ServerPack;

import java.io.IOException;

/**
 * Контролер в клиентской части чата
 * Имеет связь к вьюхой клиентской части чата.
 * 1. Принимает от вьюхи сообщения и передает их серверной части в режиме клиента
 * 2. Прослушивает в режиме клиента сообщения от серверной части, и передает их вьюхе
 */
public class ClientChatController extends WindowControllerChat {

    //незначимый параметр
    private static final int INSIGNIFICANT_COD = 0;

    //клиент в схеме клиент-сервер передачи данных
    Client client;

    public ClientChatController() {
        ClientChatController clientChatController = this;
        client = new Client() {

            //Обработка принятых пакетов от сервера заключается в том, что пришедшая с сервера информация передается контролеру, и он в дальнейшем передает ее в вьюху
            @Override
            public void processingPack(ServerPack serverPack) {
                Object[] contextEventInModel = (Object[]) serverPack.getContext()[1];
                Event eventInModel = (Event) serverPack.getContext()[0];
                writeToLog("Отправление произошедшего на серверной стороне события  вьюхе: " + eventInModel);
                clientChatController.sendEventToView(eventInModel, contextEventInModel);
            }

            @Override
            public void close() {
                //при закрытии ресурсов, отобразим инфо об этом на вьюхе
                clientChatController.sendEventToView(Event.MODEL_CREATED_EXCEPTION, "Соединение с сервером отсутствует");
                //освободим все ресурсы по поддержании сети
                super.close();
            }
        };
    }

    /**
     * В качестве вью может быть использована только ClientChatView
     *
     * @param viewChat
     */
    @Override
    public void setViewChat(WindowViewChat viewChat) {
        if (viewChat instanceof ClientChatView) {
            super.setViewChat(viewChat);
        } else {
            throw new IllegalArgumentException("В ClientChatController должна быть указана связь с вью ClientChatView");
        }
    }

    /**
     * Принятие контролером событий произошедших во вьюхе
     *
     * @param eventInView
     * @param contextEventInView
     */
    @Override
    public void tryTakeEvent(Event eventInView, Object... contextEventInView) {
        client.writeToLog("Пришло событие из вью: " + eventInView);
        //событие надо отправлять на сервер
        ClientPack clientPark;
        clientPark = new ClientPack(INSIGNIFICANT_COD, eventInView, contextEventInView);
        client.sendPack(clientPark);
    }

    /**
     * Создать соединение с серверной частью чата.
     *
     * @param ipServer
     * @param portServer
     * @throws IOException в случае невозможности установить соединение
     */
    public void startConnect(String ipServer, Integer portServer) throws IOException {
        client.configConnection(ipServer, portServer);
        client.startConnect();
    }

    /**
     * Отключить соединение с серверной частью чата
     */
    public void stopConnect() {
        if (client.isClientStarted()) {
            client.stopConnect();
        }
    }
}
