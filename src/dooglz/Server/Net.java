package dooglz.Server;

import dooglz.*;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;


public class Net extends WebSocketServer {
    public Net(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));

    }

    public Net(InetSocketAddress address) {
        super(address);

    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conn.DEBUG = false;
        this.sendToAll("new connection: " + handshake.getResourceDescriptor());
        System.out.println(conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        this.sendToAll(conn + " has left the room!");
        System.out.println(conn + " has left the room!");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        //   this.sendToAll( message );
        System.out.println(conn + ": " + message);
        dooglz.Proto.Parse(message, conn);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    /**
     * Sends <var>text</var> to all currently connected WebSocket clients.
     *
     * @param text The String to send across the network.
     * @throws InterruptedException When socket related I/O errors occur.
     */
    public void sendToAll(String text) {
        Collection<WebSocket> con = connections();
        synchronized (con) {
            for (WebSocket c : con) {
                c.send(text);
            }
        }
    }

    public void sendTo(String text, int id) {
        if (id >= 0 && id < connections().size()) {
            Collection<WebSocket> con = connections();
            synchronized (con) {
                int i = 0;
                for (WebSocket c : con) {
                    if (i == id) {
                        c.send(text);
                        break;
                    }
                    ++i;
                }
            }
        }
    }

    public void List() {
        Collection<WebSocket> con = connections();
        synchronized (con) {
            int i = 0;
            for (WebSocket c : con) {
                System.out.println(i + " " + c.getRemoteSocketAddress());
                ++i;
            }
        }
    }

    public void sendToAll(Command c) {
        this.sendToAll(Proto.gson.toJson(c));
    }

    public void sendTo(Command c, int id) {
        this.sendTo(Proto.gson.toJson(c), id);
    }
}