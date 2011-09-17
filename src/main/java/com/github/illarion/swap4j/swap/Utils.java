package com.github.illarion.swap4j.swap;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 17, 2011 1:22:27 PM
 *
 * @author Alexey Tigarev
 */
public class Utils {
    /**
     * Determines the type of object inside the Proxy declared by a given field
     * @param field field of interest
     * @return class, or null in case of unparameterized Proxy
     */
    public static Class getProxyType(Field field) {
        assert Proxy.class.equals(field.getClass()): "Proxy fields are expected in Util.getProxyType";
        final Type type = field.getGenericType();
        if (type instanceof ParameterizedType) {
            return (Class)((ParameterizedType) type).getActualTypeArguments()[0];
        } else {
            return null;
        }
    }
}
