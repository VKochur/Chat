package kvv.education.khasang.java1.chat.model.basic_entity;

import java.io.Serializable;
import java.util.UUID;

/**
 * Представление диалога в чате. (Беседы)
 * Сведения о диалоге чата хранятся в хранилище, доступ к которому осуществляется через коннектор.
 * Создание представлений диалогов (бесед) должно осуществляться через StorageConnector.createDialog
 */
public class Dialog implements Comparable<Dialog>, Serializable {
    private static final long serialVersionUID = 000000000001L;

    private int id;
    private UUID idStorage;

    private String name;

    public Dialog(int id, UUID idStorage, String name) {
        this.id = id;
        this.idStorage = idStorage;
        this.name = name;
    }


    @Override
    public String toString() {
        return name;
    }

    public int getId() {
        return id;
    }

    public UUID getIdStorage() {
        return idStorage;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(Dialog o) {
        return Integer.compare(this.id, o.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dialog)) return false;

        Dialog dialog = (Dialog) o;

        if (getId() != dialog.getId()) return false;
        if (getIdStorage() != null ? !getIdStorage().equals(dialog.getIdStorage()) : dialog.getIdStorage() != null)
            return false;
        return getName() != null ? getName().equals(dialog.getName()) : dialog.getName() == null;
    }

    @Override
    public int hashCode() {
        int result = getId();
        result = 31 * result + (getIdStorage() != null ? getIdStorage().hashCode() : 0);
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        return result;
    }
}
