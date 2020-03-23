import java.io.*;
import java.net.*;

/**
 * This thread is used to receive server response and print in to the console
 */
public class ReceiveThread extends Thread{
    private Socket socket;
    private BufferedReader buffer;
    private ClientApp client;

    public ReceiveThread(Socket socket, ClientApp client) {
        this.socket = socket;
        this.client = client;

        try {
            InputStream in = socket.getInputStream();
            buffer = new BufferedReader(new InputStreamReader(in));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
                String received = buffer.readLine();
                System.out.println(received);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
