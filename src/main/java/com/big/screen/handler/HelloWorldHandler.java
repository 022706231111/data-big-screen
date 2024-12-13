package com.big.screen.handler;

import com.big.screen.config.AbstractBigScreenHandler;
import com.big.screen.config.BigScreenArea;
import com.big.screen.entity.City;
import com.big.screen.mapper.CityMapper;
import com.big.screen.proto.HelloWorldBigScreenStruct;
import com.big.screen.util.JacksonUtil;
import com.big.screen.util.MyBatisUtil;

import java.util.List;

public class HelloWorldHandler extends AbstractBigScreenHandler<BigScreenArea, HelloWorldBigScreenStruct.HelloWorldBigScreen.Builder> {

    private CityMapper cityMapper = MyBatisUtil.getMapper(CityMapper.class);

    @Override
    protected void doLoadBigScreenData(BigScreenArea bigScreenArea, HelloWorldBigScreenStruct.HelloWorldBigScreen.Builder messageBuilder) {
        if (bigScreenArea.getName().equals("area-1")) {
            List<City> cities = cityMapper.list10();
            messageBuilder.setContent(JacksonUtil.toJson(cities));
        } else {
            messageBuilder.setContent("HelloWorld, area-2");
        }
    }

}
