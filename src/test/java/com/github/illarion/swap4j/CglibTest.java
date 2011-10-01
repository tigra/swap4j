/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.illarion.swap4j;

import java.lang.reflect.Method;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author shaman
 */
@Ignore("Not a test")
public class CglibTest {

    public static class Foo {

    }

    private static class MethodInterceptorImpl implements MethodInterceptor {

        public MethodInterceptorImpl() {
        }

        @Override
        public Object intercept(Object o, Method method, Object[] os, MethodProxy mp) throws Throwable {

            if (method.getName().equals("finalize")) {
                System.err.println("FINALIZING!");
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                
                for (StackTraceElement stackTraceElement : stackTrace) {
                    System.err.println(stackTraceElement.toString());
                }
                
            }

            return mp.invokeSuper(o, os);
            
            
        }
    }

    @Test
    public void testEnhance() throws InterruptedException {

        Enhancer enhancer = new Enhancer();

        Callback callback = new MethodInterceptorImpl();

        Foo created = (Foo) Enhancer.create(Foo.class, callback);

        created = null;

        System.gc();
        
        Thread.sleep(1000);
    }
}
