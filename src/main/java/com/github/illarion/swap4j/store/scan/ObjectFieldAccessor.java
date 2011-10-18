package com.github.illarion.swap4j.store.scan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public class ObjectFieldAccessor implements FieldAccessor {
    private static final Logger log = LoggerFactory.getLogger("ObjectFieldAccessor");

    private Field field;

    public ObjectFieldAccessor(Field field) {
        this.field = field;
    }

    @Override
    public void set(Object destination, Object value) {
        try {
            field.set(destination, value);
        } catch (IllegalAccessException e) {
            log.error("Error setting object field", e);
        }
    }

    @Override
    public Object get(Object destination) {
        try {
            return field.get(destination);
        } catch (IllegalAccessException e) {
            log.error("Error getting object field", e);
            return null;
        }
    }

    @Override
    public Class getType() {
        return field.getType();
    }
}
