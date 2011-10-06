package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.swap.Proxy;
import com.github.illarion.swap4j.swap.ProxyList;
import com.github.illarion.swap4j.swap.ProxyUtils;
import com.github.illarion.swap4j.swap.Utils;

import java.lang.reflect.*;
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
    private FieldStorage writer;
    private final ObjectContext objectContext = new ObjectContext();

    public ObjectScanner(FieldStorage writer) {
        this.writer = writer;
    }

    public void scanObject(UUID id, Object object) throws IllegalAccessException, StoreException {
        objectContext.addRoot(id);
        scan(object, null);
    }

    public void scanObject(Object object) throws StoreException, IllegalAccessException {
        scanObject(null, object);
    }

    public <E> void  scanObject(ProxyList<E> list, Class<E> elementClass) throws StoreException {
        scanList(list, elementClass);
    }

    private void scan(Object object, Field declaringField) throws IllegalAccessException, StoreException {
        Type type = null;
        if (null != declaringField) {
            type = declaringField.getGenericType();
        }
        scanObject(object, type);
    }

    private void scanObject(Object object, Type type) throws StoreException, IllegalAccessException {
        if (object == null) {
//            System.out.println("Null, nothing to scan");
            return;
        }
//        System.out.println("Scanning " + object + " of type " + object.getClass());
        Class clazz = object.getClass();
        if (ProxyList.class.isAssignableFrom(clazz)) {
            scanList((ProxyList)object, type);
        } else if (clazz == String.class) { // primitive
            visitAtom(object, String.class);
        } else if (Proxy.class.isAssignableFrom(clazz)) {
            visitProxy(clazz, (Proxy) object);
        } else {
            Proxy proxy = ProxyUtils.getProxy(object);
            if (null != proxy) {
                visitEnhancedProxy(clazz, proxy);
            } else { // usual object
                visitCompoundObject(object, clazz);
            }
        }
    }

    private void scanList(ProxyList list, Type type) throws StoreException {
        Class elementClass = null;
        if (type instanceof ParameterizedType) {
            elementClass = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
        }
        if (null == elementClass) {
            elementClass = Object.class; // TODO try to infer in runtime?
        }
        scanList(list, elementClass);
    }

    private void scanList(ProxyList list, Class elementClass) throws StoreException {
        scanList(null, list, elementClass);
    }

    private void scanList(UUID uuid, ProxyList list, Class elementClass) throws StoreException {
        objectContext.addRoot(uuid);
        visitList(list, list.getClass(), elementClass);
    }

    private void visitCompoundObject(Object object, Class clazz) throws IllegalAccessException, StoreException {
        if (null == object) {
            return;
        }
        assert clazz.equals(object.getClass());
        List<Field> fields = Utils.getAllFields(clazz);
        for (Field field : fields) {
            final Class<?> fieldType = field.getType();
            final Object fieldValue = field.get(object);
//            System.out.println("Field: " + field + ", type=" + fieldType + ", value=" + fieldValue);
            if (field.isSynthetic() || Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            if (Proxy.class.isAssignableFrom(fieldType)) {
                visitProxyField(field.getName(), (Proxy) fieldValue, Utils.getProxyType(field));
            } else if (String.class.isAssignableFrom(fieldType)) { // primitive
                visitPrimitiveField(field.getName(), fieldValue);
            } else { // compound
                visitCompoundField(field.getName(), fieldValue, clazz);
            }
        }
    }

    private void visitProxy(Class clazz, Proxy proxy) throws StoreException, IllegalAccessException {
        objectContext.updateId(proxy.getId());
        final Object realObject = proxy.getRealObject();
        write(objectContext.peek(), realObject, proxy.getClazz(), RECORD_TYPE.PROXIED_VALUE);
        visitCompoundObject(realObject, proxy.getClazz());
    }

    private void visitEnhancedProxy(Class clazz, Proxy proxy) {
        objectContext.updateId(proxy.getId());
        write(objectContext.peek(), proxy.getRealObject(), clazz, RECORD_TYPE.PROXIED_VALUE); // TODO type for enhanced?
    }

    private void visitCompoundField(String name, Object value, Class clazz) throws IllegalAccessException, StoreException {
        Locator locator = objectContext.push(name);
        write(locator, value, clazz, RECORD_TYPE.COMPOUND_FIELD);
        scan(value, null);
        objectContext.pop();
    }

    private void visitPrimitiveField(String name, Object value) {
        Locator locator = objectContext.push(name);
        write(locator, value, value.getClass(), RECORD_TYPE.PRIMITIVE_FIELD);
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
            write(objectContext.peek(), proxy, clazz, RECORD_TYPE.PROXIED_FIELD);
        } else {
            Locator locator = objectContext.push(proxy.getId(), "/" + name);
            write(locator, proxy, clazz, RECORD_TYPE.PROXIED_FIELD);
//        if (proxy.isLoaded()) {
//            scan(proxy.getRealObject());
//        }
            proxy.load();
            scan(proxy.getRealObject(), null);
        }
        objectContext.pop();
    }

    private void visitAtom(Object object, Class clazz) {
        write(objectContext.peek(), object, clazz, RECORD_TYPE.PRIMITIVE_VALUE);
    }

    private void write(Locator locator, Object value, Class clazz, RECORD_TYPE recordType) {
        FieldRecord fieldRecord = new FieldRecord(locator, value, clazz, recordType);
//        System.out.println(">" + fieldRecord);
        writer.serialize(fieldRecord);
    }

    private void write(Locator locator, Object value, Class clazz, Class elementClass, RECORD_TYPE recordType) {
        FieldRecord fieldRecord = new FieldRecord(locator, value, clazz, elementClass, recordType);
//        System.out.println(">" + fieldRecord);
        writer.serialize(fieldRecord);
    }

    private void visitList(ProxyList list, Class clazz, Class elementClass) throws StoreException {
        objectContext.push(list.getId(), "[");
//        final Class clazz = list.getClazz();
        write(objectContext.peek(), list, clazz, RECORD_TYPE.PROXY_LIST); // TODO store elementclass in Proxy
        int i = 0;
        for (Object obj : list) {
            visitProxyListElement(obj, i++, elementClass);
        }
        objectContext.pop();
    }


    private void visitProxyListElement(Object obj, int pos, Class clazz) throws StoreException {
//        if (Enhancer.isEnhanced(obj.getClass())) {
        Proxy proxy = ProxyUtils.getProxy(obj);
        if (proxy != null) {
            objectContext.push(proxy.getId(), String.valueOf(pos));
            proxy.load();
            write(objectContext.peek(), proxy.getRealObject(), clazz, RECORD_TYPE.LIST_VALUE);
            objectContext.pop();
        } else {
            throw new NullPointerException("null proxy");
        }
//        }
    }
}
