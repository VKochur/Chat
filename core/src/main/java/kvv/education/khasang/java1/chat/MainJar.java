package kvv.education.khasang.java1.chat;

import kvv.education.khasang.java1.chat.model.ModelFactory;
import kvv.education.khasang.java1.chat.views.gui.WindowViewChat;
import kvv.education.khasang.java1.chat.model.IllegalFormatParserException;
import kvv.education.khasang.java1.chat.model.ModelChat;
import kvv.education.khasang.java1.chat.model.Parser;
import kvv.education.khasang.java1.chat.views.gui.WindowControllerChat;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Чат c GUI
 * Main class для создания запускаемого jar
 * args[0] - файл с настройками соединения
 */
public class MainJar {
    private static WindowViewChat windowViewChat;

    public static void main(String[] args) {
        String pathSettings = args[0];
        Parser parser = null;
        try {
            parser = new Parser(pathSettings);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Ошибка инициализации парсера");
            System.exit(1);
        }
        ModelChat modelChat = null;
        try {
            modelChat = ModelFactory.getInstance(parser);
        } catch (IllegalFormatParserException e) {
            e.printStackTrace();
            System.out.println("Ошибка построение модели (некорректные сведения в парсере)");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Ошибка построение модели");
            System.exit(1);
        }
        runView();
        WindowControllerChat controllerChat = new WindowControllerChat();
        controllerChat.setViewChat(windowViewChat);
        controllerChat.setModelChat(modelChat);
        controllerChat.start();
    }

    private static void runView() {
        try {
            SwingUtilities.invokeAndWait(() -> windowViewChat = new WindowViewChat());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
