package dooglz.Server;

import dooglz.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;

public class CmdLine implements Runnable {
    public Net s;
    private Thread selectorthread;

    public void start() {
        if(this.selectorthread != null) {
            throw new IllegalStateException(this.getClass().getName() + " can only be started once.");
        } else {
            (new Thread(this)).start();
        }
    }

    public void run() {
        synchronized(this) {
            if(this.selectorthread != null) {
                throw new IllegalStateException(this.getClass().getName() + " can only be started once.");
            }
            this.selectorthread = Thread.currentThread();
        }

        this.selectorthread.setName("WebsocketSelector" + this.selectorthread.getId());

        System.out.println(Thread.currentThread());
        BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
        boolean run = true;
        while (run) {
            String in = null;
            try {
                in = sysin.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String[] splited = in.split("\\s+");
            Command c;
            switch (splited[0]) {
                case "exit":
                    try {
                        s.stop();
                    } catch (Exception  e) {
                        e.printStackTrace();
                    }
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
                case "status":
                    System.out.println(Server.state);
                    break;
                case "load":
                    if(Server.state != Server.States.Lobby){
                        System.out.println("Server is busy");
                        break;
                    }
                    Server.Load(86);
                    break;
                case "stop":
                    if(Server.state == Server.States.Lobby){;
                        break;
                    }
                    Server.Stop();
                    break;
                default:
                    break;
            }
        }

    }

}
