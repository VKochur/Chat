package kvv.education.khasang.java1.chat.model;

/**
 * Исключение возникающее при попытке создать в хранилище информацию с неуникальными параметрами (которые должны быть уникальны)
 */
public class NonUniqueException extends Exception {
    public NonUniqueException(String message) {
        super(message);
    }
}
