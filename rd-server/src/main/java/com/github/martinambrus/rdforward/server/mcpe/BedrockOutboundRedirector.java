package com.github.martinambrus.rdforward.server.mcpe;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;

/**
 * Intercepts outbound writes on CloudburstMC's internal NioDatagramChannel
 * and redirects them through the front-end channel (port 19132).
 *
 * Without this, CloudburstMC would try to send responses from its internal
 * loopback socket (127.0.0.1:ephemeral), which real clients can't receive.
 * By redirecting through the front-end, responses appear to come from
 * the public port 19132.
 */
public class BedrockOutboundRedirector extends ChannelOutboundHandlerAdapter {

    private final Channel frontEndChannel;

    public BedrockOutboundRedirector(Channel frontEndChannel) {
        this.frontEndChannel = frontEndChannel;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof DatagramPacket) {
            DatagramPacket pkt = (DatagramPacket) msg;
            // Redirect through the front-end channel so the client sees
            // the response coming from port 19132, not the internal loopback port.
            frontEndChannel.writeAndFlush(
                    new DatagramPacket(pkt.content().retain(), pkt.recipient())
            ).addListener(f -> {
                if (f.isSuccess()) {
                    promise.setSuccess();
                } else {
                    promise.setFailure(f.cause());
                }
            });
            pkt.release();
            return;
        }
        ctx.write(msg, promise);
    }
}
