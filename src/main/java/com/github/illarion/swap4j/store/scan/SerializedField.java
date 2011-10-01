package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.swap.Utils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 16, 2011 2:53:26 AM
 *
 * @author Alexey Tigarev
 */
public class SerializedField<T> implements Comparable<SerializedField<T>> {
    private T value;
    private TYPE type;
    private Class clazz;
    Locator locator;


    public SerializedField() {
    }

    public SerializedField(UUID id, String path, T value, Class clazz, TYPE type) {
        checkValue(value);
        this.clazz = clazz;
        this.locator = new Locator(id, path);
        this.type = type;
        this.value = value;
    }

    private void checkValue(T value) {
        if (null != value && value instanceof SerializedField) {
            throw new IllegalArgumentException("Nested SerializedFields are wrong + " + this + ", " + value);
        }
    }

    public SerializedField(int idNumber, String path, T value, Class clazz, TYPE type) {
        this(new UUID(0, idNumber), path, value, clazz, type);
    }

    public SerializedField(Locator locator, T value, Class clazz, TYPE type) {
        checkValue(value);
        this.clazz = clazz;
        this.locator = locator;
        this.type = type;
        this.value = value;
    }

    public UUID getId() {
        return locator.getId();
    }

    public Class getClazz() {
        return clazz;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SF{");
        sb.append("@").append(locator);
        sb.append(" =").append(value);
        sb.append(" c=").append(shortName(clazz));
        sb.append(" t=").append(type);
        sb.append('}');
        return sb.toString();
    }

    private String shortName(Class clazz) {
        if (clazz == null) {
            return null;
        } else {
            return clazz.getSimpleName();
        }
    }

    @SuppressWarnings({"RedundantIfStatement"})
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SerializedField that = (SerializedField) o;

        if (type != that.type) return false;
        if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null) return false;
        if (locator != null ? !locator.equals(that.locator) : that.locator != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (clazz != null ? clazz.hashCode() : 0);
        result = 31 * result + (locator != null ? locator.hashCode() : 0);
        return result;
    }

    public Locator getLocator() {
        return locator;
    }

    @Override
    public int compareTo(SerializedField<T> that) {
        if (this.locator == null) {
            return that.locator == null? 0 : -1;
        }
        if (that.locator == null) {
            return 1;
        }
        return this.locator.compareTo(that.locator);
    }


    /**
     * Write the value of this field to specified object into place identified by path.
     *
     * @param object
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public void writeTo(Object object) throws NoSuchFieldException, IllegalAccessException {
        if (getLocator().isRoot(this)) {
            return; // can't set object itself, ignoring
        }
        List<String> pathComponents = locator.getParsedPath();
        writeTo(object, pathComponents, getValue());
    }

    public void writeTo(Object object, List<String> pathComponents, Object value) throws NoSuchFieldException, IllegalAccessException {
        if (pathComponents.size() < 1) {
            throw new IllegalArgumentException("Hm.....");
        }
        String fieldName = pathComponents.get(0);
        Class<?> clazz = object.getClass();

        Field field = Utils.getAccessibleField(fieldName, clazz);
        if (pathComponents.size() == 1) {
            field.set(object, value);
        } else {
            pathComponents.remove(0);
            writeTo(field.get(object), pathComponents, value);
        }
    }

}
