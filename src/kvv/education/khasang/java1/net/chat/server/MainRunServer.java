package kvv.education.khasang.java1.net.chat.server;


import kvv.education.khasang.java1.chat.model.Parser;

import java.io.*;
import java.util.Scanner;

/**
 * Главный класс для запуска сервера чата
 * В качестве args:
 * args[0] - номер порта
 * <p>
 * Если args.length < 1 используются умолчательный 8082
 * <p>
 * настройки подключения к хранилищу должны быть указаны в файле "connection_settings.txt" находящимся в папке с исполняемым файлом
 * перечень запрещенных слов и выражений в чате в файле "taboo utf-8.txt" (кодировка д.б. utf-8) находящимся в папке с исполняемым файлом
 *
 * Изначально созданы пользователи "Николай", "Евгений" пароли пустые ""
 */
public class MainRunServer {

    private static final Integer DEFAULT_PORT = 8082;
    private static final String SETTING = "connection_settings.txt";
    private static final String PATH_TABOO_WORDS = "taboo utf-8.txt";

    public static void main(String[] args) {
        System.out.println("Запуск сервера");
        Integer port = DEFAULT_PORT;
        if (args.length > 0) {
            port = Integer.valueOf(args[0]);
        }

        ChatServer server = new ChatServer();
        server.configServer(port);
        try {
            server.setParser(new Parser(SETTING));
            fillTabooWords(server);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new IllegalStateException("Ошибка при построении парсера, возможно отсутствует файл настроек");
        }
        try {
            server.startServer();
        } catch (IOException e) {
            System.err.println("Сервер не удалось запустить");
            e.printStackTrace();
        }

        stop(server);
    }

    private static void fillTabooWords(ChatServer server) {
        try (Scanner scanner = new Scanner(new InputStreamReader(new FileInputStream(PATH_TABOO_WORDS), "UTF-8"))) {
            String temp;
            boolean firstWord = true;
            while ((scanner.hasNext())) {
                temp = scanner.nextLine();
                if (firstWord) {
                    firstWord = false;
                    //почему-то в 1й строчке при прочтении в начале стоит символ '.' (хотя в файле его нет), избавляемся от него
                    temp = temp.substring(1);
                }
                server.addWordToTaboo(temp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void stop(ChatServer server) {
        System.out.println("ДЛЯ ОСТАНОВКИ СЕРВЕРА, нажмите любую клавишу");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.stop();
    }
}
