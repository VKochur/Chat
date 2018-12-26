package kvv.education.khasang.java1.chat;

import kvv.education.khasang.java1.chat.model.IllegalFormatParserException;
import kvv.education.khasang.java1.chat.model.ModelFactory;
import kvv.education.khasang.java1.chat.model.Parser;
import kvv.education.khasang.java1.chat.views.console.ConsoleControllerChat;
import kvv.education.khasang.java1.chat.views.console.ConsoleViewChat;

import java.io.IOException;

/**
 * Консольное приложение чат
 * <p>
 * Используется модель-вьювер-контроллер
 * <p>
 * Модель реализует Chatable определяющий основной набор функций чата.
 * Chatable содержит StorageConnector, определяющий доступ к хранилищу данных
 * реализовано 2 доступа к различным типам хранилища:
 * ConnectorOMDStorage - доступ к хранилищу, которое является набором объектов в ОЗУ.
 * Для реализации данного доступа реализовано хранилище StorageOMD, информацию о котором содержит ConnectorOMDStorage
 * ConnectorFileStorage - доступ к хранилищу, которое является набором файлов на жестком диске
 * <p>
 * Реализация Chatable предусматривает реализацию метода получения подготовленной строки сообщения и метода вывода сообщения для пользователя.
 * Модель содержит интерфейс IOChat, посредством которого реализует методы получения строки и вывода сообщений для пользователя.
 * <p>
 * Реализация вьювер должна также реализовывать методы получения текста подготовленного сообщения и вывода сообщений для пользователя, поэтому вьювер наследует IOChat
 * <p>
 * Контроллер содержит информацию об используемой моделе и вьювере, и в момент установки модели или вьювера указывает для используемой модели в качестве интерфейса IOChat
 * используемый вьювер. Получается что модель и вьювер связаны, но связаны методом контроллера.
 */
public class MainConsoleChat {
    //путь к настройкам подключения, с которыми запускается чат
    private static String pathSettingFile = "connection_settings1.txt";

    public static void main(String[] args) throws IllegalFormatParserException {
        work(pathSettingFile);
    }

    /**
     * Демонстрация работы чата
     */
    private static void work(String pathSettingFile) throws IllegalFormatParserException {
        ConsoleControllerChat chat = new ConsoleControllerChat();
        try {
            Parser parser = new Parser(pathSettingFile);
            chat.setModel(ModelFactory.getInstance(parser));
        } catch (IOException e) {
            e.printStackTrace();
        }
        chat.setView(new ConsoleViewChat() {
        });
        chat.interactiveWork();
    }
}
