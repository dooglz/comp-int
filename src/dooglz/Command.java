package dooglz;

import org.java_websocket.WebSocket;

public class Command {
    public long sendTime;
    public boolean responce;
    public String command;
    public String send_data;
    public String old_data;
    WebSocket conn;
    public Command(long t, boolean b, String cmd, String dta, String odta) {
        this.sendTime = t;
        this.responce = b;
        this.command = cmd;
        this.send_data = dta;
        this.old_data = odta;
        conn = null;
    }
}