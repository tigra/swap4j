package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.store.StorageException;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public class FieldRestoreException extends StorageException {
    public FieldRestoreException(Throwable e) {
        super(e);
    }
    public FieldRestoreException(String string, Throwable throwable) {
        super(string, throwable);
    }
}
