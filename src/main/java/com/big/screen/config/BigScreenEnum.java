package com.big.screen.config;

import com.big.screen.handler.HelloWorldHandler;
import com.big.screen.proto.HelloWorldBigScreenStruct;
import com.big.screen.util.AssertUtil;
import com.big.screen.util.HandlerProxyUtil;
import com.big.screen.util.JacksonUtil;
import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import com.google.protobuf.Message;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Getter
@Slf4j
public enum BigScreenEnum implements Serializable {

    HELLO_WORLD(
            "1",
            "HelloWorld",
            new HelloWorldHandler(),
            HelloWorldBigScreenStruct.HelloWorldBigScreen.getDefaultInstance(),
            new BigScreenArea().setName("area-1").setCron("0/1 * * * * ?"),
            new BigScreenArea().setName("area-2").setCron("0/5 * * * * ?")
    ),

    ;

    private String code;

    private String desc;

    private BigScreenHandler bigScreenHandler;

    private Message message;

    private BigScreenArea[] bigScreenAreas;

    private List<Channel> subscriberList;

    BigScreenEnum(String code, String desc, BigScreenHandler bigScreenDataHandler, Message message, BigScreenArea... bigScreenAreas) {
        AssertUtil.hasLength(desc, "desc不能为空");
        AssertUtil.notNull(bigScreenDataHandler, "bigScreenDataHandler不能为空");
        AssertUtil.notNull(message, "message不能为空");
        AssertUtil.notEmpty(bigScreenAreas, "bigScreenAreas不能为空");
        this.code = code;
        this.desc = desc;
        this.bigScreenAreas = bigScreenAreas;
        for (BigScreenArea bigScreenArea : bigScreenAreas) {
            bigScreenArea.setBigScreenEnum(this);
        }
        this.message = message;
        this.bigScreenHandler = HandlerProxyUtil.createHandlerProxy(bigScreenDataHandler);
        subscriberList = new CopyOnWriteArrayList<>();
    }

    public static BigScreenEnum getEnumByCode(String code) {
        for (BigScreenEnum value : BigScreenEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static String getSubscribeInfo() {
        Map<String, List<String>> infoMap = new HashMap<>();
        for (BigScreenEnum value : BigScreenEnum.values()) {
            String desc = value.getDesc();
            List<String> list = value.getSubscriberList().stream().map((channel) -> {
                return channel.id().asLongText();
            }).collect(Collectors.toList());
            infoMap.put(desc, list);
        }
        return JacksonUtil.toJson(infoMap);
    }

    public void publish(boolean clearSha256, List<Channel> channels, BigScreenArea... bigScreenAreas) {
        if (channels == null || channels.isEmpty()) {
            return;
        }
        for (BigScreenArea bigScreenArea : bigScreenAreas) {
            Message.Builder bigScreenDataBuilder = getMessage().newBuilderForType();
            getBigScreenHandler().loadBigScreenData(bigScreenArea, bigScreenDataBuilder);
            Message bigScreenData = bigScreenDataBuilder.build();
            String data = bigScreenData.toString();
            AttributeKey<String> sha256Key = AttributeKey.valueOf("sha256");
            for (Channel channel : channels) {
                if (clearSha256) {
                    channel.attr(sha256Key).set(null);
                }
                String oldSha256 = channel.attr(sha256Key).get();
                String newSha256 = sha256(data);
                if (Strings.isNullOrEmpty(oldSha256) || !oldSha256.equals(newSha256)) {
                    channel.attr(sha256Key).set(newSha256);
                    channel.writeAndFlush(bigScreenData);
                    log.info("【{}】-【{}】写入数据完成", getDesc() + "-" + bigScreenArea.getName(), channel.id().asLongText());
                }
            }
        }
    }

    public static void publish(BigScreenArea bigScreenArea) {
        BigScreenEnum bigScreenEnum = bigScreenArea.getBigScreenEnum();
        List<Channel> subscribers = bigScreenEnum.getSubscriberList();
        bigScreenEnum.publish(false, subscribers, bigScreenArea);
    }

    public synchronized void registerSubscriber(Channel channel) throws Exception {
        if (subscriberList.isEmpty()) {
            for (BigScreenArea bigScreenArea : getBigScreenAreas()) {
                bigScreenArea.startJob();
            }
        }
        subscriberList.add(channel);
    }

    public synchronized void deleteSubscriber(Channel channel) throws Exception {
        subscriberList.remove(channel);
        if (subscriberList.isEmpty()) {
            for (BigScreenArea bigScreenArea : getBigScreenAreas()) {
                bigScreenArea.removeJob();
            }
        }
    }

    public String sha256(String content) {
        return Hashing.sha256()
                .hashString(content, StandardCharsets.UTF_8)
                .toString();
    }

}
