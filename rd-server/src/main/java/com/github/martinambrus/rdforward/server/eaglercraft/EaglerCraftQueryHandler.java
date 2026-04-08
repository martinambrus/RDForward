package com.github.martinambrus.rdforward.server.eaglercraft;

import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import com.github.martinambrus.rdforward.server.PlayerManager;
import com.github.martinambrus.rdforward.server.api.ServerProperties;

import java.util.Collection;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * Handles EaglerCraft server list MOTD queries over WebSocket.
 *
 * EaglerCraft clients query the server list by connecting via WebSocket and
 * sending a text frame {@code "accept:motd"}. The server responds with a
 * JSON text frame containing server info, then closes the connection.
 *
 * This handler intercepts {@link TextWebSocketFrame} messages and responds
 * to MOTD queries. Binary frames pass through to the next handler
 * ({@link WebSocketFrameToByteBuf} → {@link EaglerCraftHandshakeHandler}).
 */
public class EaglerCraftQueryHandler extends ChannelInboundHandlerAdapter {

    private final PlayerManager playerManager;

    public EaglerCraftQueryHandler(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) msg;
            try {
                String text = textFrame.text().trim();
                // EaglerCraft clients may send "accept:motd", "Accept: MOTD", etc.
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
            super.channelRead(ctx, msg);
        }
    }

    /**
     * Escape a string for safe inclusion in a JSON string value.
     * Handles all characters required by RFC 7159.
     */
    private static String escapeJson(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b");  break;
                case '\f': sb.append("\\f");  break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    private void sendMotdResponse(ChannelHandlerContext ctx) {
        String motd = escapeJson(ServerProperties.getMotd());
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
            players.append("\"").append(escapeJson(p.getUsername())).append("\"");
            idx++;
        }
        if (total > 10) {
            players.append(",\"(").append(total - 10).append(" more)\"");
        }
        players.append("]");

        // EaglerCraft MOTD JSON response format
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
                    + "\"motd\":[\"" + motd + "\",\"EaglerCraft Compatible\"],"
                    + "\"icon\":false,"
                    + "\"online\":" + online + ","
                    + "\"max\":" + max + ","
                    + "\"players\":" + players.toString()
                + "}"
                + "}";

        ctx.writeAndFlush(new TextWebSocketFrame(json))
                .addListener(f -> ctx.close());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Query support is not implemented yet, so probes from EaglerCraft launchers and
        // server-list scanners frequently abort mid-handshake. Logging every aborted probe
        // (which used to print "[EaglerCraft Query] Exception: null" on every disconnect)
        // floods the log with no actionable information. Just close the channel quietly.
        ctx.close();
    }
}
