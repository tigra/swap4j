package com.github.illarion.swap4j.store.scan;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 16, 2011 1:51:34 PM
 *
 * @author Alexey Tigarev
 */
public enum TYPE {
    PRIMITIVE_FIELD,
    PRIMITIVE_VALUE,
    /**
     * Field of an object that is a Proxy
     */
    PROXIED_FIELD,
    PROXIED_VALUE,
    COMPOUND_FIELD,
    COMPOUND_VALUE,
    PROXY_LIST,
    /**
     * Proxy inside certain ProxyList
     */
    LIST_VALUE,
    LIST_FIELD,
    PROXY_SET,
    SET_VALUE,
    SET_FIELD,
    PROXY_MAP,
    MAP_VALUE,
    MAP_FIELD
}
