package kvv.net.example;

import kvv.net.Client;
import kvv.net.ServerPack;

import java.util.List;
import java.util.UUID;

/**
 * Клиент в схеме отправки смс сообщений
 */
public class ClientSms extends Client {
    @Override
    public void processingPack(ServerPack serverPack) {
        Integer cod = serverPack.getCod();
        Object[] context = serverPack.getContext();
        if (cod == CmdFromServer.TAKE_SMS.getCod()){
            String fromNik = (String) context[0];
            String sms = (String) context[1];
            System.out.println("Получено сообщение: '" + sms + "' от '" + fromNik+"'");
        }
        if (cod == CmdFromServer.GET_ANSWER.getCod()){
            UUID uuidQuestion = (UUID) context[0];
            List<String> abonentsList = (List<String>) context[1];
            System.out.println("Получен ответ: " + abonentsList.toString() + " на вопрос " + uuidQuestion);
        }
    }
}
