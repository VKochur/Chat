import kvv.education.khasang.java1.chat.model.ModelChat;
import kvv.education.khasang.java1.chat.model.basic_entity.Dialog;
import kvv.education.khasang.java1.chat.model.basic_entity.User;
import kvv.education.khasang.java1.chat.views.gui.Event;
import kvv.education.khasang.java1.chat.views.gui.UserStatus;
import kvv.education.khasang.java1.chat.views.gui.WindowControllerChat;
import kvv.education.khasang.java1.chat.views.gui.WindowViewChat;
import kvv.net.ServerPack;

/**
 * Контролер чата на серверной стороне
 * Используется в диспетчере подключений
 */
public class ServerChatController extends WindowControllerChat {

    //незначимый параметр
    private static final int INSIGNIFICANT_COD = 0;

    /**
     * Диспетчер отработки соединения клиент-сервер
     */
    private ChatSocketDispatcher dispatcher;

    public ServerChatController() {
        super();
    }

    public void setDispatcher(ChatSocketDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public ChatSocketDispatcher getDispatcher() {
        return dispatcher;
    }

    @Override
    public void setModelChat(ModelChat modelChat) {
        super.setModelChat(modelChat);
    }

    /**
     * Данный контролер не имеет связь с вьюхой. События произошедшие во вьюхе и их контекст он получает по сети
     *
     * @param viewChat
     */
    @Override
    public void setViewChat(WindowViewChat viewChat) {
        throw new UnsupportedOperationException("Контролер чата используемый в диспетчере обработки связи клиента с сервером не должен быть связан с вью");
    }

    @Override
    public ModelChat getModelChat() {
        return super.getModelChat();
    }

    @Override
    public WindowViewChat getViewChat() {
        throw new UnsupportedOperationException("Контролер чата используемый в диспетчере обработки связи клиента с сервером не должен быть связан вью");
    }

    /**
     * Главный цикл обработки событий как и у родительского класса
     */
    @Override
    public void start() {
        super.start();
    }

    /**
     * Получение событий и контекста событий такое же как и в родительском классе
     *
     * @param eventInView
     * @param contextEventInView
     */
    @Override
    public void tryTakeEvent(Event eventInView, Object... contextEventInView) {
        getDispatcher().getServer().writeToLog("Передача пакета контролеру:" + eventInView);
        super.tryTakeEvent(eventInView, contextEventInView);
    }

    /**
     * События произошедших в моделе или команды контролера отправляются в отличие от родительского класса по сети в сторону клиента
     * где в дальнейшем принимаются клиентом, отдаются контролеру на стороне клиента и передаются во вьюху
     * <p>
     * После передачи в сторону клиента, провоисходит анализ на необходимость дополнительных действий для других имеющихся подключений
     * (например передача другим подключенным к тому же чату инфо о новом введеном сообщении, или инфо о включении пользователей в диалог,
     * или о входе/выходе пользователей в чат)
     *
     * @param event
     * @param contextEvent
     */
    @Override
    public void sendEventToView(Event event, Object... contextEvent) {
        //формируем пакет о прошедших событиях
        ServerPack serverPack = new ServerPack(INSIGNIFICANT_COD, event, contextEvent);
        //отправляем на клиентскую сторону изменения произошедшие в модели
        getDispatcher().sendPack(serverPack);


        if (event == Event.MODEL_USER_DO_ENTER) {
            User user = (User) contextEvent[0];
            getDispatcher().sayServerEnterUser(user);
        } else if (event == Event.MODEL_USER_DO_EXIT) {
            User user = (User) contextEvent[0];
            getDispatcher().sayServerExitUser(user);
        } else if (event == Event.CONTROLLER_UPDATED_LIST_MSG) {
            Dialog dialog = (Dialog) contextEvent[0];
            getDispatcher().sayServerUpdateMsg(dialog);
        } else if (event == Event.MODEL_ADD_USER_TO_DIALOG) {
            User newUser = (User) contextEvent[1];
            Dialog dialog = (Dialog) contextEvent[2];
            getDispatcher().sayServerNewUserIntoDialog(newUser, dialog);
        }
    }

    @Override
    public UserStatus defineStatus(User user) {
        return getDispatcher().getStatusUser(user);
    }
}
