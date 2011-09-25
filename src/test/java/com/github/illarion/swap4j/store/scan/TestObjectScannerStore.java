package com.github.illarion.swap4j.store.scan;

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
public class TestObjectScannerStore extends ObjectScannerStore {
    public TestObjectScannerStore(Swap swap, ObjectSerializer writer, UUIDGenerator uuidGenerator) {
        super(writer, uuidGenerator, swap);
    }

    @Override
    public UUID createUUID() {
        return uuidGenerator.createUUID();
    }

    @Override
    public Iterator<Locator> iterator() {
        return writer.iterateStoredObjects();
    }

    @Override
    public SerializedField getSerializedField(Locator locator) {
        return writer.read(locator);
    }

    public ObjectSerializer getWriter() {
        return writer;
    }

    public void setUuidGenerator(UUIDGenerator uuidGenerator) {
        this.uuidGenerator = uuidGenerator;
    }
}
