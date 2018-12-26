package kvv.net.example;

/**
 * Команды посылаемые клиентом серверу
 */
public enum CmdFromClient {

    GET_LIST_ABONENT(0),
    SEND_SMS(1),
    DISCONNECT(2),
    DISCONNECT_ALL(3),
    SET_NIK(4);

    CmdFromClient(int cod){
        this.cod = cod;
    }

    public int getCod() {
        return cod;
    }

    int cod;
}
