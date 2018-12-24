package kvv.education.khasang.java1.chat.model;

import kvv.education.khasang.java1.chat.model.basic_entity.Message;

/**
 * Интерфейс обеспечиваюший в чате ввод/вывод строк
 */
public interface IOChat {
    //получает строку
    String getPreparedString();

    //отправляет в устройство отображения сообщение
    void outMessageForSee(Message message, String autor);
}
