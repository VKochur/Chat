package kvv.education.khasang.java1.net.chat.client;

import kvv.education.khasang.java1.chat.views.gui.WindowControllerChat;
import kvv.education.khasang.java1.chat.views.gui.WindowViewChat;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * Вью в клиентской части приложения чат
 * Вью не осуществляет прослушивание хранилища на наличие новых сообщений
 * При закрытии окна вью, связанный в вью контролер освобождает ресурсы задействованные в поддержании соединения с сервером
 */
public class ClientChatView extends WindowViewChat {

    public ClientChatView() throws HeadlessException {
        super();
        //не осуществляется прослушивание хранилища на наличие новых сообщений
        setDoListenNewMessages(false);
        setTitleSuffix("/Клиентская версия чата/");
        ClientChatView clientChatView = this;
        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                //при закрытии окна освобождаем ресурсы
                if (clientChatView.getWindowControllerChat() != null) {
                    ((ClientChatController) clientChatView.getWindowControllerChat()).stopConnect();
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });
    }

    /**
     * В качестве контролера должен использоваться ClientChatController
     *
     * @param windowControllerChat
     */
    @Override
    public void setWindowControllerChat(WindowControllerChat windowControllerChat) {
        if (windowControllerChat instanceof ClientChatController) {
            super.setWindowControllerChat(windowControllerChat);
        } else {
            throw new IllegalArgumentException("В ClientChatView должна быть указана связь с контроллером ClientChatController");
        }
    }
}
