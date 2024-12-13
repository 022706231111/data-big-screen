package com.big.screen.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.big.screen.config.ApplicationConfig;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.control.DeepClone;
import org.mapstruct.factory.Mappers;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.InputStream;

@Slf4j
public class YmlUtil {

    private static ApplicationConfig APPLICATION_CONFIG;

    public static void init() throws Exception {
        if (APPLICATION_CONFIG != null) {
            return;
        }
        Representer representer = new Representer(new DumperOptions());
        representer.getPropertyUtils().setSkipMissingProperties(true);
        Yaml yaml = new Yaml(new Constructor(ApplicationConfig.class, new LoaderOptions()), representer);
        InputStream inputStream = YmlUtil.class
                .getClassLoader()
                .getResourceAsStream("application.yml");
        ApplicationConfig commonConfig = yaml.load(inputStream);
        String active = commonConfig.getEnv().getActive();
        if (Strings.isNullOrEmpty(active)) {
            active = "test";
        }
        logbackHandle(active);
        log.info("The following 1 profile is active: {}", active);
        inputStream = YmlUtil.class
                .getClassLoader()
                .getResourceAsStream("application-" + active + ".yml");
        ApplicationConfig envConfig = yaml.load(inputStream);
        YmlMapStruct.INSTANCE.copy(envConfig, commonConfig);
        APPLICATION_CONFIG = commonConfig;
        log.info("{}", JacksonUtil.toJson(APPLICATION_CONFIG));
    }

    private static void logbackHandle(String active) throws JoranException {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();
        ClassLoader classLoader = YmlUtil.class.getClassLoader();
        configurator.doConfigure(classLoader.getResourceAsStream("logback-" + active + ".xml"));
    }

    public static ApplicationConfig getConfig() {
        return APPLICATION_CONFIG;
    }

    @Mapper(mappingControl = DeepClone.class, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public interface YmlMapStruct {

        YmlMapStruct INSTANCE = Mappers.getMapper(YmlMapStruct.class);

        void copy(ApplicationConfig src, @MappingTarget ApplicationConfig dest);

    }
}
