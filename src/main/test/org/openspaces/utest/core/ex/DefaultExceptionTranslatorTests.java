package org.openspaces.utest.core.ex;

import junit.framework.TestCase;
import org.openspaces.core.GigaSpaceException;
import org.openspaces.core.exception.DefaultExceptionTranslator;
import org.openspaces.core.exception.ExceptionTranslator;

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
        GigaSpaceException gse = exTranslator.translate(e);
        assertSame(e, gse.getCause());
    }
}
