package kvv.education.khasang.java1.chat.views.gui;

import kvv.education.khasang.java1.chat.model.*;
import kvv.education.khasang.java1.chat.model.basic_entity.Dialog;
import kvv.education.khasang.java1.chat.model.basic_entity.Message;
import kvv.education.khasang.java1.chat.model.basic_entity.User;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Презентер в моделе MVP приложения чат с gui
 */
public class WindowControllerChat {
    //ключ по которому засыпает поток основного цикла, отработки событий во вьюхе
    final Object key = new Object();

    //сообытие во вьюхе, и контекст события
    public volatile Event eventInView;
    public volatile Object[] contextEventInView;

    private WindowViewChat viewChat;
    private ModelChat modelChat;

    private String preparedForSendMessage;

    public WindowControllerChat() {
        eventInView = null;
        contextEventInView = null;
    }

    public void setModelChat(ModelChat modelChat) {
        this.modelChat = modelChat;
        if (modelChat != null) {

            IOChat ioChat = new IOChat() {
                @Override
                public String getPreparedString() {
                    return preparedForSendMessage;
                }

                @Override
                public void outMessageForSee(Message message, String autor) {

                }
            };
            modelChat.setIOChatInterface(ioChat);
            //this.modelChat.setIOChatInterface(viewChat);
        }
    }

    public void setViewChat(WindowViewChat viewChat) {
        this.viewChat = viewChat;
        viewChat.setWindowControllerChat(this);
        //if (modelChat != null) {
        //    modelChat.setIOChatInterface(viewChat);
        //}
    }

    public ModelChat getModelChat() {
        return modelChat;
    }

    public WindowViewChat getViewChat() {
        return viewChat;
    }

    public UUID getIdStorage() {
        try {
            return modelChat.getStorageConnector().getStorageId();
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Не удалось получить id хранилища");
        } catch (UndefinedConnectorException e) {
            throw new IllegalStateException("Не определен коннектор в моделе");
        }
    }

