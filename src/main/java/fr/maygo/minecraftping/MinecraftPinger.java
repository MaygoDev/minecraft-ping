package fr.maygo.minecraftping;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MinecraftPinger {

    /**
     * Get server's json data with fully minecraft ping. The timeout is 7000 and the port is 25565.
     *
     *
     * @see <a href="https://wiki.vg/Server_List_Ping">Minecraft Server List Ping Wiki</a>
     * @param ip the server's ip
     * @return server's json data
     * @throws IOException When values are incorrect
     */
    public static String pingServer(String ip) throws IOException {
        return pingServer(ip, 25565);
    }

    /**
     * Get server's json data with fully minecraft ping. The timeout is 7000.
     *
     *
     * @see <a href="https://wiki.vg/Server_List_Ping">Minecraft Server List Ping Wiki</a>
     * @param ip the server's ip
     * @param port the server's port
     * @return server's json data
     * @throws IOException When values are incorrect
     */
    public static String pingServer(String ip, int port) throws IOException {
        return pingServer(ip, port, 7000);
    }

    /**
     * Get server's json data with fully minecraft ping
     *
     *
     * @see <a href="https://wiki.vg/Server_List_Ping">Minecraft Server List Ping Wiki</a>
     * @param ip the server's ip
     * @param port the server's port
     * @param timeout the socket timeout
     * @return server's json data
     * @throws IOException When values are incorrect
     */
    public static String pingServer(String ip, int port, int timeout) throws IOException {
        final Socket socket = new Socket();

        socket.setSoTimeout(timeout);
        socket.connect(new InetSocketAddress(ip, port), timeout);//7 seconds of timeout

        final DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        final DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final DataOutputStream handshakePacket = new DataOutputStream(byteArrayOutputStream);

        handshakePacket.writeByte(0x00);//Handshake packet id
        writeVarInt(handshakePacket, 47); //47 = 1.8

        writeVarInt(handshakePacket, ip.length()); //Ip length
        handshakePacket.writeBytes(ip); //Clear ip

        handshakePacket.writeShort(port); //Port
        writeVarInt(handshakePacket, 1); //State 1 = status

        //Send Handshake Packet
        writeVarInt(outputStream, byteArrayOutputStream.size()); //Packet size
        outputStream.write(byteArrayOutputStream.toByteArray()); //Packet data

        //Send Request Packet
        outputStream.writeByte(0x01); //Packet size = 1
        outputStream.writeByte(0x00); //Packet id = 0

        //Read response
        final int responseSize = readVarInt(inputStream); //Unused
        final int responsePacketId = readVarInt(inputStream);

        if (responsePacketId == -1) {
            throw new IOException("Premature end of stream.");
        }else if (responsePacketId != 0x00) { //we want a status response
            throw new IOException("Invalid packetID");
        }

        final int jsonLength = readVarInt(inputStream);

        if (jsonLength == -1) {
            throw new IOException("Premature end of stream.");
        }else if (jsonLength == 0) {
            throw new IOException("Invalid string length.");
        }

        final byte[] jsonData = new byte[jsonLength];
        inputStream.readFully(jsonData);
        final String json = new String(jsonData);

        inputStream.close();
        outputStream.close();
        socket.close();

        return json;
    }

    private static int readVarInt(DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;
        while (true) {
            int k = in.readByte();
            i |= (k & 0x7F) << j++ * 7;
            if (j > 5) throw new RuntimeException("VarInt too big");
            if ((k & 0x80) != 128) break;
        }
        return i;
    }

    private static void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
        while (true) {
            if ((paramInt & 0xFFFFFF80) == 0) {
                out.writeByte(paramInt);
                return;
            }

            out.writeByte(paramInt & 0x7F | 0x80);
            paramInt >>>= 7;
        }
    }

}
