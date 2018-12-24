package kvv.education.khasang.java1.net.chat.server;

import kvv.education.khasang.java1.chat.model.*;
import kvv.education.khasang.java1.chat.model.basic_entity.Dialog;
import kvv.education.khasang.java1.chat.model.basic_entity.User;
import kvv.education.khasang.java1.chat.views.gui.Event;
import kvv.education.khasang.java1.chat.views.gui.UserStatus;
import kvv.net.Server;
import kvv.net.Serverable;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

import static kvv.education.khasang.java1.chat.views.gui.UserStatus.*;

/**
 * Серверная часть чата
 */
public class ChatServer extends Server<ChatSocketDispatcher> {

    //список пользователей онлайн (лист, а не множество, так как возможны повторения пользователей - один и тот же логин с разных подключений)
    List<User> onlineUsers;

    //набор информации, которую использует сервер при создании модели чата для подключений
    Parser parser;

    //создатель модели чата, которую использует сервер при подключении клиентов
    //используется именно он, если отличен от null, а не информация parser
    ModelChatCreator modelChatCreator;

    private Set<String> tabooWords;

    public ChatServer() {
        //пользователи онлайн
        onlineUsers = new LinkedList<>();
        //запрещеные слова
        tabooWords = new TreeSet<>();
    }

    public void addWordToTaboo(String word) {
        tabooWords.add(word);
    }

    public void removeWordFromTaboo(String word) {
        tabooWords.remove(word);
    }

    public Set<String> getTabooWords() {
        return Collections.unmodifiableSet(tabooWords);
    }

    public boolean isTaboo(String word) {
        return tabooWords.contains(word);
    }

    public ModelChatCreator getModelChatCreator() {
        return modelChatCreator;
    }

    /**
     * Чтобы сервер использовал в подключениях с клиентами модель чатов построенную не по информации parser
     * следует установить свою логику создания моделей установкой своего реализованного ModelChatCreator
     *
     * @param modelChatCreator
     */
    public void setModelChatCreator(ModelChatCreator modelChatCreator) {
        if (!isServerStarted()) {
            this.modelChatCreator = modelChatCreator;
        } else {
            throw new IllegalStateException("Сервер запущен. Изменение настроек построения модели чата невозможно");
        }
    }

    public void setParser(Parser parser) {
        if (!isServerStarted()) {
            this.parser = parser;
        } else {
            throw new IllegalStateException("Сервер запущен. Изменение настроек построения модели чата невозможно");
        }
    }

    public Parser getParser() {
        return parser;
    }

    /**
     * Создает отдельный диспетчер для отработки конкретного подключения клиента чата
     * Создает модель чата для конкретного подключения
     *
     * @param socket сокет подключения
     * @param server this
     * @return
     */
    @Override
    public ChatSocketDispatcher getSocketDispatcherInstance(Socket socket, Serverable<ChatSocketDispatcher> server) {
        ChatSocketDispatcher clientDispatcher = new ChatSocketDispatcher(socket, server);
        clientDispatcher.setModelChat(getModelChat());
        return clientDispatcher;
    }

    /**
     * @return модель чата
     * Для каждого подключения к серверу создается модель чата на основе данной.
     * Если указан modelChatCreator используется его логика построения моделей, иначе модель строится на основе
     * информации находящейся в parser
     */
    public ModelChat getModelChat() {
        if (modelChatCreator != null) {
            return modelChatCreator.getInstance();
        } else {
            ChatServer chatServer = this;
            modelChatCreator = new ModelChatCreator() {
                @Override
                public ModelChat getInstance() {

                    try {
                        if (chatServer.getParser() == null) {
                            return null;
                        } else {
                            return ModelFactory.getInstance(chatServer.getParser());
                        }
                    } catch (IllegalFormatParserException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            };

            ModelChat modelChat = modelChatCreator.getInstance();
            if (modelChat == null) {
                throw new NullPointerException("Невозможно построить модель чата для соединения. Некорректный parser");
            } else {
                return modelChat;
            }
        }
    }

    /**
     * Вход пользователя в чат
     *
     * @param user
     */
    public void enterUser(User user) {
        onlineUsers.add(user);
        sendMsgsAboutChangeStatus(user, ONLINE);
    }

    /**
     * Выход пользователя из чата
     *
     * @param user
     */
    public void exitUser(User user) {
        onlineUsers.remove(user);
        sendMsgsAboutChangeStatus(user, getStatusUser(user));
    }

    /**
     * Определение статуса пользователя
     *
     * @param user
     * @return
     */
    public UserStatus getStatusUser(User user) {
        return (onlineUsers.contains(user)) ? ONLINE : OFFLINE;
    }

    /**
     * Рассылка сообщения об изменении статуса пользователя по имеющимся подключениям
     *
     * @param user
     */
    private void sendMsgsAboutChangeStatus(User user, UserStatus status) {
        ChatServer chatServer = this;
        //делаем рассылку в новом потоке
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<ChatSocketDispatcher> chatSocketDispatchers = chatServer.getSocketDispatchersList();
                for (ChatSocketDispatcher chatSocketDispatcher : chatSocketDispatchers) {
                    ServerChatController serverChatController = chatSocketDispatcher.getChatController();
                    serverChatController.sendEventToView(Event.CONTROLLER_CMD_TO_VIEW_CHANGE_USER_STATUS, user, status);
                }
            }
        }).start();
    }

    /**
     * Рассылка сообщения о необходимости обновления окна сообщений по имеющимся подключениям с активным указанным диалогом
     *
     * @param dialog
     */
    public void sendUpdateMsgs(Dialog dialog) {
        ChatServer chatServer = this;
        //делаем рассылку в новом потоке
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<ChatSocketDispatcher> chatSocketDispatchers = chatServer.getSocketDispatchersList();
                for (ChatSocketDispatcher chatSocketDispatcher : chatSocketDispatchers) {
                    ServerChatController serverChatController = chatSocketDispatcher.getChatController();
                    ModelChat modelChat = serverChatController.getModelChat();
                    try {
                        if (modelChat.getCurrentDialog().equals(dialog)) {
                            serverChatController.tryTakeEvent(Event.VIEW_UPDATE_MSGS);
                        }
                    } catch (UndefinedDialogException e) {
                        //если диалог не определен, то данное подключение нас не интересует
                    }
                }
            }
        }).start();
    }

    /**
     * Рассылка сообщения по имеющимся подключениям пользователя о добавлении его в какую-либо беседу
     *
     * @param newUser
     * @param dialog
     */
    public void sendMsgsAboutNewUser(User newUser, Dialog dialog) {
        ChatServer chatServer = this;
        //делаем рассылку в новом потоке
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<ChatSocketDispatcher> chatSocketDispatchers = chatServer.getSocketDispatchersList();
                for (ChatSocketDispatcher chatSocketDispatcher : chatSocketDispatchers) {
                    ServerChatController serverChatController = chatSocketDispatcher.getChatController();
                    ModelChat modelChat = serverChatController.getModelChat();
                    try {
                        //если текущий пользователь подключения чата является тем, кого добавили в новую беседу
                        if (modelChat.getCurrentUser().equals(newUser)) {
                            serverChatController.sendEventToView(Event.CONTROLLER_CMD_TO_VIEW_SHOW_MSG_WINDOW, JOptionPane.INFORMATION_MESSAGE, "Вас добавили в беседу!", "Теперь вы участник беседы '" + dialog.getName() + "'/ перезайдите в чат для доступа");
                        }
                    } catch (UndefinedUserException e) {
                        //если пользователь не определен
                    }
                }
            }
        }).start();
    }
}
