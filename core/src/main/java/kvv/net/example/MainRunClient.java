package kvv.net.example;

import kvv.net.ClientPack;

import java.io.IOException;
import java.util.Scanner;

public class MainRunClient {
    public static void main(String[] args) {
        System.out.println("запуск kvv.net.example.MainRunClient");
        final int CMD_EXIT = 100;
        ClientSms clientSms = new ClientSms();
        clientSms.configConnection("127.0.0.1", 8082);

        try {
            clientSms.startConnect();
        } catch (IOException e) {
            System.err.println("Нет соединения с сервером");
            return;
        }

        while (true){
            System.out.println("Ожидание команды: ");
            for (CmdFromClient cmd : CmdFromClient.values()) {
                System.out.println(cmd.getCod() + " : " +cmd);
            }
            System.out.println("Выход: " + CMD_EXIT);
            Scanner scanner = new Scanner(System.in);
            int cmd = scanner.nextInt();

            if (cmd == CmdFromClient.DISCONNECT.getCod()){
                ClientPack clientPack = new ClientPack(cmd);
                clientSms.sendPack(clientPack);
            }

            if (cmd == CmdFromClient.DISCONNECT_ALL.getCod()){
                ClientPack clientPack = new ClientPack(cmd);
                clientSms.sendPack(clientPack);
            }

            if (cmd == CmdFromClient.GET_LIST_ABONENT.getCod()){
                ClientPack clientPack = new ClientPack(cmd);
                clientSms.sendPack(clientPack);
            }

            if (cmd == CmdFromClient.SEND_SMS.getCod()){
                System.out.println("Введите ник кому:");
                String nik = new Scanner(System.in).nextLine();
                System.out.println("Введите sms:");
                String sms = new Scanner(System.in).nextLine();
                ClientPack clientPack = new ClientPack(cmd, nik, sms);
                clientSms.sendPack(clientPack);
            }

            if (cmd == CmdFromClient.SET_NIK.getCod()){
                System.out.println("Введите свой ник:");
                String nik = new Scanner(System.in).nextLine();
                ClientPack clientPack = new ClientPack(cmd, nik);
                clientSms.sendPack(clientPack);
            }
            if (cmd == CMD_EXIT){
                break;
            }
        }

        if (clientSms.isClientStarted()) {
            clientSms.stopConnect();
        }
    }
}
