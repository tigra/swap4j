package com.github.illarion.swap4j.store.scan;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public class TypeResolver {

    private Map<Integer, Type> types = new HashMap<Integer, Type>();

    /**
     * Returns <code>Type</code> for given id.
     * @param typeId
     * @return
     */
    public Type getTypeById(int typeId) {
        return null;
    }

    /**
     * Returns the Type id for given <code>Type</code>.
     * If such type is already registered, finds it and returns its id.
     * If such type is not yet registered, registers it and returns id of newly created registration record.
     * @return id of a given type
     */
    public int getIdByType(Type type) {
        Integer key = findIdByType(type);
        if (null == key) {
            key = generateKey();
            registerType(key, type);
        }
        return key;
    }

    private void registerType(Integer key, Type type) {
        types.put(key, type);
        throw new UnsupportedOperationException(""); // TODO Implement this method
    }

    private Integer generateKey() {
        throw new UnsupportedOperationException(""); // TODO Implement this method
        //return null;
    }

    private Integer findIdByType(Type type) {
        return null;
    }
}
