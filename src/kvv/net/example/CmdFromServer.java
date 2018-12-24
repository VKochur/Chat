package kvv.net.example;

/**
 * Команды, посылаемые сервером клиенты
 */
public enum CmdFromServer {

    GET_ANSWER(0),
    TAKE_SMS(1);

    private final int cod;

    CmdFromServer(int cod){
        this.cod = cod;
    }

    public int getCod() {
        return cod;
    }
}
