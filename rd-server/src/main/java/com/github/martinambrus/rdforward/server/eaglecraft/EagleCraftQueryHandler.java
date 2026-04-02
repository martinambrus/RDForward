package com.github.martinambrus.rdforward.server.eaglecraft;

import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import com.github.martinambrus.rdforward.server.PlayerManager;
import com.github.martinambrus.rdforward.server.api.ServerProperties;

import java.util.Collection;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * Handles EagleCraft server list MOTD queries over WebSocket.
 *
 * EagleCraft clients query the server list by connecting via WebSocket and
 * sending a text frame {@code "accept:motd"}. The server responds with a
 * JSON text frame containing server info, then closes the connection.
 *
 * This handler intercepts {@link TextWebSocketFrame} messages and responds
 * to MOTD queries. Binary frames pass through to the next handler
 * ({@link WebSocketFrameToByteBuf} → {@link EagleCraftHandshakeHandler}).
 */
public class EagleCraftQueryHandler extends ChannelInboundHandlerAdapter {

    private final PlayerManager playerManager;

    public EagleCraftQueryHandler(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("[EagleCraft Query] Received: " + msg.getClass().getSimpleName());
        if (msg instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) msg;
            try {
                String text = textFrame.text().trim();
                System.out.println("[EagleCraft Query] Text: " + text);
                // EagleCraft clients may send "accept:motd", "Accept: MOTD", etc.
                String lower = text.toLowerCase();
                if (lower.startsWith("accept:")) {
                    String queryType = lower.substring(7).trim();
                    if (queryType.equals("motd") || queryType.startsWith("motd.")) {
                        sendMotdResponse(ctx);
                        return;
                    }
                }
                // Unknown query — close
                ctx.close();
            } finally {
                textFrame.release();
            }
        } else {
            // Binary frames pass through to the handshake handler
            System.out.println("[EagleCraft Query] Passing through: " + msg.getClass().getSimpleName());
            super.channelRead(ctx, msg);
        }
    }

    private void sendMotdResponse(ChannelHandlerContext ctx) {
        try {
        System.err.println("[EagleCraft Query] sendMotdResponse entered");
        String motd = ServerProperties.getMotd().replace("\"", "\\\"");
        int online = playerManager.getPlayerCount();
        int max = PlayerManager.getMaxPlayers();

        // Build player list JSON array
        StringBuilder players = new StringBuilder("[");
        Collection<ConnectedPlayer> allPlayers = playerManager.getAllPlayers();
        int idx = 0;
        int total = allPlayers.size();
        for (ConnectedPlayer p : allPlayers) {
            if (idx >= 10) break;
            if (idx > 0) players.append(",");
            players.append("\"").append(p.getUsername().replace("\"", "\\\"")).append("\"");
            idx++;
        }
        if (total > 10) {
            players.append(",\"(").append(total - 10).append(" more)\"");
        }
        players.append("]");

        // EagleCraft MOTD JSON response format
        String json = "{"
                + "\"name\":\"RDForward\","
                + "\"brand\":\"RDForward\","
                + "\"vers\":\"0.2.0\","
                + "\"cracked\":true,"
                + "\"time\":" + System.currentTimeMillis() + ","
                + "\"uuid\":\"00000000-0000-0000-0000-000000000000\","
                + "\"type\":\"motd\","
                + "\"data\":{"
                    + "\"cache\":false,"
                    + "\"motd\":[\"" + motd + "\",\"EagleCraft 1.8\"],"
                    + "\"icon\":false,"
                    + "\"online\":" + online + ","
                    + "\"max\":" + max + ","
                    + "\"players\":" + players.toString()
                + "}"
                + "}";

        System.out.println("[EagleCraft Query] Sending MOTD response (" + json.length() + " chars)");
        System.out.println("[EagleCraft Query] Sending MOTD JSON");
        ctx.writeAndFlush(new TextWebSocketFrame(json))
                .addListener(f -> {
                    System.out.println("[EagleCraft Query] Write complete, success=" + f.isSuccess());
                    if (!f.isSuccess() && f.cause() != null) {
                        f.cause().printStackTrace();
                    }
                    ctx.close();
                });
        } catch (Exception e) {
            System.err.println("[EagleCraft Query] sendMotdResponse EXCEPTION: " + e);
            e.printStackTrace();
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("[EagleCraft Query] Exception: " + cause);
        cause.printStackTrace();
        ctx.close();
    }
}
