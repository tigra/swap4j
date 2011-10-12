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
        scanProxyList(list, elementClass);
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
            return;
        }
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
        scanProxyList(list, elementClass);
    }

    private void scanProxyList(ProxyList list, Class elementClass) throws StoreException {
        scanProxyList(null, list, elementClass);
    }

    void scanProxyList(UUID uuid, ProxyList list, Class elementClass) throws StoreException {
        objectContext.addRoot(uuid);
        visitProxyList(list, list.getClass(), elementClass);
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
            if (field.isSynthetic() || Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            if (Proxy.class.isAssignableFrom(fieldType)) {
                visitProxyField(field.getName(), (Proxy) fieldValue, Utils.getProxyType(field));
            } else if (ProxyList.class.isAssignableFrom(fieldType)) {
                visitProxyList2(field, fieldType, (ProxyList) fieldValue);
            } else if (List.class.isAssignableFrom(fieldType)) { // simply list
                visitProxyListField(object, field, fieldValue);
            } else if (String.class.isAssignableFrom(fieldType)) { // primitive
                visitPrimitiveField(field.getName(), fieldValue, fieldType);
            } else { // compound
                visitCompoundField(field.getName(), fieldValue, clazz);
            } // TODO handle ProxyList
        }
    }

    private void visitProxyListField(Object object, Field field, Object fieldValue) throws StoreException {
        ProxyList proxyListFieldValue = (ProxyList) fieldValue;
        objectContext.push(field.getName());
        Class elementClass = getElementClass(field);
        write(objectContext.peek(), proxyListFieldValue.getId().toString(), ProxyList.class, elementClass, RECORD_TYPE.LIST_FIELD);
        // todo list reference (uuid) and elementClass
        objectContext.pop();
        visitProxyList2(field, ProxyList.class, proxyListFieldValue); // TODO is substitution of ProxyList here correct?
        // TODO Problem: we have to serialize object based on its own class, not the field class
//                throw new UnsupportedOperationException("TODO");
    }

    private void visitProxyList2(Field field, Class<?> fieldType, ProxyList proxyListFieldValue) throws StoreException {
        Type elementClass = getElementClass(field);
        visitProxyList(proxyListFieldValue, fieldType, elementClass); // TODO determine it properly
    }

    private Class getElementClass(Field field) {
        Type genericFieldType = field.getGenericType();
        Type elementClass = Object.class;
        if (genericFieldType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)genericFieldType;
            elementClass = parameterizedType.getActualTypeArguments()[0];
        }
        return (Class)elementClass;
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

    private void visitPrimitiveField(String name, Object value, Class fieldType) {
        Locator locator = objectContext.push(name);
        Class clazz = determineClass(value, fieldType);
        write(locator, value, clazz, RECORD_TYPE.PRIMITIVE_FIELD);
        objectContext.pop();
    }

    private Class determineClass(Object value, Class fieldType) {
        Class clazz = Object.class;
        if (null != fieldType) {
            clazz = fieldType;
        } else if (null != value) {
            clazz = value.getClass();
        }
        return clazz;
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
        FieldRecord fieldRecord = new FieldRecordBuilder(locator).setValue(value).setClazz(clazz).setRecordType(recordType).create();
//        System.out.println(">" + fieldRecord);
        writer.serialize(fieldRecord);
    }

    private void write(Locator locator, Object value, Class clazz, Class elementClass, RECORD_TYPE recordType) {
        FieldRecord fieldRecord = new FieldRecordBuilder(locator).setValue(value).setClazz(clazz).setElementClass(elementClass).setRecordType(recordType).create();
//        System.out.println(">" + fieldRecord);
        writer.serialize(fieldRecord);
    }

    private void visitProxyList(ProxyList list, Class clazz, Type elementClass) throws StoreException {
        objectContext.push(list.getId(), "[");
//        final Class clazz = list.getClazz();
        write(objectContext.peek(), list, clazz, (Class)elementClass, RECORD_TYPE.PROXY_LIST); // TPDP
        // TODO Support Type properly, not cast to Class
        // TODO store elementClass in Proxy (?)
        int i = 0;
        for (Object obj : list) {
            visitProxyListElement(obj, i++, (Class)elementClass);
        }
        objectContext.pop();
    }


    private void visitProxyListElement(Object obj, int pos, Class clazz) throws StoreException {
//        if (Enhancer.isEnhanced(obj.getClass())) {
        Proxy proxy = ProxyUtils.getProxy(obj);
        if (proxy != null) {
            objectContext.pushWithoutSlash(String.valueOf(pos));
            write(objectContext.peek(), proxy.getId().toString(), clazz, clazz, RECORD_TYPE.LIST_ELEMENT);
            objectContext.pop();

//            objectContext.push(proxy.getId(), String.valueOf(pos));
//            proxy.load();
//            write(objectContext.peek(), proxy.getRealObject(), clazz, RECORD_TYPE.LIST_ELEMENT);
//            objectContext.pop();
        } else {
            throw new NullPointerException("null proxy");
        }
//        }
    }
}
