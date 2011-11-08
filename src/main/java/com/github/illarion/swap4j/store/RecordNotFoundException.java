package com.github.illarion.swap4j.store;

import com.github.illarion.swap4j.store.scan.Locator;

import java.util.UUID;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public class RecordNotFoundException extends StorageException {
    public RecordNotFoundException(Locator locator) {
        super("Can't find record", locator);
    }
    public RecordNotFoundException(UUID uuid) {
        this(new Locator(uuid, null));
    }
}
