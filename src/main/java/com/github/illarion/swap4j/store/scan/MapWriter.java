package com.github.illarion.swap4j.store.scan;

import com.google.common.collect.Lists;

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
    Map<Locator, SerializedField> serializedObjects = new HashMap<Locator, SerializedField>();
    @Override
    public void serialize(SerializedField representation) {
        serializedObjects.put(representation.getLocator(), representation);
    }
    
    SerializedField get(Locator locator) {
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
    public SerializedField read(Locator locator) {
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
    public List<SerializedField> readAll(UUID uuid) {
        ArrayList<SerializedField> fieldsRead = new ArrayList<SerializedField>();
        for (Map.Entry<Locator, SerializedField> entry : serializedObjects.entrySet()) {
            if (uuid.equals(entry.getKey().getId())) {
                fieldsRead.add(entry.getValue());
            }
        }
        Collections.sort(fieldsRead);
        return fieldsRead;
    }
}