    /**
     * Главный цикл прослушивания событий вьюхи
     */
    public void start() {

        while (true) {

            //--тут поток главного цикла должен уснуть----
            try {
                synchronized (key) {
                    key.wait();
                }
            } catch (InterruptedException e) {
                //прерывать главный поток цикла никто не должен
                processingException(e);
                throw new IllegalStateException("Получено прерывание на главном потоке цикла контролера");
            }
            //--------------------усыпили------------------

            //поток проснется при получении события с вьюхи

            if (eventInView == Event.VIEW_DO_ENTER_USER) {
                String login = (String) contextEventInView[0];
                char[] password = (char[]) contextEventInView[1];
                //отправили сообщение что произошел выход и логин пользователя, который вышел
                try {
                    try {
                        sendEventToView(Event.MODEL_USER_DO_EXIT, modelChat.getCurrentUser());
                    } catch (UndefinedUserException e) {
                        //если пользователь был неопределен ничего не делаем
                    }
                    if (modelChat.makeEnter(login, String.valueOf(password))) {
                        password = null;
                        try {
                            sendEventToView(Event.MODEL_USER_DO_ENTER, modelChat.getCurrentUser(), modelChat.getAvailableDialogs());
                        } catch (NonexistentEntitytException e) {
                            //такого не должно случиться
                            sendErrorMessageToView("Ошибка приложения, обратитесь к разработчику (аутентификация прошла успешно, но пользователь не найден в базе)");
                            processingException(e);
                        } catch (UndefinedUserException e) {
                            //такого не должно случиться
                            sendErrorMessageToView("Ошибка приложения, обратитесь к разработчику (аутентификация прошла успешно, но пользователь не определен)");
                            processingException(e);
                        }
                    } else {
                        sendErrorMessageToView("Неверный логин или пароль");
                    }
                } catch (UndefinedConnectorException e) {
                    //такого не должно случиться
                    sendErrorMessageToView("Ошибка приложения, обратитесь к разработчику (не указан способ соединения)");
                    processingException(e);
                } catch (IOException e) {
                    sendErrorMessageToView("Ошибка в структуре хранилища данных. Проверьте настройки соединения");
                    processingException(e);
                }

                eventInView = null;
            }

            if (eventInView == Event.VIEW_CHOOSE_DIALOG) {

                Dialog currentDialog = (Dialog) contextEventInView[0];
                try {
                    modelChat.changeDialog(currentDialog);
                    Set<User> dialogUsers = modelChat.getDialogUsers();
                    sendEventToView(Event.MODEL_CHANGE_DIALOG, currentDialog, defineUsersStatus(dialogUsers));
                    List<Message> messageList = modelChat.getMappedMessages();
                    showMessageInView(messageList);

                } catch (NonexistentEntitytException e) {
                    //такого не должно случиться
                    sendErrorMessageToView("Ошибка приложения, обратитесь к разработчику (диалог или пользователь не найдены в базе)");
                    processingException(e);
                } catch (UndefinedConnectorException e) {
                    //такого не должно случиться
                    sendErrorMessageToView("Ошибка приложения, обратитесь к разработчику (не указан способ соединения)");
                    processingException(e);
                } catch (UndefinedUserException e) {
                    //такого не должно случиться
                    sendErrorMessageToView("Ошибка приложения, обратитесь к разработчику (аутентификация прошла успешно, но пользователь не определен)");
                    processingException(e);
                } catch (UndefinedDialogException e) {
                    //такого не должно случиться
                    sendErrorMessageToView("Ошибка приложения, обратитесь к разработчику (диалог выбран, но не определен)");
                    processingException(e);
                } catch (IOException e) {
                    sendErrorMessageToView("Ошибка в структуре хранилища данных. Проверьте настройки соединения");
                    processingException(e);
                }

                eventInView = null;
            }

            if (eventInView == Event.VIEW_SEND_MESSAGE) {
                try {
                    //подготовим строку, которую надо отправить
                    preparedForSendMessage = (String) contextEventInView[0];
                    List<Message> newMessages = modelChat.sendMessageFromUser();
                    showMessageInView(newMessages);
                    sendEventToView(Event.CONTROLLER_UPDATED_LIST_MSG, modelChat.getCurrentDialog());

                } catch (NonexistentEntitytException e) {
                    //такого не должно случиться
                    sendErrorMessageToView("Ошибка приложения, обратитесь к разработчику (аутентификация прошла, диалог выбран, сущности не найдены в хранилище)");
                    processingException(e);
                } catch (UndefinedConnectorException e) {
                    //такого не должно случиться
                    sendErrorMessageToView("Ошибка приложения, обратитесь к разработчику (не указан способ соединения)");
                    processingException(e);
                } catch (UndefinedUserException e) {
                    //никогда не произойдет, т.к. в случае неопределенного пользователя, sendMessageFromUser выбросит в начале UndefinedDialogException, и только потом UndefinedUserException
                    sendErrorMessageToView("Не указан пользователь. Предварительно авторизуйтесь");
                } catch (UndefinedDialogException e) {
                    sendErrorMessageToView("Предварительно должен быть выбран диалог. Авторизуйтесь и выберете доступный диалог");
                } catch (IOException e) {
                    sendErrorMessageToView("Ошибка в структуре хранилища данных. Проверьте настройки соединения");
                    processingException(e);
                }

                eventInView = null;
            }

            if (eventInView == Event.VIEW_CREATE_DIALOG) {
                String newDialogName = (String) contextEventInView[0];
                try {
                    modelChat.createDialog(newDialogName);

                    Dialog currentDialog = null;
                    Set<User> availableUsers = null;
                    Set<UserInChat> availableUsersInChat = null;
                    try {
                        currentDialog = modelChat.getCurrentDialog();
                        availableUsers = modelChat.getDialogUsers();
                        availableUsersInChat = defineUsersStatus(availableUsers);
                    } catch (UndefinedDialogException e) {
                        //диалог не определен
                    }
                    //обновим панель диалогов вьюхи, при обновлении отображении текущей беседы обнуляется
                    sendEventToView(Event.MODEL_CREATE_DIALOG, modelChat.getAvailableDialogs(), availableUsersInChat);
                    //восстановим отображение беседы
                    if (currentDialog != null) {
                        List<Message> msgs = modelChat.getMappedMessages();
                        showMessageInView(msgs);
                    }
                } catch (NonexistentEntitytException e) {
                    //такого не должно случиться
                    sendErrorMessageToView("Ошибка приложения, обратитесь к разработчику (сущности не найдены в хранилище)");
                    processingException(e);
                } catch (UndefinedConnectorException e) {
                    //такого не должно случиться
                    sendErrorMessageToView("Ошибка приложения, обратитесь к разработчику (не указан способ доступа к хранилищу)");
                    processingException(e);
                } catch (UndefinedUserException e) {
                    //такого не должно случиться
                    sendErrorMessageToView("Ошибка приложения, обратитесь к разработчику (текущий пользователь не найден в хранилище)");
                    processingException(e);
                } catch (IOException e) {
                    //такого не должно случиться
                    sendErrorMessageToView("Ошибка приложения (нет доступа или неправильная структура хранилища)");
                    processingException(e);
                }

                eventInView = null;
            }

            if (eventInView == Event.VIEW_ADD_USER_TO_DIALOG) {
                String login = (String) contextEventInView[0];
                try {
                    User user = modelChat.getUserByLogin(login);
                    if (!Objects.equals(user, null)) {
                        modelChat.addUserIntoDialog(user.getId());
                        sendEventToView(Event.MODEL_ADD_USER_TO_DIALOG, defineUsersStatus(modelChat.getDialogUsers()), user, modelChat.getCurrentDialog());
                    } else {
                        sendEventToView(Event.CONTROLLER_CMD_TO_VIEW_SHOW_MSG_WINDOW, JOptionPane.ERROR_MESSAGE, "Ошибка при добавлении", "Пользователь '" + login + "' не найден");
                    }
                } catch (UndefinedConnectorException e) {
                    //такого не должно случиться
                    sendErrorMessageToView("Ошибка приложения, обратитесь к разработчику (не указан способ доступа к хранилищу)");
                    processingException(e);
                } catch (IOException e) {
                    //такого не должно случиться
                    sendErrorMessageToView("Ошибка приложения (нет доступа или неправильная структура хранилища)");
                    processingException(e);
                } catch (NonexistentEntitytException e) {
                    //такого не должно случиться
                    sendErrorMessageToView("Ошибка приложения, обратитесь к разработчику (сущности не найдены в хранилище)");
                    processingException(e);
                } catch (UndefinedDialogException e) {
                    //такого не должно случиться
                    sendErrorMessageToView("Ошибка приложения, обратитесь к разработчику (не определен диалог)");
                    processingException(e);
                }

                eventInView = null;
            }

            if (eventInView == Event.VIEW_UPDATE_MSGS) {
                try {
                    List<Message> msgs = modelChat.updateMappedMessages();
                    showMessageInView(msgs);
                } catch (NonexistentEntitytException e) {
                    sendErrorMessageToView("Ошибка приложения, обратитесь к разработчику (сущности не найдены в хранилище)");
                    processingException(e);
                } catch (UndefinedConnectorException e) {
                    sendErrorMessageToView("Ошибка приложения, обратитесь к разработчику (не определен конектор)");
                    processingException(e);
                } catch (UndefinedDialogException e) {
                    sendErrorMessageToView("Ошибка приложения (нет определен текущий диалог)");
                    processingException(e);
                } catch (IOException e) {
                    sendErrorMessageToView("Ошибка приложения (нет доступа или неправильная структура хранилища)");
                    processingException(e);
                }

                eventInView = null;
            }

            if (eventInView == Event.VIEW_CREATE_NEW_USER) {
                try {
                    modelChat.createUser((String) contextEventInView[0], (String) contextEventInView[1]);
                } catch (NonUniqueException e) {
                    sendErrorMessageToView("Пользователь с логином '" + (String) contextEventInView[0] + "' уже зарегистрирован");
                } catch (UndefinedConnectorException e) {
                    sendErrorMessageToView("Ошибка приложения, обратитесь к разработчику (не определен конектор)");
                    processingException(e);
                } catch (IOException e) {
                    sendErrorMessageToView("Ошибка приложения (нет доступа или неправильная структура хранилища)");
                    processingException(e);
                }

                eventInView = null;
            }


            if (eventInView == Event.TO_CONTROLLER_CMD_STOP_PROCESS) {
                try {
                    //выход активного пользователя
                    sendEventToView(Event.MODEL_USER_DO_EXIT, modelChat.getCurrentUser());
                } catch (UndefinedUserException e) {
                    //если пользователь не определен, ничего не делаем
                }
                // разрушаем цикл
                break;
            }
        }
    }

