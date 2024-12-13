package com.big.screen.config;

import lombok.Data;

@Data
public class ApplicationConfig {

    private Env env;

    private Server server;

    private DataSource dataSource;

    @Data
    public static class Env {
        private String active;
    }

    @Data
    public static class Server {
        private int port;
        private String path;
    }

    @Data
    public static class DataSource {
        private String url;
        private String username;
        private String password;
        private Integer initialSize;
        private Integer minIdle;
        private Integer maxActive;
        private Integer maxWait;
        private Integer timeBetweenEvictionRunsMillis;
        private Integer minEvictableIdleTimeMillis;
        private Integer maxEvictableIdleTimeMillis;
    }

}
