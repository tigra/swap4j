package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.swap.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public class ObjectStructure extends ContextTracking {
    Class clazz = Object.class;
    private final static Logger log = LoggerFactory.getLogger("ObjectStructure");

    public ObjectStructure(Object object) {
        if (null != object) {
            clazz = object.getClass();
        }
//        swap = fieldRecord.swap;
    }

    Object writeTo(Object object, FieldRecord fieldRecord) throws NoSuchFieldException, IllegalAccessException, StoreException {
        List<String> pathComponents = fieldRecord.locator.getParsedPath();
        return writeTo(object, pathComponents, fieldRecord.getValue(), fieldRecord);
    }

    public Object writeTo(Object destination, List<String> path, Object value, FieldRecord fieldRecord) throws NoSuchFieldException, IllegalAccessException, StoreException {
        log.debug(String.format("writeTo(d=%s, p=%s, v=%s, fr=%s", new Object[]{destination, path, value, fieldRecord}));
        if (path.size() < 1) {
            throw new IllegalArgumentException("Hm.....");
        }
        String fieldName = path.get(0);
        Class<?> clazz = destination.getClass();

        FieldAccessor accessor = getAccessor(fieldName, clazz);
        if (null == accessor) {
            System.err.println("Error deserializing: " + fieldRecord);
//            throw new NoSuchFieldException(fieldName);
            return null;
        } else if (path.size() == 1) {
//            field.set(destination, deserializeValue(field.getType(), value, fieldRecord));
            Object deserializedValue = deserializeValue(accessor.getType(), value, fieldRecord);
            accessor.set(destination, deserializedValue);
            return deserializedValue;
        } else {
            path.remove(0);
            return writeTo(accessor.get(destination), path, value, fieldRecord);
        }
    }

    private FieldAccessor getAccessor(String fieldName, Class<?> clazz) throws NoSuchFieldException {
        if (fieldName.startsWith("[")) {
            int position = Integer.valueOf(fieldName.substring(1));
            return new ListElementAccessor(position, clazz);
        } else {
            Field field = Utils.getAccessibleField(fieldName, clazz);
            FieldAccessor accessor = field == null ? null : new ObjectFieldAccessor(field);
            return accessor;
        }
    }

    private Object deserializeValue(Class clazz, Object value, FieldRecord fieldRecord) throws StoreException {
        enter("deserializeValue");
        if (List.class.isAssignableFrom(clazz)) {
//            UUID uuid = UUID.fromString((String)value);
            UUID uuid = getUUID(value);
            List list = new ProxyList(Swap.getInstance(), fieldRecord.getElementClass(), uuid, false);
            // TODO also process attaching individual elements to the list - somewhere else
            exit();
            return list;
        } else if (fieldRecord.isListElement()) {
            UUID uuid = getUUID(value);
            Proxy proxy = new Proxy(uuid, Swap.getInstance().getStore(), clazz);
            exit();
            return proxy;
        } else {
            exit();
            return value;
        }
    }

    private UUID getUUID(Object value) {
        return value instanceof String ? UUID.fromString((String) value) : (UUID) value;
    }

    @Override
    protected String getContextInfo(String context) {
        return "ObjectTracking." + context;
    }
}
