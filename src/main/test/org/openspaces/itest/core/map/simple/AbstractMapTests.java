package org.openspaces.itest.core.map.simple;

import com.j_spaces.map.IMap;
import org.openspaces.core.GigaMap;
import org.openspaces.core.SpaceTimeoutException;
import org.openspaces.core.map.LockHandle;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author kimchy
 */
public abstract class AbstractMapTests extends AbstractDependencyInjectionSpringContextTests {

    protected IMap map;

    protected GigaMap gigaMap;

    protected AbstractMapTests() {
        setPopulateProtectedVariables(true);
    }

    protected void onSetUp() throws Exception {
        gigaMap.clear(true);
    }

    public void testSimpleMapOperations() {
        map.put("1", "value");

        assertEquals("value", map.get("1"));

        assertEquals("value", map.remove("1"));
    }

    public void testSimpleGigaMapOperations() {
        gigaMap.put("1", "value");
        assertEquals("value", gigaMap.get("1"));
        assertEquals("value", gigaMap.remove("1"));
    }

    public void testSimpleLock() {
        gigaMap.put("2", "value");
        gigaMap.lock("2");
        assertTrue(gigaMap.isLocked("2"));
        try {
            gigaMap.put("2", "value1");
            fail();
        } catch (SpaceTimeoutException e) {
            // all is well, we are locked
        }
        gigaMap.unlock("2");
        gigaMap.put("2", "value2");
    }

    public void testLockOnNonExistingValue() {
        gigaMap.lock("2");
        assertTrue(gigaMap.isLocked("2"));
        try {
            gigaMap.put("2", "value1");
            fail();
        } catch (SpaceTimeoutException e) {
            // all is well, we are locked
        }
        gigaMap.unlock("2");
        gigaMap.put("2", "value2");
    }

    public void testSimpleLockWithLockHandle() {
        gigaMap.put("1", "value");
        LockHandle lockHandle = gigaMap.lock("1");
        gigaMap.put("1", "value1", lockHandle);
        try {
            gigaMap.put("1", "value2");
            fail();
        } catch (SpaceTimeoutException e) {
            // all is well, we are locked
        }
        gigaMap.unlock("1");
        assertEquals("value1", gigaMap.get("1"));
        gigaMap.put("1", "value3");
    }

    public void testMultiThreadedLockAndUnlock() {
        gigaMap.put("1", "value");
        gigaMap.lock("1");
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    gigaMap.lock("1");
                } catch (Exception e) {
                    fail();
                }
            }
        });
        thread.start();
        gigaMap.unlock("1");
    }
}
                        