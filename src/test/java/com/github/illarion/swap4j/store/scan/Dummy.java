package com.github.illarion.swap4j.store.scan;

/**
 * One-element object for testing.
 *
 * @author Alexey Tigarev
 */
class Dummy {
    String field = null;

    Dummy() {
    }

    Dummy(String field) {
        this.field = field;
    }

    @Override
    public String toString() {
        return "Dummy{field='" + field + "\'}";
    }

    @SuppressWarnings({"RedundantIfStatement"})
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dummy)) return false;

        Dummy dummy = (Dummy) o;

        if (field != null ? !field.equals(dummy.getField()) : dummy.getField() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return field != null ? field.hashCode() : 0;
    }

    public String getField() {
        return field;
    }
}
