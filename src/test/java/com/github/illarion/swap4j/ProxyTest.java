/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author shaman
 */
@Ignore("Not a test")
public class ProxyTest {

    public static interface Foo {
        
        public void doSomething();
        
    }

    @Test
    public void test() throws IOException {

        InvocationHandler handler = new InvocationHandler() {

            @Override
            public Object invoke(Object o, Method method, Object[] os) throws Throwable {
                System.out.println(method + "does nothing!!");
                return null;
            }
        };


        Foo f = (Foo) Proxy.newProxyInstance(Foo.class.getClassLoader(),
                new Class[]{Foo.class},
                handler);
        
        f.doSomething();


    }
}
