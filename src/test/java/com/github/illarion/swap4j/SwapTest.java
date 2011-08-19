/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j;

import com.github.illarion.swap4j.swap.Proxy;
import java.util.Map;
import java.util.HashMap;
import com.github.illarion.swap4j.store.StoreService;
import com.github.illarion.swap4j.swap.Swap;
import java.util.UUID;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author shaman
 */
public class SwapTest {

    public static class Bar {

        private String value = "new";

        public void change(String change) {
            value = change;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Bar{" + "value=" + value + '}';
        }
        
        
    }
    private StoreService store = new StoreService() {

        private Map<UUID, Object> map = new HashMap<UUID, Object>();

        @Override
        public <T> void store(T t, UUID id) {
            System.out.println("Storing " + t.toString());
            map.put(id, t);
        }

        @Override
        public <T> T reStore(UUID id) {
            System.out.println("Restoring something by id = " + id);
            return (T) map.get(id);
        }
    };

    @Test
    public void testSwap() {
        Swap swap = new Swap(store);

        Proxy<Bar> proxy = swap.wrap(new Bar(), Bar.class);

        {
            Bar bar = proxy.get();
            bar.change("1");
            bar.change("2");
        }
        
        Bar baz = proxy.get();
        
        assertEquals("2",baz.getValue());
        
        

    }
}
