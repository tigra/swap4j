package com.github.illarion.swap4j.swap;

import com.github.illarion.swap4j.store.StorageException;

import java.util.UUID;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 19, 2011 10:47:24 PM
 *
 * @author Alexey Tigarev
 */
public interface Locatable<T> {
    void load() throws StorageException;

    UUID getId();

    boolean isLoaded();

    void unload() throws StorageException;
}
