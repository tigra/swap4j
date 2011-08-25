/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.store.simplegsonstore;

import com.github.illarion.swap4j.store.Store;
import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.swap.Proxy;
import com.github.illarion.swap4j.swap.SwapCallback;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.cglib.asm.Type;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackHelper;
import net.sf.cglib.proxy.Enhancer;
import sun.font.Type1Font;

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
            System.out.println("Storing " + id + " " + t.toString());
            w = getOutputStream(id);




            if (Enhancer.isEnhanced(t.getClass())) {
                Method[] methods = t.getClass().getMethods();

                try {
                    Method getCallback = t.getClass().getMethod("getCallbacks", new Class[]{});

                    Callback[] callbacks = (Callback[]) getCallback.invoke(t, new Object[]{});

                    Callback callback = callbacks[0];

                    Method getProxy = callback.getClass().getMethod("getProxy", new Class[]{});

                    Proxy proxy = (Proxy) getProxy.invoke(callback, new Object[]{});

                    store(id, proxy);
                    return;

                    
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(SimpleStore.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(SimpleStore.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(SimpleStore.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(SimpleStore.class.getName()).log(Level.SEVERE, null, ex);
                }


            }

            Gson gson = makeGson(t.getClass());


            String serialized = gson.toJson(t);
            w.println(serialized);
            //System.out.println(serialized);
        } finally {
            if (null != w) {
                w.close();
            }
        }
    }

    private <T> Gson makeGson(Class<T> clazz) {
        return new GsonBuilder().create();
    }

    @Override
    public <T> T reStore(UUID id, Class<T> clazz) throws StoreException {
        System.out.println("ReStorg " + id);
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
