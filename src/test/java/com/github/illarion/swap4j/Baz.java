package com.github.illarion.swap4j;

import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.swap.Swap;
import de.huxhorn.lilith.logback.classic.NDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final static Logger log = LoggerFactory.getLogger("Baz");
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

    @Override
    public boolean equals(Object o) {
        NDC.push(String.format("%s.equals(%s)", this, o));
        log.debug(String.format("%s.equals(%s)", this, o));
        try {
            if (this == o) return true;
            if (!(o instanceof Baz)) return false;

            Baz baz = (Baz) o;

            if (children != null ? !children.equals(baz.getChildren()) : baz.getChildren() != null) return false;
            if (value != null ? !value.equals(baz.getValue()) : baz.getValue() != null) return false;

            return true;
        } finally {
            NDC.pop();
        }
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (children != null ? children.hashCode() : 0);
        return result;
    }
}
