package com.big.screen;

import com.big.screen.config.BigScreenDataSource;
import com.big.screen.config.BigScreenInitializer;
import com.big.screen.util.MyBatisUtil;
import com.big.screen.util.QuartzUtil;
import com.big.screen.util.YmlUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public final class BigScreenApplication {

    public static void main(String[] args) throws Exception {
        System.setProperty("user.timezone", "GMT+8");
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            YmlUtil.init();
            MyBatisUtil.init();
            QuartzUtil.init();
            int port = YmlUtil.getConfig().getServer().getPort();
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new BigScreenInitializer());
            Channel ch = b.bind(port).sync().channel();
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            BigScreenDataSource.close();
        }
    }
}
