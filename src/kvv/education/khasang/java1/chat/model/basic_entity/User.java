package kvv.education.khasang.java1.chat.model.basic_entity;

import java.io.Serializable;
import java.util.UUID;


/**
 * Представление об участнике чата.
 * Сведения об участнике чата хранятся в хранилище, доступ к которому осуществляется через коннектор.
 * Создание представлений участников должно осуществляться через StorageConnector.createUser
 */
public class User implements Comparable<User>, Serializable {

    private static final long serialVersionUID = 000000000001L;

    private int id;
    private UUID idStorage;
    private String login;

    public User(int id, UUID idStorage, String login) {
        this.id = id;
        this.idStorage = idStorage;
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public int getId() {
        return id;
    }

    public UUID getIdStorage() {
        return idStorage;
    }

    @Override
    public String toString() {
        return login;
    }

    @Override
    public int compareTo(User o) {
        return Integer.compare(this.id, o.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (getId() != user.getId()) return false;
        if (getIdStorage() != null ? !getIdStorage().equals(user.getIdStorage()) : user.getIdStorage() != null)
            return false;
        return getLogin() != null ? getLogin().equals(user.getLogin()) : user.getLogin() == null;
    }

    @Override
    public int hashCode() {
        int result = getId();
        result = 31 * result + (getIdStorage() != null ? getIdStorage().hashCode() : 0);
        result = 31 * result + (getLogin() != null ? getLogin().hashCode() : 0);
        return result;
    }
}
