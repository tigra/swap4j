/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.store.simplegsonstore;

import com.github.illarion.swap4j.store.Store;
import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.swap.Proxy;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author shaman
 */
class ProxyAdapter implements JsonSerializer<Proxy>, JsonDeserializer<Proxy> {

    private final Store store;
    private final Class clazz;

    public static class SimpleProxy {

        private UUID id;

        public SimpleProxy setId(UUID id) {
            this.id = id;
            return this;
        }

        public UUID getId() {
            return id;
        }
    }

    public ProxyAdapter(Store store, Class clazz) {
        this.store = store;
        this.clazz = clazz;
    }

    @Override
    public JsonElement serialize(Proxy src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(new SimpleProxy().setId(src.getId()));
    }

    @Override
    public Proxy deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        SimpleProxy simpleProxy = context.deserialize(json, SimpleProxy.class);
        UUID id = simpleProxy.getId();
        Proxy proxy = null;

        proxy = new Proxy(id, store, clazz);

        return proxy;
    }
}
