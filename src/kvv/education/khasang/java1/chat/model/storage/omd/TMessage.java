package kvv.education.khasang.java1.chat.model.storage.omd;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class TMessage implements Serializable {

    private static final long serialVersionUID = 0000000000000000001L;

    private UUID idStorage;

    private Integer idAutor;
    private String text;
    private Date dateCreate;


    public TMessage(UUID idStorage, String text, Integer idAutor, Date dateCreate) {
        this.idStorage = idStorage;
        this.text = text;
        this.idAutor = idAutor;
        this.dateCreate = dateCreate;
    }

    public UUID getIdStorage() {
        return idStorage;
    }

    public Integer getIdAutor() {
        return idAutor;
    }

    public String getText() {
        return text;
    }

    public Date getDateCreate() {
        return dateCreate;
    }
}
