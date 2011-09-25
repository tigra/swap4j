package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.swap.Proxy;
import com.github.illarion.swap4j.swap.ProxyList;
import com.github.illarion.swap4j.swap.ProxyUtils;
import com.github.illarion.swap4j.swap.Utils;
import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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

    public void scanObject(UUID id, Object object) throws IllegalAccessException, StoreException {
        objectContext.addRoot(id);
        scan(object);
    }

    public void scanObject(Object object) throws StoreException, IllegalAccessException {
        scanObject(null, object);
    }

    private void scan(Object object) throws IllegalAccessException, StoreException {
        if (object == null) {
//            System.out.println("Null, nothing to scan");
            return;
        }
//        System.out.println("Scanning " + object + " of type " + object.getClass());
        Class clazz = object.getClass();
        if (clazz == String.class) {
            visitAtom(object, String.class);
        } else if (ProxyList.class.isAssignableFrom(clazz)) {
            visitList((ProxyList) object);
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

    private void visitCompoundObject(Object object, Class clazz) throws IllegalAccessException, StoreException {
        if (null == object) {
            return;
        }
        assert clazz.equals(object.getClass());
        List<Field> fields = getAllFields(clazz);
        for (Field field : fields) {
            final Class<?> fieldType = field.getType();
            final Object fieldValue = field.get(object);
//            System.out.println("Field: " + field + ", type=" + fieldType + ", value=" + fieldValue);
            if (field.isSynthetic() || Modifier.isTransient(field.getModifiers())) {
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

    private void visitProxy(Class clazz, Proxy proxy) throws StoreException, IllegalAccessException {
        objectContext.updateId(proxy.getId());
        final Object realObject = proxy.getRealObject();
        write(objectContext.peek(), realObject, proxy.getClazz(), TYPE.PROXIED_VALUE);
        visitCompoundObject(realObject, proxy.getClazz());
    }

    private void visitEnhancedProxy(Class clazz, Proxy proxy) {
        objectContext.updateId(proxy.getId());
        write(objectContext.peek(), proxy.getRealObject(), clazz, TYPE.PROXIED_VALUE); // TODO type for enhanced?
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
            final Field[] declaredFields = clazz.getDeclaredFields();
            AccessibleObject.setAccessible(declaredFields, true);
            fields.addAll(Arrays.asList(declaredFields));
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
            Locator locator = objectContext.push(proxy.getId(), "/" + name);
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
//        System.out.println(">" + serializedField);
        writer.serialize(serializedField);
    }

    private void visitList(ProxyList list) throws StoreException {
        objectContext.push(list.getId(), "[");
        final Class clazz = list.getClazz();
        write(objectContext.peek(), list, clazz, TYPE.PROXY_LIST);
        int i = 0;
        for (Object obj : list) {
            visitProxyListElement(obj, i++, clazz);
        }
        objectContext.pop();
    }

    private void visitProxyListElement(Object obj, int pos, Class clazz) throws StoreException {
//        if (Enhancer.isEnhanced(obj.getClass())) {
            Proxy proxy = ProxyUtils.getProxy(obj);
            if (proxy != null) {
                objectContext.push(proxy.getId(), String.valueOf(pos));
                proxy.load();
                write(objectContext.peek(), proxy.getRealObject(), clazz, TYPE.LIST_VALUE);
                objectContext.pop();
            } else {
                throw new NullPointerException("null proxy");
            }
//        }
    }


}
