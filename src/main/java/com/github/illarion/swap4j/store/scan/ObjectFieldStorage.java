package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.store.ObjectStorage;
import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.swap.ProxyList;
import com.github.illarion.swap4j.swap.Swap;
import com.github.illarion.swap4j.swap.UUIDGenerator;

import java.util.List;
import java.util.UUID;

/**
 * <code>ObjectStorage</code> that stores objects field-by-field in some <code>FieldStorage</code>.
 *
 * @author Alexey Tigarev
 */
public abstract class ObjectFieldStorage implements ObjectStorage {
    FieldStorage fieldStorage;
    ObjectScanner scanner;
    UUIDGenerator uuidGenerator;
    private Swap swap;

    @Override
    @Deprecated // TODO Decouple Swap from ObjectStorage
    public void setSwap(Swap swap) {
        this.swap = swap;
        fieldStorage.setSwap(swap);
    }

    protected ObjectFieldStorage(FieldStorage fieldStorage, UUIDGenerator uuidGenerator, Swap swap) {
        this.fieldStorage = fieldStorage;
        this.scanner = new ObjectScanner(fieldStorage);
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
                final FieldRecord<ProxyListRecord> fieldRecord = fieldStorage.read(new Locator(id, ".["));
                ProxyList proxyList = new ProxyList(swap, fieldRecord.getClazz(), id, (ProxyListRecord) fieldRecord.getValue());
                return (T)proxyList;
            } else {
                List<FieldRecord> fieldRecords = fieldStorage.readAll(id);
                if (fieldRecords.size() == 0) {
                    throw new StoreException("Object not found in ObjectScannerStore.reStore(" + id + ", " + clazz);
                }
                T object = (T) fieldRecords.get(0).getValue();
                for (FieldRecord fieldRecord : fieldRecords) {
                    fieldRecord.writeTo(object);
                }
                return object;
//                path = ".";
//                final FieldRecord serializedField = fieldStorage.read(new Locator(id, path));
//                if (null == serializedField) {
//                    throw new StoreException("Object not found in ObjectScannerStore.reStore(" + id + ", " + clazz);
//                }
//                return (T) (serializedField.getValue());
            }
        } catch (ClassCastException cce) {
            throw new StoreException("ObjectScannerStore.reStore(" + id + ", " + clazz, cce);
        } catch (IllegalAccessException e) {
            throw new StoreException("ObjectScannerStore.reStore(" + id + ", " + clazz, e);
        } catch (NoSuchFieldException e) {
            throw new StoreException("ObjectScannerStore.reStore(" + id + ", " + clazz, e);
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
            synchronized (fieldStorage) {
                fieldStorage.clean(id);
                if (object instanceof ProxyList) {
                    storeList(id, (ProxyList)object, Object.class); //TODO proper elementClass
                } else {
                    scanner.scanObject(id, object);
                }
            }
        } catch (IllegalAccessException e) {
            throw new StoreException("IllegalAccessException thrown when analysing object " + object, e);
        }
    }

    @Override
    public <T> void storeList(UUID uuid, ProxyList proxyList, Class elementClass) throws StoreException {
        synchronized (fieldStorage) {
            fieldStorage.clean(uuid);
            scanner.scanObject(proxyList, elementClass);
        }
    }
}
