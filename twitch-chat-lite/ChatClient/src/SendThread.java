import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * This thread is used to read users' input, parse it and send it to the server
 * in an IRC Message
 */
public class SendThread extends Thread{
    private Socket socket;
    private ClientApp client;
    private OutputStream out;

    public SendThread(Socket socket, ClientApp client) {
        this.socket = socket;
        this.client = client;

        try {
            out = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        Scanner in = new Scanner(System.in);
        // ask user for their username
        System.out.print("Enter your name: ");
        String userName = in.nextLine();
        client.setSender(userName);

        while (true) {
            // ask user which channel they want to join
            System.out.print("Join channel: ");
            String roomName = in.nextLine();

            client.setRoomName(roomName);

            String content = "";
            do {
                content = in.nextLine();
                IRCMessage message = new IRCMessage(roomName, userName, content);
                try {
                    out.write(message.getBytes());
                } catch (Exception e) {
                    try {
                        socket.close();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }
            } while (!content.equals("/leave"));

            IRCMessage leaveMessage = new IRCMessage(roomName, userName, "/leave");
        }
    }
}
