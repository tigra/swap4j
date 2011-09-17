package com.github.illarion.swap4j.store.scan;

import java.util.UUID;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 16, 2011 2:53:26 AM
 *
 * @author Alexey Tigarev
 */
public class SerializedField<T> implements PreSerialized {
    private T value;
    private TYPE type;
    private Class clazz;
    private Locator locator;


    public SerializedField() {
    }

    public SerializedField(UUID id, String path, T value, Class clazz, TYPE type) {
        this.clazz = clazz;
        this.locator = new Locator(id, path);
        this.type = type;
        this.value = value;
    }

    public SerializedField(int idNumber, String path, T value, Class clazz, TYPE type) {
        this(new UUID(0, idNumber), path, value, clazz, type);
    }

    public SerializedField(Locator locator, T value, Class clazz, TYPE type) {
        this.clazz = clazz;
        this.locator = locator;
        this.type = type;
        this.value = value;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SerializedField that = (SerializedField) o;

        if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null) return false;
        if (locator != null ? !locator.equals(that.locator) : that.locator != null) return false;
        if (type != that.type) return false;
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
}
