/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.store.simplegsonstore;

import com.github.illarion.swap4j.store.Store;
import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.store.scan.Locator;
import com.github.illarion.swap4j.store.scan.SerializedField;
import com.github.illarion.swap4j.swap.Proxy;
import com.github.illarion.swap4j.swap.ProxyUtils;
import com.github.illarion.swap4j.swap.UUIDGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.UUID;

import net.sf.cglib.proxy.Enhancer;
import org.apache.commons.io.IOUtils;

/**
 * @author shaman
 */
public class SimpleStore implements Store {

    private final File dir;
    private Gson gson = new GsonBuilder().create();
    private UUIDGenerator uuidGenerator = new UUIDGenerator();

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
            System.out.println("Storing " + id + " " + t.toString());
            w = getOutputStream(id);

            if (tryToStoreProxy(id, t)) {
                return;
            }

            String serialized = gson.toJson(t);
            w.println(serialized);
            //System.out.println(serialized);
        } finally {
            IOUtils.closeQuietly(w);
        }
    }

    /**
     * Tries to store proxy.
     *
     * If a given object is enhanced with our enhancer
     * (which enables it to unload when references are lost) and inside it is a Proxy...
     * then we try to extract real object from proxy and store it.
     *
     * If this succeeds, returns <code>true</code>.
     *
     * If object is not enhanced or is not a proxy, it does not save it
     * and returns <code>false</code>
     *
     * @param id id to associate the object with
     * @param object object to store
     * @param <T> type of an object
     * @return <code>true</code> if this is enhanced proxy and save succeeds, <code>false</code> otherwise
     * @throws StoreException in case of storage error
     */
    private <T> boolean tryToStoreProxy(UUID id, Object object) throws StoreException {
        if (Enhancer.isEnhanced(object.getClass())) {
            Proxy<T> proxy = ProxyUtils.getProxy(object);
            if (proxy != null) {
                store(id, proxy);
                return true;
            }
        }
        return false;
    }


    @Override
    public <T> T reStore(UUID id, Class<T> clazz) throws StoreException {
        System.out.println("ReStore " + id);
        BufferedReader in = null;
        try {
            in = getInputStream(id);
            String readLine = in.readLine();
            return gson.fromJson(readLine, clazz);
        } catch (IOException e) {
            throw new StoreException(e);
        } finally {
            IOUtils.closeQuietly(in);
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

    public UUID createUUID() {
        return uuidGenerator.createUUID();
    }

    public void setUuidGenerator(UUIDGenerator uuidGenerator) {
        this.uuidGenerator = uuidGenerator;
    }

    @Override
    public Iterator<Locator> iterator() {
        throw new UnsupportedOperationException(""); // TODO Implement this method
    }

    @Override
    public SerializedField getSerializedField(Locator locator) {
        throw new UnsupportedOperationException(""); // TODO Implement this method
    }
}
