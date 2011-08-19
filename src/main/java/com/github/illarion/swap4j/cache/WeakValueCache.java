/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j.cache;

import java.util.Map;
import com.google.common.collect.MapMaker;


/**
 *
 * @author shaman
 */

public abstract class WeakValueCache<R, T> implements Cache<R, T> {

        private final Map<R, T> referenceMap = new MapMaker().softValues().makeMap();

        private final Object lock = new Object();

        @Override
        public T getObject(final R id) throws Exception {

                synchronized (lock) {

                        final T t = referenceMap.get(id);

                        if (null == t) {
                                final T instance = createInstance(id);
                                referenceMap.put(id, instance);
                                return instance;
                        }

                        return t;
                }

        }

        protected abstract T createInstance(R id) throws Exception;

}