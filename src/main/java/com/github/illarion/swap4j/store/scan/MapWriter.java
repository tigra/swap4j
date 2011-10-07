package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.swap.Swap;

import java.util.*;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 23, 2011 3:24:13 PM
 *
 * @author Alexey Tigarev
 */
public class MapWriter implements FieldStorage {
    Map<Locator, FieldRecord> serializedObjects = new HashMap<Locator, FieldRecord>();
    @Override
    public void serialize(FieldRecord representation) {
        serializedObjects.put(representation.getLocator(), representation);
    }
    
    FieldRecord get(Locator locator) {
        return serializedObjects.get(locator);
    }

    @Override
    public boolean clean(UUID id) {
        Set<Locator> keys = serializedObjects.keySet();
        boolean somethingDeleted = false;
        Iterator<Locator> iterator = keys.iterator();
        while (iterator.hasNext()) {
            Locator locator = iterator.next();
            if (id.equals(locator.getId())) {
                iterator.remove();
                somethingDeleted = true;
            }
        }
        return somethingDeleted;
    }

    @Override
    public void remove(Locator locator) {
        serializedObjects.remove(locator);
    }

    @Override
    public FieldRecord read(Locator locator) {
        return get(locator);
    }

    @Override
    public Iterator<Locator> iterator() {
        return serializedObjects.keySet().iterator();
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
    public List<FieldRecord> readAll(UUID uuid) {
        ArrayList<FieldRecord> fieldsRead = new ArrayList<FieldRecord>();
        for (Map.Entry<Locator, FieldRecord> entry : serializedObjects.entrySet()) {
            if (uuid.equals(entry.getKey().getId())) {
                fieldsRead.add(entry.getValue());
            }
        }
        Collections.sort(fieldsRead);
        return fieldsRead;
    }

    @Override
    public void setSwap(Swap swap) {
    }
}
