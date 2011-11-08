package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.store.ObjectStorage;
import com.github.illarion.swap4j.store.RecordNotFoundException;
import com.github.illarion.swap4j.store.StorageException;
import com.github.illarion.swap4j.swap.ContextTracking;
import com.github.illarion.swap4j.swap.ProxyList;
import com.github.illarion.swap4j.swap.Swap;
import com.github.illarion.swap4j.swap.UUIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <code>ObjectStorage</code> that stores objects field-by-field in some <code>FieldStorage</code>.
 *
 * @author Alexey Tigarev
 */
public abstract class ObjectFieldStorage extends ContextTracking implements ObjectStorage {
    private final static Logger log = LoggerFactory.getLogger("ObjectFieldStorage");

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
     * @throws com.github.illarion.swap4j.store.StorageException
     *
     */
    @Override
    public <T> T reStore(UUID id, Class<T> clazz) throws StorageException {
        try {
            enter("reStore");
            final String path;
            if (ProxyList.class.isAssignableFrom(clazz)) {
//                final FieldRecord<ProxyListRecord> fieldRecord = fieldStorage.read(new Locator(id, ".["));
//                ProxyList proxyList = new ProxyList(swap, fieldRecord.getClazz(), id, (ProxyListRecord) fieldRecord.getValue());
//                // TODO load elements, call something else instead
//                return (T) proxyList;
                List listToRestore = new ArrayList();
                return (T) reStoreList(id, Object.class, listToRestore);
            } else {
                return restoreCompoundObject(id, clazz);
            }
        } catch (ClassCastException cce) {
            throw new StorageException("ObjectScannerStore.reStore(" + id + ", " + clazz, cce);
        } catch (IllegalAccessException e) {
            throw new StorageException("ObjectScannerStore.reStore(" + id + ", " + clazz, e);
        } catch (NoSuchFieldException e) {
            throw new StorageException("ObjectScannerStore.reStore(" + id + ", " + clazz, e);
        } catch (NoSuchMethodException e) {
            throw new StorageException("ObjectScannerStore.reStore(" + id + ", " + clazz, e);
        } catch (InstantiationException e) {
            throw new StorageException("ObjectScannerStore.reStore(" + id + ", " + clazz, e);
        } catch (InvocationTargetException e) {
            throw new StorageException("ObjectScannerStore.reStore(" + id + ", " + clazz, e);
        } finally {
            exit();
        }
    }

    private <T> T restoreCompoundObject(UUID id, Class<T> clazz)
            throws StorageException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException, NoSuchFieldException {
        enter("restoreCompoundObject(%s, %s)", id, clazz);
        try {
            List<FieldRecord> fieldRecords = fieldStorage.readAll(id);
            log.debug("records read: {}", fieldRecords);
            if (fieldRecords.size() == 0) {
                log.error("Object not found in ObjectScannerStore.reStore(" + id + ", " + clazz);
                throw new StorageException("Object not found in ObjectScannerStore.reStore(" + id + ", " + clazz);
            }
            FieldRecord rootRecord = fieldRecords.get(0);

            T object = createRootObject(id, clazz, rootRecord);

            for (FieldRecord fieldRecord : fieldRecords) {
                if (fieldRecord.getRecordType() == RECORD_TYPE.LIST_FIELD) {
                    List emptyList = ((ProxyList) fieldRecord.writeTo(object)).getRealList();
                    //reStoreList(fieldRecord.getValueAsUuid(), fieldRecord.getElementClass(), emptyList);
                } else {
                    fieldRecord.writeTo(object);
                }
            }
            return object;
        } finally {
            exit();
        }
//                path = ".";
//                final FieldRecord serializedField = fieldStorage.read(new Locator(id, path));
//                if (null == serializedField) {
//                    throw new StorageException("Object not found in ObjectScannerStore.reStore(" + id + ", " + clazz);
//                }
//                return (T) (serializedField.getValue());
    }

    private <T> T createRootObject(UUID id, Class<T> clazz, FieldRecord rootRecord)
            throws IllegalAccessException, StorageException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        // TODO improve code here and don't do this inside FieldStorage
        Object object = rootRecord.getValue();
        if (object instanceof String) {
            return (T) ObjectStructure.valueFromString((String) object, clazz, null, id, rootRecord.getRecordType());
        } else {
            return (T) object;
        }
    }

    /**
     * Store specified object of type T with given id.
     *
     * @param id
     * @param object
     * @param <T>
     * @throws com.github.illarion.swap4j.store.StorageException
     *
     */
    @Override
    public <T> void store(UUID id, T object) throws StorageException {
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
            throw new StorageException("IllegalAccessException thrown when analysing object " + object, e);
        } finally {
            exit();
        }
    }

    @Override
    public <T> void storeProxyList(UUID uuid, ProxyList proxyList, Class elementClass) throws StorageException {
        synchronized (fieldStorage) {
            enter("storeProxyList");
            fieldStorage.clean(uuid);
            scanner.scanProxyList(uuid, proxyList, elementClass);
            exit();
        }
    }

    @Override
    public <T> List<T> reStoreList(UUID uuid, Class<T> elementClass, List<T> listToRestore) throws StorageException {
        enter("reStoreList");
        try {
            if (listToRestore instanceof ProxyList) {
                throw new IllegalArgumentException("Can't restore into ProxyList");
            }
            try {
                FieldRecord listRecord = fieldStorage.read(new Locator(uuid, ".[")); // TODO ?????
            } catch (RecordNotFoundException rnfe) {
                log.error("List not found", rnfe); // TODO?
//                return null;
            }
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
