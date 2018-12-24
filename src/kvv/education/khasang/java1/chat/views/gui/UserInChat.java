package kvv.education.khasang.java1.chat.views.gui;

import kvv.education.khasang.java1.chat.model.basic_entity.User;

import java.io.Serializable;

class UserInChat implements Serializable {
    private User user;
    private UserStatus status;

    UserInChat(User user, UserStatus status) {
        this.user = user;
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public UserStatus getStatus() {
        return status;
    }
}
