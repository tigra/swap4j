package com.github.illarion.swap4j.store.scan;

import java.util.List;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public class ListElementAccessor implements FieldAccessor {
//    private List list;
    private int position;
    private Class elementClass;

    public ListElementAccessor(int position, Class elementClass) {
        this.position = position;
        this.elementClass = elementClass;
    }

    @Override
    public void set(Object destination, Object value) {
        ((List)destination).add(position, value); // TODO take position into account?
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
