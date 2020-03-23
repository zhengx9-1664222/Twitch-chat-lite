import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.net.*;
import java.io.*;

public class ClientApp {
    // the default port for the server to bind on
    private static int DEFAULT_PORT = 65533;

    private String hostName;
    private int portNum;
    private String sender;
    private String roomName;

    public ClientApp(String hostName, int portNum) {
        this.hostName = hostName;
        this.portNum = portNum;
    }

    public void execute() {
        try {
            Socket socket = new Socket(hostName, portNum);
            System.out.println("Twitch-Chat-Lite:");

            ReceiveThread received = new ReceiveThread(socket, this);
            SendThread sent = new SendThread(socket, this);

            received.start();
            sent.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getSender() {
        return sender;
    }

    public String getRoomName() {
        return roomName;
    }

    public static void main(String[] args) {
        int portNum  = DEFAULT_PORT;
        if (args.length < 1) {
            return;
        }

        if (args.length == 2) {
            try {
                portNum = Integer.parseInt(args[1]);
            } catch (Exception exp) {
                exp.printStackTrace();
                System.out.println("Can't parse the port you provided. Using default port 65533 instead.");
            }
        }

        String hostname = args[0];

        Socket socket = null;
        ClientApp client = new ClientApp(hostname, portNum);
        client.execute();
    }
}
