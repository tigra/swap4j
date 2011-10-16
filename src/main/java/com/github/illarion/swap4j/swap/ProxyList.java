/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.swap;

import com.github.illarion.swap4j.store.ObjectStorage;
import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.store.scan.ID;
import com.github.illarion.swap4j.store.scan.ProxyListRecord;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author shaman
 */
public class ProxyList<T> extends Swappable<T> implements List<T> {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger("ProxyList");

    private List<T> list = new ArrayList<T>();
    private final Swap swap;
    private ObjectStorage objectStore;
    private final Class<T> elementClass;
    private int elementCount;
    private boolean loaded = true;

    @Deprecated
    public ProxyList(Swap swap, Class<T> elementClass, UUID id, ProxyListRecord proxyListRecord) throws StoreException {
        enter("constructor ProxyList(%s, %s, %s, %s)", swap, elementClass, id, proxyListRecord);
        try {
            this.swap = swap;
            this.elementClass = elementClass;
            this.id = id;
            this.objectStore = swap.getStore();
            createProxies(proxyListRecord);
            elementCount = list.size();
            Swap.register(this);
        } finally {
            exit();
        }
    }

    public Class<T> getElementClass() {
        return elementClass;
    }

    @Deprecated
    private void createProxies(ProxyListRecord proxyListRecord) throws StoreException {
        for (Object listElementId : proxyListRecord) {
//            list.add(new Proxy((UUID)listElementId, swap.getStore(), clazz));
//            list.add(swap.wrap())
            list.add(emptyProxy((UUID) listElementId, elementClass));
        }
    }

    private T emptyProxy(UUID uuid, Class<T> clazz) throws StoreException {
        return (T) new Proxy(uuid, objectStore, clazz).get();
    }

    public ProxyList(Swap swap, Class<T> elementClass) throws StoreException {
        enter("constructor ProxyList(%s, %s)", swap, elementClass);
        this.swap = swap;
        this.elementClass = elementClass;
        this.objectStore = swap.getStore();
        this.id = objectStore.createUUID();
        this.elementCount = 0;
        unload();
        Swap.register(this);
        exit();
    }

    public ProxyList(Swap swap, Class<T> elementClass, UUID uuid) throws StoreException {
        this(swap, elementClass, uuid, true);
    }

    public ProxyList(Swap swap, Class<T> elementClass, UUID uuid, boolean doUnload) throws StoreException {
        enter("constructor ProxyList(swap=%s, elementClass=%s, uuid=%s, doUnload=%s)", swap, uuid, doUnload);
        this.elementClass = elementClass;
        this.id = uuid;
        this.swap = swap;
        this.elementCount = 0;
        this.objectStore = swap.getStore();
        load(); // TODO ?
        this.elementCount = list.size();
        if (doUnload) {
            unload();
        }
        Swap.register(this);
        exit();
    }

    @Override
    public void load() throws StoreException {
        // TODO implement loading
        synchronized (id) {
            enter("load()");
            try {
                List<T> newList = new ArrayList<T>();
                objectStore.reStoreList(id, elementClass, newList);
                log.debug("Old list: {}", list);
                log.debug("New list: {}", newList);
                if (list.size() != newList.size()) {
                    log.info("list changed!");
                }
                if (null != newList && newList.size() > 0) {
                    list = newList; // TODO remove this dirty hack
                }
                loaded = true;
                elementCount = list.size();
            } finally {
                exit();
            }
        }
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public void unload() throws StoreException {
        try {
            enter("unload");
            log.debug("unload(), " + listInfo());
            swap.getStore().storeProxyList(id, this, this.elementClass);
            list = null;  // todo TEST
            loaded = false;
        } finally {
            exit();
        }
    }

    private String listInfo() {
        return String.format("ProxyList<%s>(%s, size=%d)", shortElementClassName(), ID.shortRepresentation(id), list.size());
    }

    private String shortElementClassName() {
        return null == elementClass ? "null" : elementClass.getSimpleName();
    }

    @Override
    public boolean add(T e) {
        enter("add(" + e + ")");
        try {
            tryToLoad();
            T wrapped = swap.wrap(e, elementClass);
            log.debug("Old list: {}", list);
            log.debug("adding {}", wrapped);
            log.debug("New list: {}", list);
            boolean result = list.add(wrapped);
            return result;
        } catch (StoreException ex) {
            log.error("Error adding " + e + " to ProxyList with id=" + id, ex);
        } finally {
            exit();
        }
        return false;
    }

    private void tryToLoad() {
        synchronized (id) {
            try {
                if (!loaded) {
                    load();
                }
            } catch (StoreException se) {
                log.error("Error loading ProxyList with id=" + id, se);
                throw new IllegalStateException("Can't load ProxyList: " + this, se);
            }
        }
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        for (T t : collection) {
            add(t);
            elementCount++;
        }
        return true;
    }

    @Override
    public void add(int i, T t) {
        enter("add(%d, %s)", i, t);
        try {
            tryToLoad();
            list.add(i, t);
            elementCount++;
        } finally {
            exit();
        }
    }

    @Override
    public int size() {
        return elementCount;
//        enter("size");
//        int s = list.size();
//        exit();
//        return s;
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        enter("contains");
        try {
            tryToLoad();
            return list.contains(o);
        } finally {
            exit();
        }
    }

    @Override
    public Iterator<T> iterator() {
        enter("iterator");
        try {
            tryToLoad();
            Iterator<T> iterator = list.iterator();
            return iterator;
        } finally {
            exit();
        }
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
        enter("containsAll(%s)", objects);
        try {
            tryToLoad();
            return list.containsAll(objects);
        } finally {
            exit();
        }
    }

    @Override
    public boolean addAll(int i, Collection<? extends T> ts) {
        enter("addAll(%d, %s)", i, ts);
        try {
            tryToLoad();
            return list.addAll(i, ts);
        } finally {
            exit();
        }
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
        enter("get");
        try {
            tryToLoad();
            return list.get(i);
        } finally {
            exit();
        }
    }

    @Override
    public T set(int i, T t) {
        enter("set(%d, %s)", i, t);
        try {
            tryToLoad();
            return list.set(i, t);
        } finally {
            exit();
        }
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
        tryToLoad();
        return list.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int i) {
        tryToLoad();
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

        if (elementClass != null ? !elementClass.equals(proxyList.elementClass) : proxyList.elementClass != null)
            return false;
        if (id != null ? !id.equals(proxyList.id) : proxyList.id != null) return false;
        if (listsEqual(proxyList)) return false;
        if (swap != null ? !swap.equals(proxyList.swap) : proxyList.swap != null) return false;

        return true;
    }

    private boolean listsEqual(ProxyList proxyList) {
        if (null == list) {
            return null == proxyList.list;
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
                ", size=" + size() +
                ", swap=" + swap +
                ", id=" + id +
                '}';
    }

    /**
     * Reserved for swap4j internal use: we may need to bypass regular methods that do loading/unloading
     *
     * @return
     */
    public Iterable<T> internalIterable() {
        return list;
    }

    public List getRealList() {
        return list;
    }

    @Override
    public void nullify() {
        list = null;
        id = null;
        objectStore = null;
    }
}
