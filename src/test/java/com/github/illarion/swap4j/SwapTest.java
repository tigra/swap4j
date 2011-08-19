/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j;

import java.util.Set;
import java.util.List;
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

        public Bar(String value) {
            this.value = value;
        }

        public Bar() {
        }

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
    public void testSwapSingleValue() {
        Swap swap = new Swap(store);

        Bar bar = swap.wrap(new Bar("new"), Bar.class);

        bar.change("old");
        bar.change("too old");

        assertEquals("too old", bar.getValue());
    }

    @Test
    public void testSwapList() {
        Swap swap = new Swap(store);

        List<Bar> list = swap.newWrapList(Bar.class);

        list.add(new Bar("1"));
        list.add(new Bar("2"));
        list.add(new Bar("3"));

        list.get(1).change("5");

    }

    @Test
    public void testSwapSet() {
        Swap swap = new Swap(store);

        Set<Bar> set = swap.newWrapSet(Bar.class);

        set.add(new Bar("1"));
        set.add(new Bar("1"));
        set.add(new Bar("3"));

        set.iterator().next().change("5");
        
        

    }
}
