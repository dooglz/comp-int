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
        boolean run = true;
        while (run) {
            String in = sysin.readLine();
            String[] splited = in.split("\\s+");
            Command c;
            switch (splited[0]) {
                case "exit":
                    s.stop();
                    run = false;
                    break;
                case "ping":
                    c = new Command(System.currentTimeMillis(), false, "Time", "", "");
                    if (splited.length > 1) {
                        s.sendTo(c, Integer.parseInt(splited[1]));
                    } else {
                        s.sendToAll(c);
                    }
                    break;
                case "cores":
                    c = new Command(System.currentTimeMillis(), false, "GetCores", "", "");
                    if (splited.length > 1) {
                        s.sendTo(c, Integer.parseInt(splited[1]));
                    } else {
                        s.sendToAll(c);
                    }
                    break;
                case "name":
                    c = new Command(System.currentTimeMillis(), false, "HostName", "", "");
                    if (splited.length > 1) {
                        s.sendTo(c, Integer.parseInt(splited[1]));
                    } else {
                        s.sendToAll(c);
                    }
                    break;
                case "list":
                    s.List();
                    break;
                default:
                    break;
            }
        }

    }
}
