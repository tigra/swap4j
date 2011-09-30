package com.github.illarion.swap4j.swap;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

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

    public static List<Field> getAllFields(Class clazz) {
        List<Field> fields = new ArrayList<Field>();
        while (clazz != null) {
            final Field[] declaredFields = clazz.getDeclaredFields();
            AccessibleObject.setAccessible(declaredFields, true);
            fields.addAll(Arrays.asList(declaredFields));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private static Map<Class, Map<String, Field>> fieldMapCache = new HashMap<Class,  Map<String, Field>>();

    public static Map<String, Field> getAllFieldsMap(Class clazz) {
        if (fieldMapCache.containsKey(clazz)) {
            return fieldMapCache.get(clazz);
        } else {
            Map<String, Field> fieldsMap = createAllFieldsMap(clazz);
            fieldMapCache.put(clazz, fieldsMap);
            return fieldsMap;
        }
    }

    private static Map<String, Field> createAllFieldsMap(Class clazz) {
        // TODO test on hiding field (will fail)
        Map<String, Field>  map = new HashMap<String, Field>();
        while (clazz != null) {
            final Field[] declaredFields = clazz.getDeclaredFields();
            AccessibleObject.setAccessible(declaredFields, true);
            for (Field f : declaredFields) {
                map.put(f.getName(), f);
            }
            clazz = clazz.getSuperclass();
        }
        return map;
    }


    public static Field getAccessibleField(String fieldName, Class clazz) throws NoSuchFieldException {
        return getAllFieldsMap(clazz).get(fieldName);
    }
}
