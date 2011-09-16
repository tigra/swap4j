package com.github.illarion.swap4j;

import com.github.illarion.swap4j.swap.Swap;

import java.util.List;

/**
* TODO Describe class
* <p/>
* <p/>
* Created at: Sep 15, 2011 11:52:36 PM
*
* @author Alexey Tigarev
*/
public class Baz {
    private String value = "baz";
    private List<Baz> children = null;
    private transient Swap swap = null;

    public Baz(Swap swap, String value) {
        this.swap = swap;
        this.value = value;
        children = swap.newWrapList(Baz.class);
    }

    public Baz() {

    }

    public void add(Baz elem) {
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
}
