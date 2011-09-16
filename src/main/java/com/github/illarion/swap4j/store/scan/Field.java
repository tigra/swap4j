package com.github.illarion.swap4j.store.scan;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 16, 2011 2:53:26 AM
 *
 * @author Alexey Tigarev
 */
public class Field<T> implements PreSerialized {
    T value;
    String name;

    public Field() {
    }

    public Field(String name, T value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Field");
        sb.append("{name='").append(name).append('\'');
        sb.append(", value=").append(value);
        sb.append('}');
        return sb.toString();
    }
}
