package com.github.illarion.swap4j.swap;

import com.github.illarion.swap4j.store.StorageException;
import com.github.illarion.swap4j.store.scan.ID;
import org.slf4j.MDC;

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
    public abstract void load() throws StorageException;

    @Override
    public abstract boolean isLoaded();

    @Override
    public abstract void unload() throws StorageException;

    @Override
    protected String getContextInfo(String context) {
        return String.format("ProxyList.%s() %s", context, ID.shortRepresentation(id));
    }

    @Override
    protected void enter(String context) {
        super.enter(context);
        MDC.put("id", null == id ? "null" : id.toString());
    }

    @Override
    protected void exit() {
        super.exit();
        MDC.remove("id");
    }

    public abstract void nullify();
}
