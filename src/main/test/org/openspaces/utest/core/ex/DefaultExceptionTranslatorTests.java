package org.openspaces.utest.core.ex;

import junit.framework.TestCase;

import org.openspaces.core.EntryAlreadyInSpaceException;
import org.openspaces.core.EntryNotInSpaceException;
import org.openspaces.core.UncategorizedGigaSpaceException;
import org.openspaces.core.UnusableEntryException;
import org.openspaces.core.exception.DefaultExceptionTranslator;
import org.openspaces.core.exception.ExceptionTranslator;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;

import com.j_spaces.core.client.EntryVersionConflictException;

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
        assertEquals(UncategorizedGigaSpaceException.class, dae.getClass());
        assertSame(e, dae.getCause());
    }

    public void testUnusableEntryException() {
        Exception cause = new Exception("cause");
        net.jini.core.entry.UnusableEntryException uee = new net.jini.core.entry.UnusableEntryException(cause);
        DataAccessException dae = exTranslator.translate(uee);
        assertEquals(UnusableEntryException.class, dae.getClass());
        assertSame(uee, dae.getCause());
    }

    public void testEntryAlreadyInSpaceException() {
        DataAccessException dae = exTranslator.translate(new com.j_spaces.core.client.EntryAlreadyInSpaceException(
                "UID", "CLASSNAME"));
        assertEquals(EntryAlreadyInSpaceException.class, dae.getClass());
        assertEquals("UID", ((EntryAlreadyInSpaceException) dae).getUID());
        assertEquals("CLASSNAME", ((EntryAlreadyInSpaceException) dae).getClassName());
    }

    public void testEntryNotInSpaceException() {
        DataAccessException dae = exTranslator.translate(new com.j_spaces.core.client.EntryNotInSpaceException("UID",
                "SPACENAME", false));
        assertEquals(EntryNotInSpaceException.class, dae.getClass());
        assertEquals("UID", ((EntryNotInSpaceException) dae).getUID());
        assertEquals(false, ((EntryNotInSpaceException) dae).isDeletedByOwnTxn());
    }

    public void testEntryVersionConflictException() {
        DataAccessException dae = exTranslator.translate(new EntryVersionConflictException("UID", 1, 2, "operation"));
        assertEquals(OptimisticLockingFailureException.class, dae.getClass());
    }
}
