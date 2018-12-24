package kvv.net.example;

import java.io.IOException;

public class MainRunServer {
    public static void main(String[] args) {

        ServerSms serverSms = new ServerSms();
        serverSms.configServer(8082);

        try {
            serverSms.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }


        serverSms.stop();

    }
}
