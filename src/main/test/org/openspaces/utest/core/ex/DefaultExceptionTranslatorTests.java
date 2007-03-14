package org.openspaces.utest.core.ex;

import junit.framework.TestCase;

import org.openspaces.core.UncategorizedGigaSpaceException;
import org.openspaces.core.UnusableEntryException;
import org.openspaces.core.exception.DefaultExceptionTranslator;
import org.openspaces.core.exception.ExceptionTranslator;
import org.springframework.dao.DataAccessException;

/**
 * @author kimchy
 */
public class DefaultExceptionTranslatorTests extends TestCase {

    private ExceptionTranslator exTranslator;

    protected void setUp() throws Exception {
        exTranslator = new DefaultExceptionTranslator();
    }

    public void testGeneralException() {
        Exception e = new Exception("test");
        DataAccessException dae = exTranslator.translate(e);
        assertEquals(dae.getClass(), UncategorizedGigaSpaceException.class);
        assertSame(e, dae.getCause());
    }

    public void testUnusableEntryException() {
        Exception cause = new Exception("cause");
        net.jini.core.entry.UnusableEntryException uee = new net.jini.core.entry.UnusableEntryException(cause);
        DataAccessException dae = exTranslator.translate(uee);
        assertEquals(dae.getClass(), UnusableEntryException.class);
        assertSame(uee, dae.getCause());
    }
}
