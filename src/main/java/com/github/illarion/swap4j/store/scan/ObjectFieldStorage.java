package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.store.ObjectStorage;
import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.swap.ContextTracking;
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
public abstract class ObjectFieldStorage extends ContextTracking implements ObjectStorage {
    FieldStorage fieldStorage;
    ObjectScanner scanner;
    UUIDGenerator uuidGenerator;
    private Swap swap;

    @Override
    @Deprecated
    // TODO Decouple Swap from ObjectStorage
    public void setSwap(Swap swap) {
        this.swap = swap;
        fieldStorage.setSwap(swap);
    }

    protected ObjectFieldStorage(FieldStorage fieldStorage, UUIDGenerator uuidGenerator, Swap swap) {
        enter("constructor");
        this.fieldStorage = fieldStorage;
        this.scanner = new ObjectScanner(fieldStorage);
        this.uuidGenerator = uuidGenerator;
        this.swap = swap;
        exit();
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
            enter("reStore");
            final String path;
            if (ProxyList.class.isAssignableFrom(clazz)) {
                final FieldRecord<ProxyListRecord> fieldRecord = fieldStorage.read(new Locator(id, ".["));
                ProxyList proxyList = new ProxyList(swap, fieldRecord.getClazz(), id, (ProxyListRecord) fieldRecord.getValue());
                // TODO load elements, call something else instead
                return (T) proxyList;
            } else {
                List<FieldRecord> fieldRecords = fieldStorage.readAll(id);
                if (fieldRecords.size() == 0) {
                    throw new StoreException("Object not found in ObjectScannerStore.reStore(" + id + ", " + clazz);
                }
                T object = (T) fieldRecords.get(0).getValue();
                for (FieldRecord fieldRecord : fieldRecords) {
                    if (fieldRecord.getRecordType() == RECORD_TYPE.LIST_FIELD) {
                        List emptyList = ((ProxyList) fieldRecord.writeTo(object)).getRealList();
                        reStoreList(fieldRecord.getValueAsUuid(), fieldRecord.getElementClass(), emptyList);
                    } else {
                        fieldRecord.writeTo(object);
                    }
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
        } finally {
            exit();
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
                enter("store");
                fieldStorage.clean(id);
                if (object instanceof ProxyList) {
                    storeProxyList(id, (ProxyList) object, Object.class); //TODO proper elementClass
                } else {
                    scanner.scanObject(id, object);
                }
            }
        } catch (IllegalAccessException e) {
            throw new StoreException("IllegalAccessException thrown when analysing object " + object, e);
        } finally {
            exit();
        }
    }

    @Override
    public <T> void storeProxyList(UUID uuid, ProxyList proxyList, Class elementClass) throws StoreException {
        synchronized (fieldStorage) {
            enter("storeProxyList");
            fieldStorage.clean(uuid);
            scanner.scanProxyList(uuid, proxyList, elementClass);
            exit();
        }
    }

    @Override
    public <T> List<T> reStoreList(UUID uuid, Class<T> elementClass, List<T> listToRestore) throws StoreException {
        enter("reStoreList");
        try {
            if (listToRestore instanceof ProxyList) {
                throw new IllegalArgumentException("Can't restore into ProxyList");
            }
            FieldRecord listRecord = fieldStorage.read(new Locator(uuid, ".[")); // TODO ?????
//        List<T> restored = new ArrayList<T>();
            List<FieldRecord> elementRecords = fieldStorage.readElementRecords(uuid, elementClass);
            for (FieldRecord record : elementRecords) {
//            restored.add(new Proxy<T>(UUID.fromString((String)record.getValue()), this, elementClass));
                listToRestore.add(Swap.newEmptyWrapper(record.getValueAsUuid(), elementClass));
            }
            return listToRestore;
        } finally {
            exit();
        }
    }

    @Override
    protected String getContextInfo(String context) {
        return String.format("ObjectFieldStorage.%s() ", context);
    }
}
