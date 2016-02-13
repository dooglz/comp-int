package dooglz.Client;

import dooglz.Command;
import modelP.Problem;
import org.java_websocket.WebSocket;

public class Proto extends dooglz.Proto {
    public static void Parse(String s, WebSocket ws) {
        Command c;
        try {
            c = gson.fromJson(s, Command.class);
        } catch (com.google.gson.JsonSyntaxException ex) {
            return;
        }
        if (c.responce) {
            switch (c.command) {
                default:
                    BParse(s, ws);
                    return;
            }
        } else {
            switch (c.command) {
                case "NewProblem":
                    synchronized (Client.problem) {
                        Client.problem = gson.fromJson(c.send_data, Problem.class);
                    }
                    break;
                case "NewPopulation":
                    synchronized (Client.population) {
                        Client.population = gson.fromJson(c.send_data, int[][][].class);
                    }
                    break;
                case "Start":
                    Client.Start();
                    break;
                default:
                    BParse(s, ws);
                    return;
            }
        }

    }
}
