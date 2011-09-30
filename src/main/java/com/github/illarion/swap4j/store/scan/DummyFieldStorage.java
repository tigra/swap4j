package com.github.illarion.swap4j.store.scan;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 16, 2011 2:23:16 AM
 *
 * @author Alexey Tigarev
 */
public abstract class DummyFieldStorage implements FieldStorage {
    @Override
    public void serialize(SerializedField representation) {
        System.out.println("Scan: " + representation);
    }

    @Override
    public SerializedField read(Locator locator) {
        throw new UnsupportedOperationException(""); // TODO Implement this method
    }

    @Override
    public Iterator<Locator> iterator() {
        throw new UnsupportedOperationException(""); // TODO Implement this method
    }

    /**
     * Read all fields of object identified by given <code>uuid</code>.
     * Fields are returned in such order that will allow to recreate it in that order.
     * I.e. "./fieldA" will always be before "./fieldA/field1".
     *
     * @param uuid UUID identifying object
     * @return all fields of object in order that allow to recreate it
     */
    @Override
    public List<SerializedField> readAll(UUID uuid) {
        throw new UnsupportedOperationException(""); // TODO Implement this method
        //return null;
    }
}
