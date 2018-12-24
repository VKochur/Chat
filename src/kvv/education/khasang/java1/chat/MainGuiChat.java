package kvv.education.khasang.java1.chat;

import kvv.education.khasang.java1.chat.model.*;
import kvv.education.khasang.java1.chat.model.storage.omd.ConnectorOMDStorage;
import kvv.education.khasang.java1.chat.model.storage.omd.StorageOMD;
import kvv.education.khasang.java1.chat.views.gui.WindowControllerChat;
import kvv.education.khasang.java1.chat.views.gui.WindowViewChat;
import kvv.education.khasang.java1.chat.model.multithreading.Util;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Демонстрация чат с gui
 * Модель чата использует тип и расположение хранилища в зависимости от настроек указанных в pathSetting
 * Запускается несколько чатов в разных потоках для возможности выбрать в разных чатах разных пользователей и организовать беседу
 */
public class MainGuiChat {
    private static int x = 100;
    private static int y = 100;
    private static WindowViewChat windowViewChat;

    //файловое хранилище. уже с определенным наполнением пользователей, бесед
    //логины "Евгений" "Сергей" "Мария", пароли пустые: ""
    private static String pathSetting = "connection_settings1.txt";

    //файловое хранилище. новое. без пользователей, бесед.
    //private static String pathSetting = "connection_settings2.txt";

    //хранилище OMD. С восстановлением из файла дессериализацией. С определенным наполнением
    //логины "admin" "user" "kvv", пароли пустые: ""
    //private static String pathSetting = "connection_settings3.txt";

    //хранилище OMD. новое. без пользователей, бесед.
    //private static String pathSetting = "connection_settings4.txt";


    public static void main(String[] args) throws IllegalFormatParserException, IOException, ClassNotFoundException, InvocationTargetException, InterruptedException {
        int countChats = 3;
        startChats(countChats);
    }

    private static void startChats(int countChats) throws IOException, IllegalFormatParserException, ClassNotFoundException, InvocationTargetException, InterruptedException {
        ModelChat modelChat;
        StorageOMD storageOMD = null;

        //если в настройках указано хранилище OMD, обеспечим одно и тоже хранилище для всех чатов
        Parser parser = new Parser(pathSetting);
        if (parser.getStorageType() == StorageType.OMD) {
            //если тип хранилища OMD
            if (parser.getFileSerializationOmdPath() == null) {
                //если не указано из какого файла восстановить
                storageOMD = new StorageOMD();
            } else {
                //указано из какого файла восстановить
                storageOMD = StorageOMD.getInstance(parser.getFileSerializationOmdPath());
            }
        }

        for (int i = 0; i < countChats; i++) {
            if (storageOMD != null) {
                //если определено хранилище, значит тип соединения в настройках указан именно OMD, и нужное хранилище уже дано
                modelChat = new ModelChat();
                modelChat.setStorageConnector(new ConnectorOMDStorage(storageOMD));
            } else {
                //иначе инстанцируем модель с указанным типом соединения
                modelChat = ModelFactory.getInstance(parser);
            }

            //запустили вьюху
            runView();

            WindowControllerChat windowControllerChat = new WindowControllerChat();
            windowControllerChat.setModelChat(modelChat);
            windowControllerChat.setViewChat(windowViewChat);
            Thread thread = Util.startGuiChatInNewThread(windowControllerChat);
            windowViewChat.setTitleSuffix(thread.getName());
        }
    }

    private static void runView() throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                windowViewChat = new WindowViewChat();
                windowViewChat.setLocation(x += 20, y += 30);
            }
        });
    }
}
