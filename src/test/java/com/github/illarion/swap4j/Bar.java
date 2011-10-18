package com.github.illarion.swap4j;

/**
* TODO Describe class
*
* @author Alexey Tigarev tigra@agile-algorithms.com
*/
public class Bar {

    String value = "new";

    public Bar(String value) {
        this.value = value;
    }

    public Bar() {
    }

    public void change(String change) {
        value = change;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Bar{" + "value=" + value + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bar)) return false;

        Bar bar = (Bar) o;

        if (value != null ? !value.equals(bar.getValue()) : bar.value != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
