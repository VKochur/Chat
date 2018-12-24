package kvv.net;

import java.io.Serializable;
import java.util.UUID;

/**
 * Пакет данных передаваемый между клиентом и сервером
 */
public class Pack implements Serializable {
    /**
     * Уникальный номер пакета
     */
    UUID uuid;

    /**
     *Код команды, который интерпретируется анализируется сервером или клиентом для определения дальнейших действий
     */
    int cod;

    /**
     * Контекст команды, вспомагательная информация
     * Элементы в контексте должны быть Serializable
     */
    Object[] context;

    public Pack(int cod, Object... context){
        this.uuid = UUID.randomUUID();
        this.cod = cod;
        this.context = context;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getCod() {
        return cod;
    }

    public Object[] getContext() {
        return context;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder().append("{uuid="+String.valueOf(uuid)+";"+"cod="+String.valueOf(cod));
        for (Object o : context) {
            stringBuilder.append(";"+o.toString()+"}");
        }
        return stringBuilder.toString();
    }
}
