package dooglz.Server;

import static dooglz.Constants.*;

import dooglz.Command;
import org.java_websocket.WebSocketImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Server {
    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("Hello World Server!");

        WebSocketImpl.DEBUG = true;
        int port = SERVER_PORT;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception ex) {
        }
        Net s = new Net(port);
        s.start();
        System.out.println("ChatServer started on port: " + s.getPort());

        BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String in = sysin.readLine();
            s.sendToAll(in);
            if (in.equals("exit")) {
                s.stop();
                break;
            } else if (in.equals("restart")) {
                s.stop();
                s.start();
                break;
            } else if (in.equals("Time")) {
                Command c = new Command(System.currentTimeMillis(), false, "Time", "", "");
                s.sendToAll(Proto.gson.toJson(c));
            } else if (in.equals("cores")) {
                Command c = new Command(System.currentTimeMillis(), false, "GetCores", "", "");
                s.sendToAll(Proto.gson.toJson(c));
            } else if (in.equals("HostName")) {
                Command c = new Command(System.currentTimeMillis(), false, "HostName", "", "");
                s.sendToAll(Proto.gson.toJson(c));
            }
        }

    }
}
