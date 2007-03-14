package org.openspaces.utest.core;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.LeaseContext;
import com.j_spaces.core.client.ReadModifiers;
import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Constraint;
import org.openspaces.core.DefaultGigaSpace;
import org.openspaces.core.exception.ExceptionTranslator;
import org.openspaces.core.transaction.TransactionProvider;
import org.springframework.transaction.TransactionDefinition;

/**
 * A set of mock tests verifies that the correct {@link com.j_spaces.core.IJSpace} API is
 * called as a result of {@link org.openspaces.core.DefaultGigaSpace} execution.
 *
 * @author kimchy
 */
public class DefaultGigaSpacesTests extends MockObjectTestCase {

    private DefaultGigaSpace gs;

    private Mock mockIJSpace;
    private Mock mockTxProvider;
    private Mock mockExTranslator;

    protected void setUp() throws Exception {
        mockIJSpace = mock(IJSpace.class);
        mockTxProvider = mock(TransactionProvider.class);
        mockExTranslator = mock(ExceptionTranslator.class);

        gs = new DefaultGigaSpace((IJSpace) mockIJSpace.proxy(), (TransactionProvider) mockTxProvider.proxy(),
                (ExceptionTranslator) mockExTranslator.proxy(), TransactionDefinition.ISOLATION_DEFAULT);
    }

