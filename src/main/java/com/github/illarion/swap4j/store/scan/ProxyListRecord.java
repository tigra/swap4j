package com.github.illarion.swap4j.store.scan;

import org.hamcrest.internal.ArrayIterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 25, 2011 8:45:31 PM
 *
 * @author Alexey Tigarev
 */
public class ProxyListRecord implements Iterable {
    private UUID[] uuids;
    public ProxyListRecord(UUID... uuids) {
        this.uuids = uuids;
    }
    public Iterator iterator() {
        return new ArrayIterator(uuids);
    }

}
