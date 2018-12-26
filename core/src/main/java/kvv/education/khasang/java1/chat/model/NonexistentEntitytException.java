package kvv.education.khasang.java1.chat.model;

/**
 * Исключение возникающее при попытке произвести действия над объектами отсутствующими в хранилище
 */
public class NonexistentEntitytException extends Exception {

    public NonexistentEntitytException(String message) {
        super(message);
    }
}
