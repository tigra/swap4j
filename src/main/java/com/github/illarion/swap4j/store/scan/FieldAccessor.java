package com.github.illarion.swap4j.store.scan;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public interface FieldAccessor { // TODO <T> ?
    public void set(Object destination, Object value); // TODO throws
    public Object get(Object destination);
    public Class getType();
}
