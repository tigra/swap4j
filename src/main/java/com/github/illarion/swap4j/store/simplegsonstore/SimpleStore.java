/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.store.simplegsonstore;

import com.github.illarion.swap4j.store.Store;
import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.swap.Proxy;
import com.github.illarion.swap4j.swap.SwapPowered;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author shaman
 */
public class SimpleStore implements Store {

    private final File dir;

    public SimpleStore(File dir) throws StoreException {
        this.dir = dir;

        if (!dir.exists()) {
            dir.mkdir();
        }

        if (dir.isFile()) {
            throw new StoreException("For simple store it should be directory");
        }
    }

    @Override
    public <T> void store(UUID id, T t) throws StoreException {

        PrintStream w = null;
        try {
            w = getOutputStream(id);
            Gson gson = makeGson(t.getClass());
            String serialized = gson.toJson(t);
            w.println(serialized);
        } finally {
            if (null != w) {
                w.close();
            }
        }
    }

    private <T> Gson makeGson(Class<T> clazz) {
        return new GsonBuilder().registerTypeAdapter(SwapPowered.class, new SwapAdapter(this, clazz)).create();
    }

    @Override
    public <T> T reStore(UUID id, Class<T> clazz) throws StoreException {

        BufferedReader in = null;
        try {
            in = getInputStream(id);
            String readLine = in.readLine();
            Gson gson = makeGson(clazz);
            return (T) gson.fromJson(readLine, clazz);

        } catch (IOException e) {
            throw new StoreException(e);
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException ex) {
                    throw new StoreException(ex);
                }
            }
        }
    }

    private PrintStream getOutputStream(UUID id) throws StoreException {
        try {
            if (!dir.exists()) {
                dir.mkdir();
            }

            return new PrintStream(new FileOutputStream(new File(dir, id.toString())));
        } catch (IOException e) {
            throw new StoreException(e);
        }

    }

    private BufferedReader getInputStream(UUID id) throws StoreException {
        try {
            return new BufferedReader(new FileReader(new File(dir, id.toString())));
        } catch (FileNotFoundException ex) {
            throw new StoreException(ex);
        }
    }
}
