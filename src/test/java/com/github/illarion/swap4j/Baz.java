package com.github.illarion.swap4j;

import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.swap.Swap;

import java.util.List;

/**
* Class with String field and swapped List of same class elements.
 * For testing purposes.
*
* @author Alexey Tigarev tigra&at;agile-algorithms.com
*/
public class Baz {
    private String value = "baz";
    private List<Baz> children = null;
//    private transient Swap swap = null;

    public Baz(String value) throws StoreException {
        this.value = value;
        children = Swap.getInstance().newWrapList(Baz.class);
//        this.swap = swap;
    }

    public Baz() {

    }

    public void add(Baz elem) throws StoreException {
        if (null == children) {
            children = Swap.getInstance().newWrapList(Baz.class);
        }
        children.add(elem);
    }

    public List<Baz> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Baz{(");
        sb.append(value).append(')');
        if (children != null && children.size() > 0) {
            sb.append("[").append(children).append("]");
        }
        sb.append('}');
        return sb.toString();
    }

    public Object getValue() {
        return value;
    }
}
