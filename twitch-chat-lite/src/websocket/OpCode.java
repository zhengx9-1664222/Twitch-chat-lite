package websocket;

public class OpCode {

    public static int CONTINUATION = 0x0;

    public static int TEXT = 0x1;

    public static int BINARY = 0x2;

    public static int CLOSE = 0x8;

    public static int PING = 0x9;

    public static int PONG = 0xA;

}
