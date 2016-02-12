package dooglz;


import com.google.gson.Gson;
import dooglz.Server.Net;
import org.java_websocket.WebSocket;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class Proto {
    public static Gson gson = new Gson();

    public static String Cmd_GetCores() {
        return gson.toJson(Runtime.getRuntime().availableProcessors());
    }

    public static String Cmd_Time() {
        return gson.toJson(System.currentTimeMillis());
    }

    public static String Cmd_HostName() {
        try {
            return gson.toJson(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            return gson.toJson("Hostname");
        }
    }

    public static void BParse(String s, WebSocket ws) {
        Command c;
        try {
            c = gson.fromJson(s, Command.class);
        } catch (com.google.gson.JsonSyntaxException ex) {
            return;
        }

        if (c.responce) {
            //peer is responding to us
            switch (c.command) {
                case "GetCores":
                    System.out.println("Peer reported cores as " + gson.fromJson(c.send_data, int.class));
                    break;
                case "HostName":
                    System.out.println("Peer reported Hosntame as " + gson.fromJson(c.send_data, String.class));
                    break;
                case "Time":
                    System.out.println("Peer reported Time as " + gson.fromJson(c.send_data, long.class));
                    break;
                default:
                    return;
            }

        } else {
            //peer is asking something from us
            String resp = "";
            switch (c.command) {
                case "GetCores":
                    resp = Cmd_GetCores();
                    break;
                case "HostName":
                    resp = Cmd_HostName();
                    break;
                case "Time":
                    resp = Cmd_Time();
                    break;
                default:
                    return;
            }
            Command cr = new Command(System.currentTimeMillis(), true, c.command, resp, c.send_data);
            Send(cr, ws);
        }
    }

    public static void Send(Command c, WebSocket ws) {
        System.out.println("Send:\n " + gson.toJson(c));
        ws.send(gson.toJson(c));
    }
}
