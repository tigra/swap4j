package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.store.StorageException;
import com.github.illarion.swap4j.swap.Swap;
import com.github.illarion.swap4j.swap.UUIDGenerator;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.UUID;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public class H2ObjectStorage extends ObjectFieldStorage implements UUIDGenerator {

    public H2ObjectStorage(Swap swap, FieldStorage fieldStorage)
            throws ClassNotFoundException, SQLException {
        super(fieldStorage, null, swap);
        this.uuidGenerator = this;
    }

    /**
     * Creates unique UUID
     *
     * @return
     */
    @Override
    public UUID createUUID() {
        return ((H2FieldStorage)fieldStorage).createUUID();
    }

    /**
     * Get iterator over <code>FieldRecord</code>s stored in this <code>Store</code>
     *
     * @return iterator
     */
    @Override
    public Iterator<Locator> iterator() {
        return fieldStorage.iterator();
    }

    /**
     * Access <code>FieldRecord</code> identified by given <code>Locator</code>.
     * Used for testing purposes.
     *
     * @param locator Locator identifying the field to load
     * @return loaded <code>FieldRecord</code>
     */
    @Override
    public FieldRecord getSerializedField(Locator locator) throws StorageException {
        return fieldStorage.read(locator);
    }
}
