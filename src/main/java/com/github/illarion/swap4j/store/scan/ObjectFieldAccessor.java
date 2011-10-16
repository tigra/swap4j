package com.github.illarion.swap4j.store.scan;

import java.lang.reflect.Field;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public class ObjectFieldAccessor implements FieldAccessor {
    private Field field;

    public ObjectFieldAccessor(Field field) {
        this.field = field;
    }

    @Override
    public void set(Object destination, Object value) {
        try {
            field.set(destination, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace(); // TODO throw own exception
        }
    }

    @Override
    public Object get(Object destination) {
        try {
            return field.get(destination);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Class getType() {
        return field.getType();
    }
}
