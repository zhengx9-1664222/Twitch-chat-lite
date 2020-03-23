package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;

public class HTTPUtil {

    private static final String MAGIC_STRING = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private static final String WEBSOCKET_KEY = "Sec-WebSocket-Key: ";

    public static String parseHeader(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = "";
        StringBuilder sb = new StringBuilder();
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            sb.append(line).append("\r\n");
        }
        return sb.toString();
    }

    public static boolean isWebsocketRequest(String header) {
        return header.contains("Connection: Upgrade") && header.contains("Upgrade: websocket");
    }

    public static String getWebSocketKey(String header) {
        Scanner scanner = new Scanner(header);
        String line = "";
        while (scanner.hasNext()) {
            line = scanner.nextLine();
            if (line.contains(WEBSOCKET_KEY)) {
                return line.replace(WEBSOCKET_KEY, "").replace("\r\n", "");
            }
        }
        throw new IllegalStateException();
    }

    public static String makeWebSocketAcceptKey(String key) {
        key += MAGIC_STRING;
        byte[] hash = sha1(key);
        byte[] accept = Base64.getEncoder().encode(hash);

        return "HTTP/1.1 101 Switching Protocols\r\n" +
                "Upgrade: websocket\r\n" +
                "Connection: Upgrade\r\n" +
                "Sec-WebSocket-Accept: " + new String(accept) + "\r\n\r\n";
    }

    private static byte[] sha1(String text) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException();
        }
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        md.update(textBytes, 0, textBytes.length);
        return md.digest();
    }
}
