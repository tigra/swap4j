package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.swap.Proxy;
import com.github.illarion.swap4j.swap.ProxyList;
import com.github.illarion.swap4j.swap.Utils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 16, 2011 12:36:39 AM
 *
 * @author Alexey Tigarev
 */
public class ObjectScanner {
    private ObjectSerializer writer;
    private final ObjectContext objectContext = new ObjectContext();

    public ObjectScanner(ObjectSerializer writer) {
        this.writer = writer;
    }

    public void scanObject(Object object) throws IllegalAccessException, StoreException {
        objectContext.addRoot();
        scan(object);
    }

    private void scan(Object object) throws IllegalAccessException, StoreException {
        if (object == null) {
            System.out.println("Null, nothing to scan");
            return;
        }
        System.out.println("Scanning " + object + " of type " + object.getClass());
        Class clazz = object.getClass();
        if (clazz == String.class) {
            visitAtom(object, String.class);
        } else if (ProxyList.class.isAssignableFrom(clazz)) {
            visitList((ProxyList) object);
        } else { // usual object
            visitCompoundObject(object, clazz);
        }
    }

    private void visitCompoundObject(Object object, Class clazz) throws IllegalAccessException, StoreException {
        if (Proxy.class.isAssignableFrom(clazz)) {
            objectContext.updateId(((Proxy) object).getId());
        }
        List<Field> fields = getAllFields(clazz);
        for (Field field : fields) {
            final Class<?> fieldType = field.getType();
            final Object fieldValue = field.get(object);
            System.out.println("Field: " + field + ", type=" + fieldType + ", value=" + fieldValue);
            if (field.isSynthetic()) {
                continue;
            }
            if (Proxy.class.isAssignableFrom(fieldType)) {
                try {
                    visitProxyField(field.getName(), (Proxy) fieldValue, Utils.getProxyType(field));
                } catch (IllegalAccessException e) {
                    throw e;
                }
            } else if (String.class.isAssignableFrom(fieldType)) { // primitive
                visitPrimitiveField(field.getName(), fieldValue);
            } else { // compound
                visitCompoundField(field.getName(), fieldValue, clazz);
            }
        }
    }

    private void visitCompoundField(String name, Object value, Class clazz) throws IllegalAccessException, StoreException {
        Locator locator = objectContext.push(name);
        write(locator, value, clazz, TYPE.COMPOUND_FIELD);
        scan(value);
        objectContext.pop();
    }

    static List<Field> getAllFields(Class clazz) {
        List<Field> fields = new ArrayList<Field>();
        while (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private void visitPrimitiveField(String name, Object value) {
        Locator locator = objectContext.push(name);
        write(locator, value, value.getClass(), TYPE.PRIMITIVE_FIELD);
        objectContext.pop();
    }

    /**
     * @param name
     * @param proxy
     * @param clazz Class for "proxy" - taken from class field
     * @throws IllegalAccessException
     * @throws StoreException
     */
    private void visitProxyField(String name, Proxy proxy, Class clazz) throws IllegalAccessException, StoreException {
        if (null == proxy) {
            objectContext.push(name);
            write(objectContext.peek(), proxy, clazz, TYPE.PROXIED_FIELD);
        } else {
            Locator locator = objectContext.push(name, proxy.getId());
            write(locator, proxy, clazz, TYPE.PROXIED_FIELD);
//        if (proxy.isLoaded()) {
//            scan(proxy.getRealObject());
//        }
            proxy.load();
            scan(proxy.getRealObject());
        }
        objectContext.pop();
    }


    private void visitAtom(Object object, Class clazz) {
        write(objectContext.peek(), object, clazz, TYPE.PRIMITIVE_VALUE);
    }

    private void write(Locator locator, Object value, Class clazz, TYPE type) {
        SerializedField serializedField = new SerializedField(locator, value, clazz, type);
        System.out.println(">" + serializedField);
        writer.serialize(serializedField);
    }

    private void visitList(ProxyList list) {
        write(objectContext.peek(), list, null, TYPE.PROXY_LIST);
    }

}