    public void testReadOperation() {
        Object template = new Object();
        Object retVal = new Object();

        mockIJSpace.expects(once()).method("read").with(same(template), NULL, eq(JavaSpace.NO_WAIT), eq(ReadModifiers.READ_COMMITTED))
                .will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs));
        mockTxProvider.expects(once()).method("getCurrentTransactionIsolationLevel").with(eq(gs)).will(returnValue(TransactionDefinition.ISOLATION_READ_COMMITTED));

        Object actualRetVal = gs.read(template);

        assertEquals(retVal, actualRetVal);
    }

    public void testReadOperationWithDefaultTimeout() {
        Object template = new Object();
        Object retVal = new Object();

        mockIJSpace.expects(once()).method("read").with(same(template), NULL, eq(10l), eq(ReadModifiers.READ_COMMITTED))
                .will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs));
        mockTxProvider.expects(once()).method("getCurrentTransactionIsolationLevel").with(eq(gs)).will(returnValue(TransactionDefinition.ISOLATION_READ_COMMITTED));

        gs.setDefaultReadTimeout(10l);
        Object actualRetVal = gs.read(template);

        assertEquals(retVal, actualRetVal);
    }

    public void testReadOperationWithTimeoutParameter() {
        Object template = new Object();
        Object retVal = new Object();

        mockIJSpace.expects(once()).method("read").with(same(template), NULL, eq(11l), eq(ReadModifiers.READ_COMMITTED))
                .will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs));
        mockTxProvider.expects(once()).method("getCurrentTransactionIsolationLevel").with(eq(gs)).will(returnValue(TransactionDefinition.ISOLATION_READ_COMMITTED));

        Object actualRetVal = gs.read(template, 11l);

        assertEquals(retVal, actualRetVal);
    }

    public void testReadIfExistsOperation() {
        Object template = new Object();
        Object retVal = new Object();

        mockIJSpace.expects(once()).method("readIfExists").with(same(template), NULL, eq(JavaSpace.NO_WAIT), eq(ReadModifiers.READ_COMMITTED))
                .will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs));
        mockTxProvider.expects(once()).method("getCurrentTransactionIsolationLevel").with(eq(gs)).will(returnValue(TransactionDefinition.ISOLATION_READ_COMMITTED));

        Object actualRetVal = gs.readIfExists(template);

        assertEquals(retVal, actualRetVal);
    }

    public void testReadIfExistsOperationWithDefaultTimeout() {
        Object template = new Object();
        Object retVal = new Object();

        mockIJSpace.expects(once()).method("readIfExists").with(same(template), NULL, eq(10l), eq(ReadModifiers.READ_COMMITTED))
                .will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs));
        mockTxProvider.expects(once()).method("getCurrentTransactionIsolationLevel").with(eq(gs)).will(returnValue(TransactionDefinition.ISOLATION_READ_COMMITTED));

        gs.setDefaultReadTimeout(10l);
        Object actualRetVal = gs.readIfExists(template);

        assertEquals(retVal, actualRetVal);
    }

    public void testReadIfExistsOperationWithTimeoutParameter() {
        Object template = new Object();
        Object retVal = new Object();

        mockIJSpace.expects(once()).method("readIfExists").with(same(template), NULL, eq(11l), eq(ReadModifiers.READ_COMMITTED))
                .will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs));
        mockTxProvider.expects(once()).method("getCurrentTransactionIsolationLevel").with(eq(gs)).will(returnValue(TransactionDefinition.ISOLATION_READ_COMMITTED));

        Object actualRetVal = gs.readIfExists(template, 11l);

        assertEquals(retVal, actualRetVal);
    }

    public void testReadMultipleOperation() {
        Object template = new Object();
        Object[] retVal = new Object[]{new Object(), new Object()};

        mockIJSpace.expects(once()).method("readMultiple").with(same(template), NULL, eq(2), eq(ReadModifiers.READ_COMMITTED))
                .will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs));
        mockTxProvider.expects(once()).method("getCurrentTransactionIsolationLevel").with(eq(gs)).will(returnValue(TransactionDefinition.ISOLATION_READ_COMMITTED));

        Object actualRetVal = gs.readMultiple(template, 2);

        assertEquals(retVal, actualRetVal);
    }

    public void testTakeOperation() {
        Object template = new Object();
        Object retVal = new Object();

        mockIJSpace.expects(once()).method("take").with(same(template), NULL, eq(JavaSpace.NO_WAIT))
                .will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs));

        Object actualRetVal = gs.take(template);

        assertEquals(retVal, actualRetVal);
    }

    public void testTakeOperationWithDefaultTimeout() {
        Object template = new Object();
        Object retVal = new Object();

        mockIJSpace.expects(once()).method("take").with(same(template), NULL, eq(10l))
                .will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs));

        gs.setDefaultTakeTimeout(10l);
        Object actualRetVal = gs.take(template);

        assertEquals(retVal, actualRetVal);
    }

    public void testTakeOperationWithTimeoutParameter() {
        Object template = new Object();
        Object retVal = new Object();

        mockIJSpace.expects(once()).method("take").with(same(template), NULL, eq(11l))
                .will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs));

        Object actualRetVal = gs.take(template, 11l);

        assertEquals(retVal, actualRetVal);
    }

    public void testTakeIfExistsOperation() {
        Object template = new Object();
        Object retVal = new Object();

        mockIJSpace.expects(once()).method("takeIfExists").with(same(template), NULL, eq(JavaSpace.NO_WAIT))
                .will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs));

        Object actualRetVal = gs.takeIfExists(template);

        assertEquals(retVal, actualRetVal);
    }

    public void testTakeIfExistsOperationWithDefaultTimeout() {
        Object template = new Object();
        Object retVal = new Object();

        mockIJSpace.expects(once()).method("takeIfExists").with(same(template), NULL, eq(10l))
                .will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs));

        gs.setDefaultTakeTimeout(10l);
        Object actualRetVal = gs.takeIfExists(template);

        assertEquals(retVal, actualRetVal);
    }

    public void testTakeIfExistsOperationWithTimeoutParameter() {
        Object template = new Object();
        Object retVal = new Object();

        mockIJSpace.expects(once()).method("takeIfExists").with(same(template), NULL, eq(11l))
                .will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs));

        Object actualRetVal = gs.takeIfExists(template, 11l);

        assertEquals(retVal, actualRetVal);
    }

    public void testTakeMultiple() {
        Object template = new Object();
        Object[] retVal = new Object[]{new Object(), new Object()};

        mockIJSpace.expects(once()).method("takeMultiple").with(same(template), NULL, eq(2)).will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs));

        Object actualRetVal = gs.takeMultiple(template, 2);

        assertEquals(retVal, actualRetVal);
    }

    @SuppressWarnings("unchecked")
    public void testWriteOperation() {
        Object entry = new Object();
        Mock mockLeaseContext = mock(LeaseContext.class);
        LeaseContext<Object> leaseContext = (LeaseContext<Object>) mockLeaseContext.proxy();

        mockIJSpace.expects(once()).method("write").with(same(entry), NULL, eq(Lease.FOREVER)).will(returnValue(leaseContext));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs));

        LeaseContext<Object> actualLeaseContext = gs.write(entry);
        assertEquals(leaseContext, actualLeaseContext);
    }

    @SuppressWarnings("unchecked")
    public void testWriteOperationWithDefaultLease() {
        Object entry = new Object();
        Mock mockLeaseContext = mock(LeaseContext.class);
        LeaseContext<Object> leaseContext = (LeaseContext<Object>) mockLeaseContext.proxy();

        mockIJSpace.expects(once()).method("write").with(same(entry), NULL, eq(10l)).will(returnValue(leaseContext));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs));

        gs.setDefaultWriteLease(10l);
        LeaseContext actualLeaseContext = gs.write(entry);

        assertEquals(leaseContext, actualLeaseContext);
    }

    @SuppressWarnings("unchecked")
    public void testWriteOperationWithLeaseParameter() {
        Object entry = new Object();
        Mock mockLeaseContext = mock(LeaseContext.class);
        LeaseContext<Object> leaseContext = (LeaseContext<Object>) mockLeaseContext.proxy();

        mockIJSpace.expects(once()).method("write").with(same(entry), NULL, eq(10l)).will(returnValue(leaseContext));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs));

        LeaseContext<Object> actualLeaseContext = gs.write(entry, 10l);

        assertEquals(leaseContext, actualLeaseContext);
    }

    @SuppressWarnings("unchecked")
    public void testWriteOperationWithLeaseTimeoutModifiersParameters() {
        Object entry = new Object();
        Mock mockLeaseContext = mock(LeaseContext.class);
        LeaseContext<Object> leaseContext = (LeaseContext<Object>) mockLeaseContext.proxy();

        mockIJSpace.expects(once()).method("write")
                .with(new Constraint[]{same(entry), NULL, eq(10l), eq(2l), eq(3)})
                .will(returnValue(leaseContext));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs));

        LeaseContext actualLeaseContext = gs.write(entry, 10l, 2l, 3);

        assertEquals(leaseContext, actualLeaseContext);
    }
}
