/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author shaman
 */
public class UsageScenario {

    static class Foo {

        byte[] consume = new byte[1024 * 1024 * 5];
        List<Bar> bars;

        public Foo() {
            new Random().nextBytes(consume);
        }

        public void setBars(List<Bar> bars) {
            this.bars = bars;
        }

        public List<Bar> getBars() {
            return bars;
        }

        public byte[] getConsume() {
            return consume;
        }
    }

    static class Bar {

        byte[] consume = new byte[1024 * 1024 * 5];

        public Bar() {
            new Random().nextBytes(consume);
        }

        public byte[] getConsume() {
            return consume;
        }
    }

    @Test
    public void fooShouldConsumeMemory() throws Exception {
        long totalMemory1 = Runtime.getRuntime().freeMemory();

        Foo foo = new Foo();

        {
            final Bar bar = new Bar();

            foo.setBars(new ArrayList<Bar>() {

                {
                    add(bar);
                }
            });
        } //bar link lost here

        long totalMemory2 = Runtime.getRuntime().freeMemory();

        assertTrue(totalMemory2 < totalMemory1);

        foo = null;

        System.gc();

        long totalMemory3 = Runtime.getRuntime().freeMemory();

        assertTrue(totalMemory3 >= totalMemory1);

    }

    @Test
    public void fooShouldNotConsumeMemoryBeingWrapped() {

        Holder<String, Foo> holder = new Holder<String, Foo>("abc") {};
        
        
        
        
        
        
    }
}
