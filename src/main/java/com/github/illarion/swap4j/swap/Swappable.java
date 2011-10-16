package com.github.illarion.swap4j.swap;

import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.store.scan.ID;
import de.huxhorn.lilith.logback.classic.NDC;

import java.util.UUID;

/**
 * Object that has ID and can be loaded and unloaded.
 * Can track context in NDC using <code>enter()</code> and <code>exit()</code>
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public abstract class Swappable<T> extends ContextTracking implements Locatable<T> {
    protected UUID id;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public abstract void load() throws StoreException;

    @Override
    public abstract boolean isLoaded();

    @Override
    public abstract void unload() throws StoreException;

    @Override
    protected String getContextInfo(String context) {
        return String.format("ProxyList.%s() %s", context, ID.shortRepresentation(id));
    }

    public abstract void nullify();
}
