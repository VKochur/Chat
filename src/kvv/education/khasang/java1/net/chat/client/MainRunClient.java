package kvv.education.khasang.java1.net.chat.client;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Главный класс для запуска клиента чата
 * В качестве args:
 * args[0] - ip компа, где запущена серверная часть
 * args[1] - номер порта
 * <p>
 * Если args.length <= 1 используются умолчательные 127.0.0.1:8082
 */
public class MainRunClient {

    private static final String IP_SERVER_DEFAULT = "127.0.0.1";
    private static final Integer PORT_DEFAULT = 8082;

    private static ClientChatView chatView;

    public static void main(String[] args) {
        String ipServer = IP_SERVER_DEFAULT;
        Integer port = PORT_DEFAULT;

        System.out.println("Запуск клиента чата..");

        if (args.length > 1) {
            ipServer = args[0];
            port = Integer.valueOf(args[1]);
        }

        runView();
        ClientChatController chatController = new ClientChatController();
        chatController.setViewChat(chatView);
        try {
            chatController.startConnect(ipServer, port);
        } catch (IOException e) {
            chatView.showError("Нет подключения к серверу: " + ipServer + ":" + String.valueOf(port));
        }
    }

    private static void runView() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    chatView = new ClientChatView();
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
