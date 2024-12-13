package com.big.screen.config;

import com.big.screen.util.AssertUtil;
import com.big.screen.util.YmlUtil;
import com.google.common.base.CharMatcher;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import static io.netty.buffer.Unpooled.wrappedBuffer;

public class BigScreenInitializer extends ChannelInitializer<SocketChannel> {

    private static final AttributeKey<BigScreenEnum> SCREEN_KEY = AttributeKey.valueOf("bigScreenEnum");

    private static final int READER_IDLE_TIME_SECONDS = 30;

    private static final int WRITER_IDLE_TIME_SECONDS = 0;

    private static final int ALL_IDLE_TIME_SECONDS = 0;

    public BigScreenInitializer() {
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new HttpObjectAggregator(64 * 1024));
        pipeline.addLast(new IdentityAuthHandler());
        pipeline.addLast(new WebSocketServerCompressionHandler());
        WebSocketServerProtocolHandler webSocketServerProtocolHandler = new WebSocketServerProtocolHandler(YmlUtil.getConfig().getServer().getPath(), null,
                true, 64 * 1024, false,
                true, 10000L);
        pipeline.addLast(webSocketServerProtocolHandler);
        pipeline.addLast(new WebSocketProtobufEncoder());
        pipeline.addLast(new IdleStateHandler(READER_IDLE_TIME_SECONDS, WRITER_IDLE_TIME_SECONDS, ALL_IDLE_TIME_SECONDS));
        pipeline.addLast(new WebSocketFrameHandler());

    }

    @Slf4j
    private static class IdentityAuthHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
            String uri = request.uri();
            log.info("【{}】", uri);
            Map<String, String> queryParameters = getQueryParameters(new URI(uri));
            String code = queryParameters.get("code");
            BigScreenEnum bigScreenEnum = BigScreenEnum.getEnumByCode(code);
            AssertUtil.notNull(bigScreenEnum, "未被定义的数据大屏");
            ctx.channel().attr(SCREEN_KEY).set(bigScreenEnum);
            ctx.fireChannelRead(request.retain());
        }

        private static Map<String, String> getQueryParameters(URI uri) {
            Map<String, String> queryParameters = new HashMap<>();
            String query = uri.getQuery();
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    queryParameters.put(pair.substring(0, idx), pair.substring(idx + 1));
                }
            }
            return queryParameters;
        }

    }

    @Slf4j
    private static class WebSocketFrameHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) throws Exception {
            BigScreenEnum bigScreenEnum = ctx.channel().attr(SCREEN_KEY).get();
            log.info("来自【{}】-【{}】的心跳检测", bigScreenEnum.getDesc(), ctx.channel().id().asLongText());
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete handshakeComplete) {
                subscribe(ctx);
            } else if (evt instanceof IdleStateEvent idleStateEvent) {
                BigScreenEnum bigScreenEnum = ctx.channel().attr(SCREEN_KEY).get();
                log.info("【{}】-【{}】-【{}秒】内未收到心跳检测", bigScreenEnum.getDesc(), ctx.channel().id().asLongText(), READER_IDLE_TIME_SECONDS);
                ctx.close();
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            unSubscribe(ctx);
            super.channelInactive(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }

        public void subscribe(ChannelHandlerContext ctx) throws Exception {
            Channel channel = ctx.channel();
            BigScreenEnum bigScreenEnum = channel.attr(SCREEN_KEY).get();
            bigScreenEnum.registerSubscriber(channel);
            ctx.executor().execute(() -> {
                List<Channel> channels = new ArrayList<>();
                channels.add(channel);
                bigScreenEnum.publish(true, channels, bigScreenEnum.getBigScreenAreas());
            });
            log.info("【{}】成功订阅【{}】", channel.id().asLongText(), bigScreenEnum.getDesc());
        }

        public void unSubscribe(ChannelHandlerContext ctx) throws Exception {
            Channel channel = ctx.channel();
            BigScreenEnum bigScreenEnum = channel.attr(SCREEN_KEY).get();
            bigScreenEnum.deleteSubscriber(channel);
            log.info("【{}】取消订阅【{}】", channel.id().asLongText(), bigScreenEnum.getDesc());
        }
    }

    @Slf4j
    private static class WebSocketProtobufEncoder extends ProtobufEncoder {
        @Override
        protected void encode(ChannelHandlerContext ctx, MessageLiteOrBuilder msg, List<Object> out) throws Exception {
            if (msg instanceof MessageLite) {
                out.add(new BinaryWebSocketFrame(wrappedBuffer(compress(((MessageLite) msg).toByteArray()))));
                return;
            }
            if (msg instanceof MessageLite.Builder) {
                out.add(new BinaryWebSocketFrame(wrappedBuffer(compress(((MessageLite.Builder) msg).build().toByteArray()))));
            }
        }

        private static byte[] compress(byte[] bytes) {
            byte[] compressByteArray = new byte[0];
            if (null == bytes || bytes.length == 0) {
                return new byte[0];
            }
            try (ByteArrayOutputStream out = new ByteArrayOutputStream(); GZIPOutputStream gzip = new GZIPOutputStream(out);) {
                gzip.write(bytes);
                gzip.close();
                compressByteArray = out.toByteArray();
            } catch (Exception e) {
                log.info("数据压缩异常", e);
            }
            return compressByteArray;
        }
    }
}
