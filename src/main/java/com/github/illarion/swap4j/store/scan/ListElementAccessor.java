package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.swap.ProxyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public class ListElementAccessor implements FieldAccessor {
    private Logger log = LoggerFactory.getLogger("ListElementAccessor");

    private int position;
    private Class elementClass;

    public ListElementAccessor(int position, Class elementClass) {
        this.position = position;
        this.elementClass = elementClass;
    }

    @Override
    public void set(Object destination, Object value) {
        log.debug("ListElementAccessor.set(destination={}, value={})", destination, value);
        List list = (List) destination;
        if (list instanceof ProxyList) {
            log.error("Trying to write directly into ProxyList");
            throw new IllegalArgumentException("Trying to write directly into ProxyList");
        }
        list.add(position, value); // TODO take position into account?
    }

    @Override
    public Object get(Object destination) {
        return ((List)destination).get(position);
    }

    @Override
    public Class getType() {
        return elementClass;
    }
}
