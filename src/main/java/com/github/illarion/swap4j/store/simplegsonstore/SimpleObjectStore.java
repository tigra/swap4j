/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.store.simplegsonstore;

import com.github.illarion.swap4j.store.ObjectStorage;
import com.github.illarion.swap4j.store.StorageException;
import com.github.illarion.swap4j.store.scan.FieldRecord;
import com.github.illarion.swap4j.store.scan.Locator;
import com.github.illarion.swap4j.swap.*;
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
import java.util.List;
import java.util.UUID;

import net.sf.cglib.proxy.Enhancer;
import org.apache.commons.io.IOUtils;

/**
 * @author shaman
 */
public class SimpleObjectStore implements ObjectStorage {
    @Override
    public void setSwap(Swap swap) {
        // TODO
    }

    private final File dir;
    private Gson gson = new GsonBuilder().create();
    private UUIDGenerator uuidGenerator = new RandomUuidGenerator();

    public SimpleObjectStore(File dir) throws StorageException {
        this.dir = dir;

        if (!dir.exists()) {
            dir.mkdir();
        }

        if (dir.isFile()) {
            throw new StorageException("For simple store it should be directory");
        }
    }

    @Override
    public <T> void store(UUID id, T t) throws StorageException {
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
     * @throws com.github.illarion.swap4j.store.StorageException in case of storage error
     */
    private <T> boolean tryToStoreProxy(UUID id, Object object) throws StorageException {
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
    public <T> T reStore(UUID id, Class<T> clazz) throws StorageException {
        System.out.println("ReStore " + id);
        BufferedReader in = null;
        try {
            in = getInputStream(id);
            String readLine = in.readLine();
            return gson.fromJson(readLine, clazz);
        } catch (IOException e) {
            throw new StorageException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    private PrintStream getOutputStream(UUID id) throws StorageException {
        try {
            if (!dir.exists()) {
                dir.mkdir();
            }
            return new PrintStream(new FileOutputStream(new File(dir, id.toString())));
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    private BufferedReader getInputStream(UUID id) throws StorageException {
        try {
            return new BufferedReader(new FileReader(new File(dir, id.toString())));
        } catch (FileNotFoundException ex) {
            throw new StorageException(ex);
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
    public FieldRecord getSerializedField(Locator locator) {
        throw new UnsupportedOperationException(""); // TODO Implement this method
    }

    @Override
    public <T> void storeProxyList(UUID uuid, ProxyList proxyList, Class elementClass) throws StorageException {
        throw new UnsupportedOperationException(""); // TODO Implement this method

    }

    @Override
    public <T> List<T> reStoreList(UUID uuid, Class<T> elementClass, List<T> restored) {
        throw new UnsupportedOperationException(""); // TODO Implement this method
    }
}
