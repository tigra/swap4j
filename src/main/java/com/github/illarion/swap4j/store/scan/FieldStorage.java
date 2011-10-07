package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.swap.Swap;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 29, 2011 7:30:37 PM
 *
 * @author Alexey Tigarev
 */
public interface FieldStorage extends Iterable<Locator> {
    void serialize(FieldRecord representation);

    FieldRecord read(Locator locator);

    /**
     * Read all fields of object identified by given <code>uuid</code>.
     * Fields are returned in such order that will allow to recreate it in that order.
     * I.e. "./fieldA" will always be before "./fieldA/field1".
     *
     * @param uuid UUID identifying object
     * @return all fields of object in order that allow to recreate it
     */
    List<FieldRecord> readAll(UUID uuid);

    @Override
    Iterator<Locator> iterator();

    /**
     * Remove all fields of object identified by given <code>uuid</code>
     * @param uuid UUID of object to be deleted
     * @return <code>true</code> if something deleted, <code>false</code> otherwise
     */
    boolean clean(UUID uuid);

    /**
     * Remove particular field identified by a given <code>locator</code>
     * @param locator which field to remove
     */
    void remove(Locator locator);

    @Deprecated
    void setSwap(Swap swap);
}
