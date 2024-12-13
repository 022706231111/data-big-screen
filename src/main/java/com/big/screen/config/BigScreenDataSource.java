package com.big.screen.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.big.screen.util.YmlUtil;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Slf4j
public class BigScreenDataSource implements DataSource {

    private DruidDataSource druidDataSource;

    private BigScreenDataSource() {

    }

    @Override
    public Connection getConnection() throws SQLException {
        return druidDataSource.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return druidDataSource.getConnection(username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    private static class SingletonHolder {

        private static final BigScreenDataSource INSTANCE = new BigScreenDataSource();

        static {
            ApplicationConfig applicationConfig = YmlUtil.getConfig();
            ApplicationConfig.DataSource dataSourceConfig = applicationConfig.getDataSource();
            DruidDataSource druidDataSource = new DruidDataSource();
            druidDataSource.setUrl(dataSourceConfig.getUrl());
            druidDataSource.setUsername(dataSourceConfig.getUsername());
            druidDataSource.setPassword(dataSourceConfig.getPassword());
            druidDataSource.setInitialSize(dataSourceConfig.getInitialSize());
            druidDataSource.setMinIdle(dataSourceConfig.getMinIdle());
            druidDataSource.setMaxActive(dataSourceConfig.getMaxActive());
            druidDataSource.setMaxWait(dataSourceConfig.getMaxWait());
            druidDataSource.setTimeBetweenEvictionRunsMillis(dataSourceConfig.getTimeBetweenEvictionRunsMillis());
            druidDataSource.setMinEvictableIdleTimeMillis(dataSourceConfig.getMinEvictableIdleTimeMillis());
            druidDataSource.setMaxEvictableIdleTimeMillis(dataSourceConfig.getMaxEvictableIdleTimeMillis());
            druidDataSource.setValidationQuery("SELECT 1 FROM DUAL");
            druidDataSource.setTestWhileIdle(true);
            druidDataSource.setTestOnBorrow(false);
            druidDataSource.setTestOnReturn(false);
            druidDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            INSTANCE.druidDataSource = druidDataSource;
        }

    }

    public static BigScreenDataSource getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public static void close() {
        if (getInstance().druidDataSource != null) {
            getInstance().druidDataSource.close();
        }
    }

}
