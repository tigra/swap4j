package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.store.StorageException;
import com.github.illarion.swap4j.swap.Swap;
import com.github.illarion.swap4j.swap.UUIDGenerator;

import java.util.Iterator;
import java.util.UUID;

/**
* TODO Describe class
* <p/>
* <p/>
* Created at: Sep 24, 2011 10:39:27 PM
*
* @author Alexey Tigarev
*/
public class TestObjectScannerObjectStorage extends ObjectFieldStorage {
    public TestObjectScannerObjectStorage(Swap swap, FieldStorage writer, UUIDGenerator uuidGenerator) {
        super(writer, uuidGenerator, swap);
    }

    @Override
    public UUID createUUID() {
        return uuidGenerator.createUUID();
    }

    @Override
    public Iterator<Locator> iterator() {
        return fieldStorage.iterator();
    }

    @Override
    public FieldRecord getSerializedField(Locator locator) throws StorageException {
        return fieldStorage.read(locator);
    }

    public FieldStorage getWriter() {
        return fieldStorage;
    }

    public void setUuidGenerator(UUIDGenerator uuidGenerator) {
        this.uuidGenerator = uuidGenerator;
    }
}
