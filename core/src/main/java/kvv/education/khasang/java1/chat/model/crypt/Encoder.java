package kvv.education.khasang.java1.chat.model.crypt;

public interface Encoder {
    /**
     * Шифратор паролей
     *
     * @param value алгоритм шифрования в общем случае может использовать несколько входных данных (пароль, логин-пароль, пароль-логин-соль)
     * @return
     */
    String encode(String... value);
}
