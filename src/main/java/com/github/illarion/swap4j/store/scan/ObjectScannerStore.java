package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.store.Store;
import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.swap.ProxyList;
import com.github.illarion.swap4j.swap.Swap;
import com.github.illarion.swap4j.swap.UUIDGenerator;

import java.util.UUID;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 23, 2011 3:12:08 PM
 *
 * @author Alexey Tigarev
 */
public abstract class ObjectScannerStore implements Store {
    ObjectSerializer writer;
    ObjectScanner scanner;
    UUIDGenerator uuidGenerator;
    private Swap swap;

    public void setSwap(Swap swap) {
        this.swap = swap;
    }

    protected ObjectScannerStore(ObjectSerializer writer, UUIDGenerator uuidGenerator, Swap swap) {
        this.writer = writer;
        this.scanner = new ObjectScanner(writer);
        this.uuidGenerator = uuidGenerator;
        this.swap = swap;
    }

    /**
     * Load specified object of type T with given id.
     *
     * @param id
     * @param clazz
     * @param <T>
     * @return loaded object
     * @throws com.github.illarion.swap4j.store.StoreException
     *
     */
    @Override
    public <T> T reStore(UUID id, Class<T> clazz) throws StoreException {
        try {
            final String path;
            if (ProxyList.class.isAssignableFrom(clazz)) {
                final SerializedField<ProxyListRecord> serializedField = writer.read(new Locator(id, ".["));
                ProxyList proxyList = new ProxyList(swap, serializedField.getClass(), id, (ProxyListRecord)serializedField.getValue());
                return (T)proxyList;
            } else {
                path = ".";
                final SerializedField serializedField = writer.read(new Locator(id, path));
                if (null == serializedField) {
                    throw new StoreException("Object not found in ObjectScannerStore.reStore(" + id + ", " + clazz);
                }
                return (T) (serializedField.getValue());
            }
        } catch (ClassCastException cce) {
            throw new StoreException("ObjectScannerStore.reStore(" + id + ", " + clazz);
        }
    }

    /**
     * Store specified object of type T with given id.
     *
     * @param id
     * @param object
     * @param <T>
     * @throws com.github.illarion.swap4j.store.StoreException
     *
     */
    @Override
    public <T> void store(UUID id, T object) throws StoreException {
        try {
            scanner.scanObject(id, object);
        } catch (IllegalAccessException e) {
            throw new StoreException("IllegalAccessException thrown when analysing object " + object, e);
        }
    }
}
