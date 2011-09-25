package com.github.illarion.swap4j.store.scan;

import java.util.Iterator;
import java.util.UUID;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 16, 2011 2:23:16 AM
 *
 * @author Alexey Tigarev
 */
public class ObjectSerializer {
    public void serialize(SerializedField representation) {
        System.out.println("Scan: " + representation);
    }

    public SerializedField read(Locator locator) {
        throw new UnsupportedOperationException(""); // TODO Implement this method
    }

    public Iterator<Locator> iterateStoredObjects() {
        throw new UnsupportedOperationException(""); // TODO Implement this method
    }
}
