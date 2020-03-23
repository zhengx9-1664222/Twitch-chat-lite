package websocket;

import irc.IRCMessage;
import util.HTTPUtil;
import util.SocketUtil;

import java.io.*;
import java.net.Socket;

public class ChatWebSocket {

    /*
    Implements WebSocket protocol as explained:
    https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_WebSocket_servers
     */

    private static final String LOG_PREFIX = "[ClientWebSocket]: ";

    private Socket client;
    private OutputStream out;
    private InputStream in;

    private boolean log;

    public ChatWebSocket(Socket client) throws IOException {
        this(client, false);
    }

    public ChatWebSocket(Socket client, boolean log) throws IOException {
        this.client = client;
        this.out = this.client.getOutputStream();
        this.in = this.client.getInputStream();
        this.log = log;
        openHandshake();
    }

    public void send(IRCMessage message) throws IOException {
        DataFrame frame = new DataFrame(message);
        log("Sending message to: " + client.getLocalAddress());
        out.write(frame.getBytes());
        log("Sent message to client");
    }

    public byte[] receive() throws IOException {
        log("Listening for a data message from " + client.getLocalAddress());

        DataFrame frame;
        while (true) {
            frame = new DataFrame(in);

            if (frame.isPing()) {
                log("Received ping from client");
                out.write(DataFrame.CreatePong(frame).getBytes());
            } else if (frame.isClose()) {
                this.close();
                throw new IOException();
            } else if (frame.isPong()) {
                log("Received pong");
            } else if (frame.isContinuation()) {
                log("Received frame fragment");
            }

            return frame.getPayload();
        }
    }

    public void close() {
        closeHandshake();
        SocketUtil.bestEffortClose(client);
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    private void openHandshake() throws IOException {
        String header = HTTPUtil.parseHeader(this.in);
        if (HTTPUtil.isWebsocketRequest(header)) {
            String key = HTTPUtil.getWebSocketKey(header);
            String response = HTTPUtil.makeWebSocketAcceptKey(key);
            out.write(response.getBytes());
        }
    }

    private void closeHandshake() {
        DataFrame close = DataFrame.CreateClose();
        try {
            out.write(close.getBytes());
        } catch (IOException e) {
            // simply exit without sedning frame
        }
    }

    private void log(String message) {
        if (log) {
            System.out.println(LOG_PREFIX + message);
        }
    }
}
