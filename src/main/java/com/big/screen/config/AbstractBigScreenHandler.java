package com.big.screen.config;

import com.google.common.base.Stopwatch;
import com.google.protobuf.Message;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public abstract class AbstractBigScreenHandler<A extends BigScreenArea, M extends Message.Builder> implements BigScreenHandler<A, M> {

    @Override
    public void loadBigScreenData(A bigScreenArea, M messageBuilder) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        doLoadBigScreenData(bigScreenArea, messageBuilder);
        stopwatch.stop();
        log.info("【{}】-【{}毫秒】", bigScreenArea.getBigScreenEnum().getDesc() + "-" + bigScreenArea.getName(), stopwatch.elapsed().toMillis());
    }

    protected abstract void doLoadBigScreenData(A bigScreenArea, M messageBuilder);

}
