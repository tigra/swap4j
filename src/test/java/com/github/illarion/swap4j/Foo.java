package com.github.illarion.swap4j;

/**
* TODO Describe class
*
* @author Alexey Tigarev tigra@agile-algorithms.com
*/
public class Foo {
    private String bar;
    private Foo nestedFoo;

    public Foo() {
    }

    public Foo(String bar, Foo nestedFoo) {
        this.bar = bar;
        this.nestedFoo = nestedFoo;
    }

    public String getBar() {
        return bar;
    }

    public void setBar(String bar) {
        this.bar = bar;
    }

    public Foo getNestedFoo() {
        return nestedFoo;
    }

    @Override
    public String toString() {
        return "Foo{" + "bar=" + bar + ", nestedFoo=" + nestedFoo + '}';
    }

    @SuppressWarnings({"RedundantIfStatement"})
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Foo other = (Foo) obj;
        if ((this.bar == null) ? (other.bar != null) : !this.bar.equals(other.bar)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + (this.bar != null ? this.bar.hashCode() : 0);
        return hash;
    }
}
