package kvv.education.khasang.java1.chat.model.multithreading;

import kvv.education.khasang.java1.chat.views.gui.WindowControllerChat;

public class Util {
    public static Thread startGuiChatInNewThread(WindowControllerChat windowControllerChat) {
        if (windowControllerChat.getViewChat() == null) {
            throw new IllegalStateException("Для запуска чата контролер должен быть связан с WindowViewChat");
        }
        if (windowControllerChat.getModelChat() == null) {
            throw new IllegalStateException("Для запуска чата контролер должен быть связан с ModelChat");
        }
        Thread thread = new Thread(() -> windowControllerChat.start());
        thread.start();
        return thread;
    }
}
