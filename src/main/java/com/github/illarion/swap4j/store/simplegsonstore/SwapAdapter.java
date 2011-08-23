/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.store.simplegsonstore;

import com.github.illarion.swap4j.store.Store;
import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.swap.Proxy;
import com.github.illarion.swap4j.swap.SwapPowered;
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
class SwapAdapter implements JsonSerializer<SwapPowered>, JsonDeserializer<SwapPowered> {

    private final Store store;
    private final Class clazz;


    public SwapAdapter(Store store, Class clazz) {
        this.store = store;
        this.clazz = clazz;
    }

    @Override
    public JsonElement serialize(SwapPowered src, Type typeOfSrc, JsonSerializationContext context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SwapPowered deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
