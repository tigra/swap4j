package com.github.illarion.swap4j.swap;

import com.github.illarion.swap4j.store.StoreException;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;

/**
 * TODO Describe class
 * <p/>
 * <p/>
 * Created at: Sep 17, 2011 1:23:08 PM
 *
 * @author Alexey Tigarev
 */
public class UtilTest {
    class A {
        Proxy<A> parameterizedProxy;
        Proxy rawProxy;

        A(Proxy<A> parameterizedProxy, Proxy rawProxy) {
            this.parameterizedProxy = parameterizedProxy;
            this.rawProxy = rawProxy;
        }

        A(Proxy<A> parameterizedProxy) {
            this.parameterizedProxy = parameterizedProxy;
        }
        A() {
        }
    }
    @Test
    public void testGetProxyClassParameterized() throws NoSuchFieldException, StoreException {
        A a = new A(new Proxy<A>(new UUID(0,1), null, A.class));
        Field f = a.getClass().getDeclaredField("parameterizedProxy");
        assertEquals(A.class, Utils.getProxyType(f));
    }

    @Test
    public void testGetProxyClassUnparameterized() throws NoSuchFieldException, StoreException {
        A a = new A(null, new Proxy(new UUID(0,1), null, A.class));
        Field f = a.getClass().getDeclaredField("rawProxy");
        assertEquals(null, Utils.getProxyType(f));
    }

}
