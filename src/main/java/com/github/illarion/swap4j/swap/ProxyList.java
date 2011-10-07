/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.swap;

import com.github.illarion.swap4j.store.ObjectStorage;
import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.store.scan.ProxyListRecord;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author shaman
 */
public class ProxyList<T> implements List<T>, Locatable<T> {

    private List<T> list = new ArrayList<T>();

    private final Swap swap;
    private ObjectStorage objectStore;

    public Class<T> getElementClass() {
        return elementClass;
    }

    private final Class<T> elementClass;

    public ProxyList(Swap swap, Class<T> elementClass, UUID id, ProxyListRecord proxyListRecord) throws StoreException {
        this.swap = swap;
        this.elementClass = elementClass;
        this.id = id;
        this.objectStore = swap.getStore();
        createProxies(proxyListRecord);
    }

    private void createProxies(ProxyListRecord proxyListRecord) throws StoreException {
        for (Object listElementId : proxyListRecord) {
//            list.add(new Proxy((UUID)listElementId, swap.getStore(), clazz));
//            list.add(swap.wrap())
            list.add(emptyProxy((UUID)listElementId, elementClass));
        }
    }

    private T emptyProxy(UUID uuid, Class<T> clazz) throws StoreException {
        return (T)new Proxy(uuid, objectStore, clazz).get();
    }


    private static final Logger logger = Logger.getLogger(ProxyList.class.getName());

    private UUID id;

    public ProxyList(Swap swap, Class<T> elementClass) throws StoreException {
        this.swap = swap;
        this.elementClass = elementClass;
        this.objectStore = swap.getStore();
        this.id = objectStore.createUUID();
        unload();
    }

    public ProxyList(Swap swap, Class<T> elementClass, UUID id) throws StoreException {
        this.elementClass = elementClass;
        this.id = id;
        this.swap = swap;
        this.objectStore = swap.getStore();
        unload();
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void load() throws StoreException {
        // TODO implement loading
    }

    @Override
    public boolean isLoaded() {
        return true; // TODO implement unloading
    }

    @Override
    public void unload() throws StoreException {
        swap.getStore().storeList(id, this, this.elementClass);
    }

    @Override
    public boolean add(T e) {
        T wrapped;
        try {
            wrapped = swap.wrap(e, elementClass);
            return list.add(wrapped);
        } catch (StoreException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        for (T t : collection) {
            add(t);
        }
        return true;
    }

    @Override
    public void add(int i, T t) {
        list.add(i, t);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        return list.toArray(ts);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> objects) {
        return list.containsAll(objects);
    }

    @Override
    public boolean addAll(int i, Collection<? extends T> ts) {
        return list.addAll(i, ts);
    }

    @Override
   public boolean removeAll(Collection<?> objects) {
        return list.removeAll(objects);
    }

    @Override
    public boolean retainAll(Collection<?> objects) {
        return list.retainAll(objects);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public T get(int i) {
        return list.get(i);
    }

    @Override
    public T set(int i, T t) {
        return list.set(i, t);
    }

    @Override
    public T remove(int i) {
        return list.remove(i);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int i) {
        return list.listIterator(i);
    }

    @Override
    public List<T> subList(int i, int i1) {
        return list.subList(i, i1);
    }

    @SuppressWarnings({"RedundantIfStatement"})
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProxyList proxyList = (ProxyList) o;

        if (elementClass != null ? !elementClass.equals(proxyList.elementClass) : proxyList.elementClass != null) return false;
        if (id != null ? !id.equals(proxyList.id) : proxyList.id != null) return false;
        if (listsEqual(proxyList)) return false;
        if (swap != null ? !swap.equals(proxyList.swap) : proxyList.swap != null) return false;

        return true;
    }

    private boolean listsEqual(ProxyList proxyList) {
        if (list == null) {
            return proxyList.list == null;
        } else {
            return list.equals(proxyList.list);
        }
    }

    private boolean listsUnEqual(ProxyList proxyList) {
        return list == null ? proxyList.list != null : !list.equals(proxyList.list);
    }

    @Override
    public int hashCode() {
        int result = list != null ? list.hashCode() : 0;
        result = 31 * result + (swap != null ? swap.hashCode() : 0);
        result = 31 * result + (elementClass != null ? elementClass.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return "ProxyList{" +
                "clazz=" + elementClass +
                ", size=" + ((null == list) ? "null" : list.size()) +
                ", swap=" + swap +
                ", id=" + id +
                '}';
    }

}