    private void sendErrorMessageToView(String message) {
        sendEventToView(Event.MODEL_CREATED_EXCEPTION, message);
    }

    /**
     * Определяет статус пользователей
     *
     * @param users
     * @return
     */
    private Set<UserInChat> defineUsersStatus(Set<User> users) {
        Set<UserInChat> usersInChat = new HashSet<>();
        for (User user : users) {
            usersInChat.add(new UserInChat(user, defineStatus(user)));
        }
        return usersInChat;
    }

    /**
     * Определяет статус пользователя. При данном контролере статус всегда возвращает неопределен.
     *
     * @param user
     * @return
     */
    public UserStatus defineStatus(User user) {
        return UserStatus.UNKNOWN;
    }

    /**
     * Выводит список сообщений в окне сообщений
     *
     * @param messageList
     */
    private void showMessageInView(List<Message> messageList) {
        for (Message message : messageList) {
            User autor = null;
            try {
                autor = modelChat.getUserById(message.getIdAuthor());
            } catch (UndefinedConnectorException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sendEventToView(Event.CONTROLLER_CMD_TO_VIEW_SHOW_MESSAGE, message, autor.getLogin());
        }
    }

    //обработка исключений
    private void processingException(Exception e) {

    }

    /**
     * Пытается принять на обработку событие произошедшее во вьюхе.
     * При принятии на обработку события произошедшее во вьюхе, разбудит основной поток обработки событий.
     * <p>
     * Если на обработке уже имеется событие, то игнорирует пришедшее событие
     * Не используется очередь, т.к. в результате отработки события, последующее событие в очереди может быть не логичным
     * Например
     * 1 событие - вошел другой пользователь (пусть будет растянуто по времени, например из синхронизации, вьюха не отобразит выход текущего пользователя пока модель не поменяется
     * и можно будет сделать 2 событие во вьюхе)
     * 2 событие - получить список сообщений. - в результате будет 2 события: выход из чата, и получить сообщения. Действия во вью выглядят логичными, а после изменения модели по 1му
     * событию, 2е уже не возможно
     */

    public void tryTakeEvent(Event eventInView, Object... contextEventInView) {
        if (this.eventInView == null) {
            this.eventInView = eventInView;
            this.contextEventInView = contextEventInView;

            //тут поток главного цикла  должен проснуться
            synchronized (key) {
                key.notifyAll();
            }
            //-------пробудили-----------------------

        } else {
            //не принятие контроллером сообщения нормальная ситуация в ситуации когда сообщения на получение обновлений сообщений постоянно генерятся вьюхой в отдельном потоке
        }
    }

    public void sendEventToView(Event event, Object... contextEvent) {
        viewChat.takeEvent(event, contextEvent);
    }
}
