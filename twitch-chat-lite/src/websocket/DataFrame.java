package websocket;

import irc.IRCMessage;

import javax.xml.crypto.Data;
import java.io.*;
import java.nio.ByteBuffer;

class DataFrame {

    private boolean fin;
    private long len;
    private int opcode;
    private byte[] payload;

    public DataFrame() {
        this.fin = true;
        this.len = 0;
        this.opcode = OpCode.TEXT;
        this.payload = null;
    }

    public DataFrame(IRCMessage message) {
        this.fin = true;
        this.payload = message.getBytes();
        this.len = payload.length;
        this.opcode = OpCode.TEXT;
    }

    public DataFrame(InputStream in) throws IOException {
        // TODO add fragmentation support

        DataInputStream stream = new DataInputStream(in);
        int header = stream.readUnsignedShort();

        this.fin = ((1 << 15) & header) == (1 << 15);
        this.opcode = (header >> 8) & ~(~0 << 4);
        boolean encrypted = ((1 << 8) & header) == (1 << 8);
        this.len = header & ~(~0 << 7);

        if (len == 126) {
            this.len = stream.readUnsignedShort();
        } else if (len == 127) {
            this.len = stream.readLong();
        }

        if (encrypted) {
            byte[] mask = stream.readNBytes(4);
            payload = stream.readNBytes((int) len);
            decrypt(mask, payload);
        } else {
            payload = stream.readNBytes((int) len);
        }
    }

    public DataFrame copy() {
        DataFrame frame = new DataFrame();
        frame.fin = this.fin;
        frame.len = this.len;
        frame. opcode = this.opcode;
        frame.payload = this.payload.clone();
        return frame;
    }

    public static DataFrame CreatePong(DataFrame ping) {
        DataFrame pong = ping.copy();
        pong.opcode = OpCode.PONG;
        return pong;
    }

    public static DataFrame CreateClose() {
        DataFrame close = new DataFrame();
        close.opcode = OpCode.CLOSE;
        return close;
    }

    public void send(OutputStream out) throws IOException {
        out.write(this.getBytes());
    }

    public boolean isText() {
        return opcode == OpCode.TEXT;
    }

    public boolean isBinary() {
        return opcode == OpCode.BINARY;
    }

    public boolean isPing() {
        return opcode == OpCode.PING;
    }

    public boolean isPong() {
        return opcode == OpCode.PONG;
    }

    public boolean isContinuation() {
        return opcode == OpCode.CONTINUATION;
    }

    public boolean isClose() {
        return opcode == OpCode.CLOSE;
    }

    public byte[] getBytes() {
        ByteBuffer buf;
        if (len <= 125) {
            buf = ByteBuffer.allocate(2 + (int)len);
        } else {
            buf = ByteBuffer.allocate(4 + (int)len);
        }
        addHeader(buf);
        addPayload(buf);
        return buf.array();
    }

    public byte[] getPayload() {
        return payload.clone();
    }

    private void decrypt(byte[] mask, byte[] payload) {
        for (var i = 0; i < payload.length; i++) {
            payload[i] = (byte) (payload[i] ^ mask[i % 4]);
        }
    }

    private void addHeader(ByteBuffer buffer) {
        int header = 0;
        // add fin bit
        header = (header + 1) << 15;
        // add opcode
        header = header | (OpCode.TEXT << 8);

        if (len <= 125) {
            header = header | ((int)len);
            buffer.putShort((short)header);
        } else {
            header = header | 126;
            header = header << 16;
            header |= (int) len;
            buffer.putInt(header);
        }
    }

    private void addPayload(ByteBuffer buffer) {
        if (payload == null)
            return;
        buffer.put(payload);
    }
}
