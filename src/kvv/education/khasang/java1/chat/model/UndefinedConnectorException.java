package kvv.education.khasang.java1.chat.model;

/**
 * Исключение возникающее при попытке работать с чатом, без указания коннектора для связи с хранилищем
 */
public class UndefinedConnectorException extends Exception {
    public UndefinedConnectorException() {
        super("Не определен доступ к хранилищу - не указан коннектор");
    }
}
