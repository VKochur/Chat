package kvv.education.khasang.java1.chat.model;

import kvv.education.khasang.java1.chat.model.basic_entity.Dialog;
import kvv.education.khasang.java1.chat.model.basic_entity.Message;
import kvv.education.khasang.java1.chat.model.basic_entity.User;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ModelChat implements Chatable {
    //текущий пользователь
    private User user;
    //текущий диалог
    private Dialog dialog;
    //сообщения текущего диалога отображенные текущему пользователю
    private List<Message> messages;

    private StorageConnector storageConnector;
    private IOChat ioChat;

    public ModelChat() {
        user = null;
        dialog = null;
        messages = new LinkedList<>();
    }

    public void setStorageConnector(StorageConnector storageConnector) {
        this.storageConnector = storageConnector;
    }

    @Override
    public StorageConnector getStorageConnector() throws UndefinedConnectorException {
        if (storageConnector == null) {
            throw new UndefinedConnectorException();
        }
        return storageConnector;
    }

    @Override
    public User getCurrentUser() throws UndefinedUserException {
        if (this.user == null) {
            throw new UndefinedUserException();
        }
        return this.user;
    }

    @Override
    public void setCurrentUser(User User) {
        this.user = User;
    }

    @Override
    public Dialog getCurrentDialog() throws UndefinedDialogException {
        if (this.dialog == null) {
            throw new UndefinedDialogException();
        }
        return this.dialog;
    }

    @Override
    public void setCurrentDialog(Dialog newDialog) {
        this.dialog = newDialog;
    }

    @Override
    public List<Message> getMappedMessages() {
        return this.messages;
    }

    @Override
    public void setMappedMessages(List<Message> mappedMessages) {
        this.messages = mappedMessages;
    }

    @Override
    public void outMessageForSee(Message message, String autor) {
        ioChat.outMessageForSee(message, autor);
    }

    @Override
    public String getPreparedString() {
        return ioChat.getPreparedString();
    }


    public void setIOChatInterface(IOChat ioChatInterface) {
        this.ioChat = ioChatInterface;
    }

    public Set<User> getUsers() throws UndefinedConnectorException, IOException {
        return this.getStorageConnector().pullUsers();
    }

    public Set<Dialog> getDialogs() throws UndefinedConnectorException, IOException {
        return this.getStorageConnector().pullDialogs();
    }
}
