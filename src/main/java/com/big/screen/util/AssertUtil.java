package com.big.screen.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import java.util.Collection;

public class AssertUtil {

    public static void notNull(Object object, String message) {
        Preconditions.checkNotNull(object, message);
    }

    public static void isNull(Object object, String message) {
        Preconditions.checkArgument(object == null, message);
    }

    public static void hasLength(String text, String message) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(text), message);
    }

    public static void notEmpty(Object[] array, String message) {
        Preconditions.checkArgument(array != null && array.length > 0, message);
    }

    public static void isEmpty(Object[] array, String message) {
        Preconditions.checkArgument(array == null || array.length == 0, message);
    }

    public static void notEmpty(Collection<?> collection, String message) {
        Preconditions.checkArgument(collection != null && !Iterables.isEmpty(collection), message);
    }

    public static void isEmpty(Collection<?> collection, String message) {
        Preconditions.checkArgument(collection == null || Iterables.isEmpty(collection), message);
    }

    public static void isTrue(boolean expression, String message) {
        Preconditions.checkArgument(expression, message);
    }

    public static void isFalse(boolean expression, String message) {
        Preconditions.checkArgument(!expression, message);
    }

    public static void gtZero(long number, String message) {
        Preconditions.checkArgument(number > 0, message);
    }
}