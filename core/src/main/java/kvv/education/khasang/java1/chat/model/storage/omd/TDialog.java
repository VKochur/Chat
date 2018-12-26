package kvv.education.khasang.java1.chat.model.storage.omd;

import java.io.Serializable;
import java.util.UUID;

public class TDialog implements Serializable {
    private static final long serialVersionUID = 0000000000000000001L;

    UUID idStorage;

    private String name;

    public TDialog(UUID idStorage, String name) {
        this.idStorage = idStorage;
        this.name = name;
    }

    public UUID getIdStorage() {
        return idStorage;
    }

    public String getName() {
        return name;
    }
}
