package kvv.education.khasang.java1.chat.model.basic_entity;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Представление о сообщениии в чате
 * Сведения о сообщениях чата хранятся в хранилище, доступ к которому осуществляется через коннектор.
 * Создание представлений о сообщениях должно осуществляться через StorageConnector.createMessage
 */
public class Message implements Comparable<Message>, Serializable {
    private static final long serialVersionUID = 000000000001L;

    private int id;
    private UUID idStorage;

    private String text;
    private int idAuthor;
    private Date date;

    public Message(int id, UUID idStorage, String text, Integer idAuthor, Date dateCreate) {
        this.id = id;
        this.idStorage = idStorage;
        this.text = text;
        this.idAuthor = idAuthor;
        this.date = dateCreate;
    }

    public int getId() {
        return id;
    }

    public UUID getIdStorage() {
        return idStorage;
    }

    public String getText() {
        return text;
    }

    public int getIdAuthor() {
        return idAuthor;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public int compareTo(Message o) {
        return Integer.compare(this.id, o.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;

        Message message = (Message) o;

        if (getId() != message.getId()) return false;
        if (getIdAuthor() != message.getIdAuthor()) return false;
        if (getIdStorage() != null ? !getIdStorage().equals(message.getIdStorage()) : message.getIdStorage() != null)
            return false;
        if (getText() != null ? !getText().equals(message.getText()) : message.getText() != null) return false;
        return getDate() != null ? getDate().equals(message.getDate()) : message.getDate() == null;
    }

    @Override
    public int hashCode() {
        int result = getId();
        result = 31 * result + (getIdStorage() != null ? getIdStorage().hashCode() : 0);
        result = 31 * result + (getText() != null ? getText().hashCode() : 0);
        result = 31 * result + getIdAuthor();
        result = 31 * result + (getDate() != null ? getDate().hashCode() : 0);
        return result;
    }
}
