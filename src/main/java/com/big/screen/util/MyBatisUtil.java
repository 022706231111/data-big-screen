package com.big.screen.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.*;

import java.io.Reader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static java.lang.reflect.Proxy.newProxyInstance;

@Slf4j
public class MyBatisUtil {

    private static SqlSessionFactory sqlSessionFactory;

    public static void init() throws Exception {
        if (sqlSessionFactory != null) {
            return;
        }
        Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    public static <T> T getMapper(Class<T> mapperClass) {
        return new SqlSessionTemplate(sqlSessionFactory).getMapper(mapperClass);
    }

    public static SqlSession closeSqlSession() {
        SqlSession sqlSession = SqlSessionTemplate.SQL_SESSION_HOLDER.get();
        if (sqlSession != null) {
            sqlSession.close();
            SqlSessionTemplate.SQL_SESSION_HOLDER.remove();
        }
        return sqlSession;
    }

    public static class SqlSessionTemplate implements SqlSession {

        private SqlSessionFactory sqlSessionFactory;

        private ExecutorType executorType;

        private SqlSession sqlSessionProxy;

        private static final ThreadLocal<SqlSession> SQL_SESSION_HOLDER = new ThreadLocal<>();

        public SqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
            AssertUtil.notNull(sqlSessionFactory, "Property 'sqlSessionFactory' is required");
            this.sqlSessionFactory = sqlSessionFactory;
            this.executorType = sqlSessionFactory.getConfiguration().getDefaultExecutorType();
            this.sqlSessionProxy = (SqlSession) newProxyInstance(SqlSessionFactory.class.getClassLoader(),
                    new Class[]{SqlSession.class}, new SqlSessionInterceptor());
        }

        private class SqlSessionInterceptor implements InvocationHandler {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                SqlSession sqlSession = SQL_SESSION_HOLDER.get();
                if (sqlSession == null) {
                    sqlSession = sqlSessionFactory.openSession(executorType, true);
                    if (HandlerProxyUtil.isHandlerSessionHolder()) {
                        SQL_SESSION_HOLDER.set(sqlSession);
                        log.debug("Creating a new handler SqlSession [{}]", sqlSession);
                    } else {
                        log.debug("Creating a new SqlSession [{}]", sqlSession);
                    }
                }
                try {
                    return method.invoke(sqlSession, args);
                } catch (Throwable t) {
                    throw t;
                } finally {
                    if (!HandlerProxyUtil.isHandlerSessionHolder() && sqlSession != null) {
                        sqlSession.close();
                        log.debug("Closing SqlSession [{}]", sqlSession);
                    }
                }
            }
        }

        @Override
        public <T> T selectOne(String statement) {
            return sqlSessionProxy.selectOne(statement);
        }

        @Override
        public <T> T selectOne(String statement, Object parameter) {
            return sqlSessionProxy.selectOne(statement, parameter);
        }

        @Override
        public <K, V> Map<K, V> selectMap(String statement, String mapKey) {
            return sqlSessionProxy.selectMap(statement, mapKey);
        }

        @Override
        public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
            return sqlSessionProxy.selectMap(statement, parameter, mapKey);
        }

        @Override
        public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
            return sqlSessionProxy.selectMap(statement, parameter, mapKey, rowBounds);
        }

        @Override
        public <T> Cursor<T> selectCursor(String statement) {
            return sqlSessionProxy.selectCursor(statement);
        }

        @Override
        public <T> Cursor<T> selectCursor(String statement, Object parameter) {
            return sqlSessionProxy.selectCursor(statement, parameter);
        }

        @Override
        public <T> Cursor<T> selectCursor(String statement, Object parameter, RowBounds rowBounds) {
            return sqlSessionProxy.selectCursor(statement, parameter, rowBounds);
        }

        @Override
        public <E> List<E> selectList(String statement) {
            return sqlSessionProxy.selectList(statement);
        }

        @Override
        public <E> List<E> selectList(String statement, Object parameter) {
            return sqlSessionProxy.selectList(statement, parameter);
        }

        @Override
        public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
            return sqlSessionProxy.selectList(statement, parameter, rowBounds);
        }

        @Override
        public void select(String statement, ResultHandler handler) {
            sqlSessionProxy.select(statement, handler);
        }

        @Override
        public void select(String statement, Object parameter, ResultHandler handler) {
            sqlSessionProxy.select(statement, parameter, handler);
        }

        @Override
        public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
            sqlSessionProxy.select(statement, parameter, rowBounds, handler);
        }

        @Override
        public int insert(String statement) {
            return sqlSessionProxy.insert(statement);
        }

        @Override
        public int insert(String statement, Object parameter) {
            return sqlSessionProxy.insert(statement, parameter);
        }

        @Override
        public int update(String statement) {
            return sqlSessionProxy.update(statement);
        }

        @Override
        public int update(String statement, Object parameter) {
            return sqlSessionProxy.update(statement, parameter);
        }

        @Override
        public int delete(String statement) {
            return sqlSessionProxy.delete(statement);
        }

        @Override
        public int delete(String statement, Object parameter) {
            return sqlSessionProxy.delete(statement, parameter);
        }

        @Override
        public <T> T getMapper(Class<T> type) {
            return getConfiguration().getMapper(type, this);
        }

        @Override
        public void commit() {
            sqlSessionProxy.commit();
        }

        @Override
        public void commit(boolean force) {
            sqlSessionProxy.commit(force);
        }

        @Override
        public void rollback() {
            sqlSessionProxy.rollback();
        }

        @Override
        public void rollback(boolean force) {
            sqlSessionProxy.rollback(force);
        }

        @Override
        public void close() {
            sqlSessionProxy.close();
        }

        @Override
        public void clearCache() {
            sqlSessionProxy.clearCache();
        }

        @Override
        public Configuration getConfiguration() {
            return sqlSessionFactory.getConfiguration();
        }

        @Override
        public Connection getConnection() {
            return sqlSessionProxy.getConnection();
        }

        @Override
        public List<BatchResult> flushStatements() {
            return sqlSessionProxy.flushStatements();
        }

    }

}