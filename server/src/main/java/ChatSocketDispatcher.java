import kvv.education.khasang.java1.chat.model.IOChat;
import kvv.education.khasang.java1.chat.model.ModelChat;
import kvv.education.khasang.java1.chat.model.basic_entity.Dialog;
import kvv.education.khasang.java1.chat.model.basic_entity.Message;
import kvv.education.khasang.java1.chat.model.basic_entity.User;
import kvv.education.khasang.java1.chat.views.gui.Event;
import kvv.education.khasang.java1.chat.views.gui.UserStatus;
import kvv.net.ClientPack;
import kvv.net.Serverable;
import kvv.net.SocketDispatcher;

import java.net.Socket;

/**
 * Диспетчер отработки подключения к серверу клиента чата
 * Содержит в себе модель чата и контролер
 */
public class ChatSocketDispatcher extends SocketDispatcher {
    /**
     * Интерфейс ввода-вывода сообщений используемый моделью
     */
    IOChat ioChat;
    ModelChat modelChat;
    ServerChatController chatController;

    //сообщение, которое надо отправить в чат. Используется для реализации интерфейса ioChat, используемого моделью
    String preparedMessage;

    public ServerChatController getChatController() {
        return chatController;
    }

    public void setModelChat(ModelChat modelChat) {
        this.modelChat = modelChat;
        chatController.setModelChat(modelChat);
        ioChat = new IOChat() {
            //команда отправить сообщение принимается контролером, который вызывает метод модели отправить сообщение, которая берет из данного метода подготовленную строку для отправки
            @Override
            public String getPreparedString() {
                return preparedMessage;
            }

            //вывод сообщений осуществляется через команды контролера, посылающего команды в сторону клиента
            public void outMessageForSee(Message message, String autor) {
                //ничего не делает
            }
        };
        modelChat.setIOChatInterface(ioChat);
    }

    public ChatSocketDispatcher(Socket socket, Serverable serverChat) {
        super(socket, serverChat);
        chatController = new ServerChatController();
        //устанавливаем связь контролера с текущим диспетчером. именно через диспетчера будут поступать события, и передаваться события от контролера на клиентскую сторону
        chatController.setDispatcher(this);

        //запускаем цикл обработки получаемых сообщений в новом потоке, который заснет до получения события
        new Thread(new Runnable() {
            @Override
            public void run() {
                chatController.start();
            }
        }).start();
    }

    /**
     * Обработка полученных пакетов от клиента в основном заключается в передаче контролеру (в котором спит поток обработки в ожидании событий) содержащихся в них команд и контекста команд
     * Если пакет содержит команду отправки сообщения, то тестируем в начале сообщение на допустимость
     *
     * @param clientPack
     */
    @Override
    public void processingPack(ClientPack clientPack) {
        Event event = (Event) clientPack.getContext()[0];
        Object[] contextEvent = (Object[]) clientPack.getContext()[1];
        getServer().writeToLog("Анализ полученного пакета");
        if (event == Event.VIEW_SEND_MESSAGE) {
            String temp = (String) contextEvent[0];
            if (valid(temp)) {
                preparedMessage = (String) contextEvent[0];
                chatController.tryTakeEvent(event, contextEvent);
            } else {
                //полученное сообщение не прошло проверку на допустимость, разрываем соединение
                getServer().disconnect(this);
            }
        } else {
            chatController.tryTakeEvent(event, contextEvent);
        }
    }

    //допустима фраза в которой не попадается запрещенных слов, а не фраза не включенная в список запрещенных слов и фраз
    private boolean valid(String temp) {
        //разбиваем строку на слова и определяем допустимость каждого
        String[] words = temp.split(" ");
        for (String word : words) {
            //избавимся в конце от переноса строки
            while (word.charAt(word.length()-1) == '\n') {
                word = word.substring(0, word.length() - 1);
            }
            if (((ChatServer) getServer()).isTaboo(word)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void close() {
        //остановить еще потоки запущенные в цикле прослушивании получаемых контролером событий
        chatController.tryTakeEvent(Event.TO_CONTROLLER_CMD_STOP_PROCESS);
        //закрыть все ресурсы связынные с поддержанием соединения по сети
        super.close();
    }

    public void sayServerEnterUser(User user) {
        ((ChatServer) getServer()).enterUser(user);
    }

    public void sayServerExitUser(User user) {
        ((ChatServer) getServer()).exitUser(user);
    }

    public void sayServerUpdateMsg(Dialog dialog) {
        ((ChatServer) getServer()).sendUpdateMsgs(dialog);
    }

    public void sayServerNewUserIntoDialog(User newUser, Dialog dialog) {
        ((ChatServer) getServer()).sendMsgsAboutNewUser(newUser, dialog);
    }

    public UserStatus getStatusUser(User user) {
        return ((ChatServer) getServer()).getStatusUser(user);
    }
}
