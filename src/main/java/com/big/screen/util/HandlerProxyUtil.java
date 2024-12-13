package com.big.screen.util;

import com.big.screen.config.BigScreenHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class HandlerProxyUtil {

    public static BigScreenHandler createHandlerProxy(BigScreenHandler handler) {
        return (BigScreenHandler) Proxy.newProxyInstance(
                handler.getClass().getClassLoader(),
                getAllInterfaces(handler.getClass()),
                new handlerInvocationHandler(handler)
        );
    }

    public static Class<?>[] getAllInterfaces(Class<?> clazz) {
        List<Class<?>> interfaceList = new ArrayList<>();
        while (clazz != null) {
            Class<?>[] interfaces = clazz.getInterfaces();
            interfaceList.addAll(Arrays.asList(interfaces));
            clazz = clazz.getSuperclass();
        }
        return interfaceList.toArray(new Class[]{});
    }

    public static boolean isHandlerSessionHolder() {
        return handlerInvocationHandler.HANDLER_CALL_ENTRY.get() != null;
    }

    private static class handlerInvocationHandler implements InvocationHandler {

        private static final ThreadLocal<Method> HANDLER_CALL_ENTRY = new ThreadLocal<>();

        private Object handler;

        public handlerInvocationHandler(Object handler) {
            this.handler = handler;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                if (HANDLER_CALL_ENTRY.get() == null) {
                    HANDLER_CALL_ENTRY.set(method);
                }
                return method.invoke(handler, args);
            } finally {
                if (method == HANDLER_CALL_ENTRY.get()) {
                    HANDLER_CALL_ENTRY.remove();
                    log.debug("Closing handler SqlSession [{}]", MyBatisUtil.closeSqlSession());
                }
            }
        }
    }

}
