package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.swap.Proxy;
import com.github.illarion.swap4j.swap.ProxyList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev
 */
public class SerializedList<T> implements PreSerialized {
    private List<Proxy<T>> list = new ArrayList<Proxy<T>>();

    public SerializedList() {
    }

    public SerializedList(ProxyList proxyList) {
        list = proxyList;
    }

    public boolean add(Proxy<T> elem) {
        list.add(elem);
        return true;
    }

    public boolean addAll(Collection<? extends Proxy<T>> elems) {
        list.addAll(elems);
        return true;
    }

}
