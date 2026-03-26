package com.github.martinambrus.rdforward.server.lce;

import com.github.martinambrus.rdforward.server.PlayerManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Broadcasts LAN discovery packets for the LCE client.
 *
 * The LCE client discovers servers by listening for UDP broadcasts on port 25566.
 * This advertiser sends Win64LANBroadcast packets every 1 second matching the
 * format from WinsockNetLayer.cpp.
 *
 * Packed struct (84 bytes):
 *   DWORD  magic              (0x4D434C4E = "MCLN")
 *   WORD   netVersion         (560 for TU19)
 *   WORD   gamePort           (TCP port, default 25565)
 *   wchar_t hostName[32]      (64 bytes, UTF-16LE)
 *   BYTE   playerCount
 *   BYTE   maxPlayers
 *   DWORD  gameHostSettings
 *   DWORD  texturePackParentId
 *   BYTE   subTexturePackId
 *   BYTE   isJoinable         (1 = joinable)
 */
public class LCELanAdvertiser {

    private static final int LAN_DISCOVERY_PORT = 25566;
    private static final int BROADCAST_MAGIC = 0x4D434C4E; // "MCLN"
    private static final short NET_VERSION = 560;
    private static final int BROADCAST_INTERVAL_MS = 1000;
    private static final int PACKET_SIZE = 84;
    /** Offset of the playerCount byte within the broadcast packet. */
    private static final int PLAYER_COUNT_OFFSET = 4 + 2 + 2 + 64; // magic + netVersion + gamePort + hostName

    private final PlayerManager playerManager;

    private volatile boolean running = false;
    private Thread advertiseThread;
    private DatagramSocket socket;

    /** Pre-built broadcast packet template; only playerCount is updated per broadcast. */
    private final byte[] packetTemplate;

    public LCELanAdvertiser(int gamePort, String hostName, PlayerManager playerManager) {
        this.playerManager = playerManager;

        // Build the packet template once — only playerCount changes per broadcast
        ByteBuffer buf = ByteBuffer.allocate(PACKET_SIZE);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(BROADCAST_MAGIC);
        buf.putShort(NET_VERSION);
        buf.putShort((short) gamePort);

        byte[] nameBytes = hostName.getBytes(StandardCharsets.UTF_16LE);
        byte[] namePadded = new byte[64];
        System.arraycopy(nameBytes, 0, namePadded, 0, Math.min(nameBytes.length, 62));
        buf.put(namePadded);

        buf.put((byte) 0);  // playerCount (updated per broadcast)
        buf.put((byte) 8);  // maxPlayers
        buf.putInt(0);       // gameHostSettings
        buf.putInt(0);       // texturePackParentId
        buf.put((byte) 0);  // subTexturePackId
        buf.put((byte) 1);  // isJoinable

        packetTemplate = buf.array();
    }

    public void start() {
        if (running) return;

        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
        } catch (IOException e) {
            System.err.println("LCE LAN: Failed to create broadcast socket: " + e.getMessage());
            return;
        }

        running = true;

        advertiseThread = new Thread(this::broadcastLoop, "LCE-LAN-Advertiser");
        advertiseThread.setDaemon(true);
        advertiseThread.start();

        System.out.println("LCE LAN advertiser started on UDP port " + LAN_DISCOVERY_PORT);
    }

    public void stop() {
        running = false;
        if (socket != null) {
            socket.close();
        }
        if (advertiseThread != null) {
            advertiseThread.interrupt();
        }
    }

    private void broadcastLoop() {
        try {
            InetAddress broadcastAddr = InetAddress.getByName("255.255.255.255");

            while (running) {
                try {
                    packetTemplate[PLAYER_COUNT_OFFSET] = (byte) playerManager.getPlayerCount();
                    DatagramPacket dgram = new DatagramPacket(
                            packetTemplate, packetTemplate.length, broadcastAddr, LAN_DISCOVERY_PORT);
                    socket.send(dgram);
                } catch (IOException e) {
                    if (running) {
                        System.err.println("LCE LAN: Broadcast failed: " + e.getMessage());
                    }
                }

                Thread.sleep(BROADCAST_INTERVAL_MS);
            }
        } catch (InterruptedException e) {
            // Shutdown
        } catch (Exception e) {
            if (running) {
                System.err.println("LCE LAN: Advertiser error: " + e.getMessage());
            }
        }
    }
}
