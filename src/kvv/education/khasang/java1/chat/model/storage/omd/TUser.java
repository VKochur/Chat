package kvv.education.khasang.java1.chat.model.storage.omd;

import java.io.Serializable;
import java.util.UUID;

public class TUser implements Serializable {
    private static final long serialVersionUID = 0000000000000000001L;

    UUID idStorage;

    public TUser(UUID idStorage, String login) {
        this.idStorage = idStorage;
        this.login = login;
    }

    private String login;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TUser)) return false;

        TUser tUser = (TUser) o;

        if (idStorage != tUser.idStorage) return false;
        return login != null ? login.equals(tUser.login) : tUser.login == null;
    }

    @Override
    public int hashCode() {
        int result = idStorage != null ? idStorage.hashCode() : 0;
        result = 31 * result + (getLogin() != null ? getLogin().hashCode() : 0);
        return result;
    }

    public String getLogin() {
        return login;
    }
}
