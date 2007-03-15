package org.openspaces.utest.core.ex;

import com.gigaspaces.converter.ConversionException;
import com.j_spaces.core.client.EntryVersionConflictException;
import junit.framework.TestCase;
import org.openspaces.core.EntryAlreadyInSpaceException;
import org.openspaces.core.EntryNotInSpaceException;
import org.openspaces.core.InternalSpaceException;
import org.openspaces.core.InvalidFifoClassException;
import org.openspaces.core.InvalidFifoTemplateException;
import org.openspaces.core.ObjectConversionException;
import org.openspaces.core.SpaceOptimisticLockingFailureException;
import org.openspaces.core.UncategorizedSpaceException;
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
        assertEquals(UncategorizedSpaceException.class, dae.getClass());
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
        DataAccessException dae = exTranslator.translate(new com.j_spaces.core.client.EntryAlreadyInSpaceException("UID", "CLASSNAME"));
        assertEquals(EntryAlreadyInSpaceException.class, dae.getClass());
        assertEquals("UID", ((EntryAlreadyInSpaceException) dae).getUID());
        assertEquals("CLASSNAME", ((EntryAlreadyInSpaceException) dae).getClassName());
    }

    public void testEntryNotInSpaceException() {
        DataAccessException dae = exTranslator.translate(new com.j_spaces.core.client.EntryNotInSpaceException("UID", "SPACENAME", false));
        assertEquals(EntryNotInSpaceException.class, dae.getClass());
        assertEquals("UID", ((EntryNotInSpaceException) dae).getUID());
        assertEquals(false, ((EntryNotInSpaceException) dae).isDeletedByOwnTxn());
    }

    public void testEntryVersionConflictException() {
        DataAccessException dae = exTranslator.translate(new EntryVersionConflictException("UID", 1, 2, "operation"));
        assertEquals(SpaceOptimisticLockingFailureException.class, dae.getClass());
    }

    public void testInvalidFifoClassException() {
        DataAccessException dae = exTranslator.translate(new com.j_spaces.core.InvalidFifoClassException("test", false, true));
        assertEquals(InvalidFifoClassException.class, dae.getClass());
        assertEquals("test", ((InvalidFifoClassException) dae).getClassName());
        assertEquals(true, ((InvalidFifoClassException) dae).isFifoClass());
    }

    public void testInvalidFifoTemplateException() {
        DataAccessException dae = exTranslator.translate(new com.j_spaces.core.InvalidFifoTemplateException("test"));
        assertEquals(InvalidFifoTemplateException.class, dae.getClass());
        assertEquals("test", ((InvalidFifoTemplateException) dae).getTemplateClassName());
    }

    public void testConversionException() {
        DataAccessException dae = exTranslator.translate(new ConversionException("test"));
        assertEquals(ObjectConversionException.class, dae.getClass());
    }

    public void testInternalSpaceExceptionOnlyWithMessage() {
        DataAccessException dae = exTranslator.translate(new net.jini.space.InternalSpaceException("test"));
        assertEquals(InternalSpaceException.class, dae.getClass());
        assertNull(((InternalSpaceException) dae).getNestedException());
    }

    public void testInternalSpaceExceptionWithUnidentifiedException() {
        DataAccessException dae = exTranslator.translate(new net.jini.space.InternalSpaceException("test", new Exception()));
        assertEquals(InternalSpaceException.class, dae.getClass());
        assertNotNull(((InternalSpaceException) dae).getNestedException());
    }

    public void testInternalSpaceExceptionWithIdentifiedException() {
        Exception cause = new Exception("cause");
        net.jini.core.entry.UnusableEntryException uee = new net.jini.core.entry.UnusableEntryException(cause);
        DataAccessException dae = exTranslator.translate(new net.jini.space.InternalSpaceException("test", uee));
        assertEquals(UnusableEntryException.class, dae.getClass());
        assertSame(uee, dae.getCause());
    }
}
