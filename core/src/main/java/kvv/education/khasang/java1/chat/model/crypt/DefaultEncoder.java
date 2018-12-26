package kvv.education.khasang.java1.chat.model.crypt;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Шифратор паролей используемый по умолчанию
 * В качестве аргумента для шифрования используется только пароль
 */
public class DefaultEncoder implements Encoder {
    @Override
    public String encode(String... value) {
        // + salt
        return DigestUtils.md5Hex(value[0] + DigestUtils.md5Hex(value[0]));
    }
}
