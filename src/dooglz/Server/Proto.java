package dooglz.Server;
import dooglz.Command;
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
            }else{
                switch (c.command) {
                    default:
                        BParse(s, ws);
                        return;
                }
            }

        }
}
