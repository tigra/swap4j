package com.github.illarion.swap4j.store.scan;

/**
* TODO Describe class
* <p/>
* <p/>
* Created at: Sep 30, 2011 10:52:38 PM
*
* @author Alexey Tigarev
*/
class Nested {
    String value;
    Nested nested = null;

    public Nested(String value) {
        this.value = value;
    }

    public Nested(String value, Nested nested) {
        this.value = value;
        this.nested = nested;
    }

    @SuppressWarnings({"RedundantIfStatement"})
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Nested nested1 = (Nested) o;

        if (nested != null ? !nested.equals(nested1.nested) : nested1.nested != null) return false;
        if (value != null ? !value.equals(nested1.value) : nested1.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (nested != null ? nested.hashCode() : 0);
        return result;
    }
}
