package com.big.screen.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.big.screen.util.YmlUtil;
import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;

import java.sql.SQLException;

public class MybatisDataSourceFactory extends PooledDataSourceFactory {

    public MybatisDataSourceFactory() throws SQLException {
        this.dataSource = BigScreenDataSource.getInstance();
    }

}
