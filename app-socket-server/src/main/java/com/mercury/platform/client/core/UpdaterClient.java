package com.mercury.platform.client.core;

import com.mercury.platform.client.bus.UpdaterClientEventBus;
import com.mercury.platform.client.bus.handlers.UpdateEventHandler;
import com.mercury.platform.client.holder.VersionHolder;
import com.mercury.platform.client.init.ClientChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;

/**
 * Created by Frost on 14.01.2017.
 */
public class UpdaterClient {

    private static final Logger LOGGER = LogManager.getLogger(UpdaterClient.class);

    private final String host;
    private final int port;
    private volatile boolean stopped;

    public UpdaterClient(String host, String mercuryVersion, int port) {
        this.host = host;
        this.port = port;
        String version = mercuryVersion.replace(".", "0");
        VersionHolder.getInstance().setVersion(Integer.valueOf(version));
    }

    public void start() throws InterruptedException {
        this.stopped = false;
        while (!stopped) {
            EventLoopGroup group = null;
            try {
                group = new NioEventLoopGroup();
                LOGGER.info("Starting updater client");
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(group).channel(NioSocketChannel.class)
                        .remoteAddress(new InetSocketAddress(host, port))
                        .handler(new ClientChannelInitializer());
                LOGGER.info("Updater client was started");
                ChannelFuture channelFuture = bootstrap.connect().sync();
                channelFuture.channel().closeFuture().sync();
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            } finally {
                if (group != null)
                    group.shutdownGracefully().sync();
            }
            Thread.sleep(5000); //TIMEOUT 5 sec
        }
    }
    public void shutdown() {
        this.stopped = true;
    }
    public int getMercuryVersion() {
        return VersionHolder.getInstance().getVersion();
    }

    public void registerListener(UpdateEventHandler handler) {
        UpdaterClientEventBus.getInstance().register(handler);
    }

    public void removeListener(UpdateEventHandler handler) {
        UpdaterClientEventBus.getInstance().unregister(handler);
    }
}
