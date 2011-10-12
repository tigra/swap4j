package com.github.illarion.swap4j.store.scan;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 16, 2011 1:51:34 PM
 *
 * @author Alexey Tigarev
 */
public enum RECORD_TYPE {
    /**
     * Primitive field of an object.
     * E.g. String A.a
     */
    PRIMITIVE_FIELD,
    /**
     * "Standalone" primitive value.
     * E.g. "5"
     */
    PRIMITIVE_VALUE,
    /**
     * Field of an object that is a Proxy
     */
    PROXIED_FIELD,
    /**
     * "Standalone" proxied object.
     * E.g. new Proxy<Baz>(new Baz())
     * swap.wrap(new Baz())
     */
    PROXIED_VALUE,
    COMPOUND_FIELD,
    COMPOUND_VALUE,
    /**
     * Proxy List
     */
    PROXY_LIST,
    /**
     * List element: Proxy inside certain ProxyList
     */
    LIST_ELEMENT,
    LIST_FIELD,
    PROXY_SET,
    SET_VALUE,
    SET_FIELD,
    PROXY_MAP,
    MAP_VALUE,
    MAP_FIELD
}
