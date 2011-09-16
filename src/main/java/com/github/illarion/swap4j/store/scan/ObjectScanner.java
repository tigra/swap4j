package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.swap.ProxyList;

import java.lang.reflect.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    public ObjectScanner(ObjectSerializer writer) {
        this.writer = writer;
    }

    public void scan(Object object) {
        if (object == null) {
            return;
        }
        Class clazz = object.getClass();
        if (clazz == String.class) {
            visitAtom(object, String.class);
        } else if (ProxyList.class.isAssignableFrom(clazz)) {
            visitList((ProxyList)object);
        } else { // usual object
            List<Field> fields = getAllFields(clazz);
            for (Field field: fields) {
                if (Proxy.class.isAssignableFrom(field.getClass())) {
                    visitProxyField((Proxy)object);
                } else { // unclassified object (primitive and more)
                    visitField(object);
                }
            }
        }
    }

    private List<Field> getAllFields(Class clazz) {
        List<Field> fields = new ArrayList<Field>();
        while (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private void visitField(Object object) {
        throw new UnsupportedOperationException(""); // TODO Implement this method
    }

    private void visitProxyField(Proxy proxy) {
        throw new UnsupportedOperationException(""); // TODO Implement this method

    }

    private void visitAtom(Object object, Class clazz) {
        writer.serialize(new Atom(object, clazz));
    }

    private void visitList(ProxyList list) {
        writer.serialize(new SerializedList(list));
    }

}
