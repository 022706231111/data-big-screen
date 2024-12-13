package com.big.screen.config;

import com.google.protobuf.Message;

public interface BigScreenHandler<A extends BigScreenArea, M extends Message.Builder> {

    public void loadBigScreenData(A bigScreenArea, M messageBuilder);
}
