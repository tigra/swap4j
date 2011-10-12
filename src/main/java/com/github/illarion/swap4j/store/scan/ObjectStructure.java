package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.swap.ProxyList;
import com.github.illarion.swap4j.swap.Swap;
import com.github.illarion.swap4j.swap.Utils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public class ObjectStructure {
    Class clazz = Object.class;

    public ObjectStructure(Object object) {
        if (null != object) {
            clazz = object.getClass();
        }
//        swap = fieldRecord.swap;
    }

    void writeTo(Object object, FieldRecord fieldRecord) throws NoSuchFieldException, IllegalAccessException, StoreException {
        List<String> pathComponents = fieldRecord.locator.getParsedPath();
        writeTo(object, pathComponents, fieldRecord.getValue(), fieldRecord);
    }

    public void writeTo(Object destination, List<String> path, Object value, FieldRecord fieldRecord) throws NoSuchFieldException, IllegalAccessException, StoreException {
        if (path.size() < 1) {
            throw new IllegalArgumentException("Hm.....");
        }
        String fieldName = path.get(0);
        Class<?> clazz = destination.getClass();

        Field field = Utils.getAccessibleField(fieldName, clazz);
        if (null == field) {
            System.err.println("Error deserializing: " + fieldRecord);
//            throw new NoSuchFieldException(fieldName);
        } else if (path.size() == 1) {
            field.set(destination, deserializeValue(field.getType(), value, fieldRecord.getElementClass()));
        } else {
            path.remove(0);
            writeTo(field.get(destination), path, value, fieldRecord);
        }
    }

    private Object deserializeValue(Class clazz, Object value, Class elementClass) throws StoreException {
        if (List.class.isAssignableFrom(clazz)) {
//            UUID uuid = UUID.fromString((String)value);
            UUID uuid = value instanceof String ? UUID.fromString((String)value) : (UUID)value;
            List list = new ProxyList(Swap.getInstance(), elementClass, uuid); // TODO also process attaching individual elements to the list - somewhere else
            return list;
        } else {
            return value;
        }
    }


}
