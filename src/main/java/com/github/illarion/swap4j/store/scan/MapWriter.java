package com.github.illarion.swap4j.store.scan;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 23, 2011 3:24:13 PM
 *
 * @author Alexey Tigarev
 */
public class MapWriter extends ObjectSerializer {
    Map<Locator, SerializedField> serializedObjects = new HashMap<Locator, SerializedField>();
    @Override
    public void serialize(SerializedField representation) {
        serializedObjects.put(representation.getLocator(), representation);
    }
    SerializedField get(Locator locator) {
        return serializedObjects.get(locator);
    }

    @Override
    public SerializedField read(Locator locator) {
        return get(locator);
    }

    @Override
    public Iterator<Locator> iterateStoredObjects() {
        return serializedObjects.keySet().iterator();
    }
}
