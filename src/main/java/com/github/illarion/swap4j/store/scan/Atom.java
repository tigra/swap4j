package com.github.illarion.swap4j.store.scan;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 16, 2011 1:32:26 AM
 *
 * @author Alexey Tigarev
 */
public class Atom implements PreSerialized {
    private Object object;
    private Class clazz;

    public Atom(Object object, Class clazz) {
        this.object = object;
        this.clazz = clazz;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Atom{");
        sb.append(object);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Atom atom = (Atom) o;

        if (clazz != null ? !clazz.equals(atom.clazz) : atom.clazz != null) return false;
        if (object != null ? !object.equals(atom.object) : atom.object != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = object != null ? object.hashCode() : 0;
        result = 31 * result + (clazz != null ? clazz.hashCode() : 0);
        return result;
    }
}
