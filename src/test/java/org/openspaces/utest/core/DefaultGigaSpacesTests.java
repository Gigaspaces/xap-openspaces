/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.utest.core;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Constraint;
import org.openspaces.core.DefaultGigaSpace;
import org.openspaces.core.exception.ExceptionTranslator;
import org.openspaces.core.transaction.TransactionProvider;
import org.springframework.transaction.TransactionDefinition;

import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.LeaseContext;
import com.j_spaces.core.client.ReadModifiers;

/**
 * A set of mock tests verifies that the correct {@link com.j_spaces.core.IJSpace} API is called as
 * a result of {@link org.openspaces.core.DefaultGigaSpace} execution.
 * 
 * @author kimchy
 */
public class DefaultGigaSpacesTests extends MockObjectTestCase {

    private DefaultGigaSpace gs;

    private Mock mockIJSpace;
    private Mock mockTxProvider;
    private Mock mockExTranslator;

    protected void setUp() throws Exception {
        mockIJSpace = mock(ISpaceProxy.class);
        mockTxProvider = mock(TransactionProvider.class);
        mockExTranslator = mock(ExceptionTranslator.class);

        mockIJSpace.expects(once()).method("getReadModifiers").will(returnValue(0));
        gs = new DefaultGigaSpace((IJSpace) mockIJSpace.proxy(), (TransactionProvider) mockTxProvider.proxy(),
                (ExceptionTranslator) mockExTranslator.proxy(), TransactionDefinition.ISOLATION_DEFAULT);
    }

    public void testReadOperation() {
        Object template = new Object();
        Object retVal = new Object();

        mockIJSpace.expects(once()).method("read").with(same(template), NULL, eq(0l), eq(ReadModifiers.READ_COMMITTED)).will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs), eq(gs.getSpace()));
        mockTxProvider.expects(once()).method("getCurrentTransactionIsolationLevel").with(eq(gs)).will(
                returnValue(TransactionDefinition.ISOLATION_READ_COMMITTED));

        Object actualRetVal = gs.read(template);

        assertEquals(retVal, actualRetVal);
    }

    public void testReadOperationWithDefaultTimeout() {
        Object template = new Object();
        Object retVal = new Object();

        mockIJSpace.expects(once())
            .method("read")
            .with(same(template), NULL, eq(10l), eq(ReadModifiers.READ_COMMITTED))
            .will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs), eq(gs.getSpace()));
        mockTxProvider.expects(once()).method("getCurrentTransactionIsolationLevel").with(eq(gs)).will(
                returnValue(TransactionDefinition.ISOLATION_READ_COMMITTED));

        gs.setDefaultReadTimeout(10l);
        Object actualRetVal = gs.read(template);

        assertEquals(retVal, actualRetVal);
    }

    public void testReadOperationWithTimeoutParameter() {
        Object template = new Object();
        Object retVal = new Object();

        mockIJSpace.expects(once())
            .method("read")
            .with(same(template), NULL, eq(11l), eq(ReadModifiers.READ_COMMITTED))
            .will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs), eq(gs.getSpace()));
        mockTxProvider.expects(once()).method("getCurrentTransactionIsolationLevel").with(eq(gs)).will(
                returnValue(TransactionDefinition.ISOLATION_READ_COMMITTED));

        Object actualRetVal = gs.read(template, 11l);

        assertEquals(retVal, actualRetVal);
    }

    public void testReadIfExistsOperation() {
        Object template = new Object();
        Object retVal = new Object();

        mockIJSpace.expects(once()).method("readIfExists").with(same(template), NULL, eq(0l),
                eq(ReadModifiers.READ_COMMITTED)).will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs), eq(gs.getSpace()));
        mockTxProvider.expects(once()).method("getCurrentTransactionIsolationLevel").with(eq(gs)).will(
                returnValue(TransactionDefinition.ISOLATION_READ_COMMITTED));

        Object actualRetVal = gs.readIfExists(template);

        assertEquals(retVal, actualRetVal);
    }

    public void testReadIfExistsOperationWithDefaultTimeout() {
        Object template = new Object();
        Object retVal = new Object();

        mockIJSpace.expects(once()).method("readIfExists").with(same(template), NULL, eq(10l),
                eq(ReadModifiers.READ_COMMITTED)).will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs), eq(gs.getSpace()));
        mockTxProvider.expects(once()).method("getCurrentTransactionIsolationLevel").with(eq(gs)).will(
                returnValue(TransactionDefinition.ISOLATION_READ_COMMITTED));

        gs.setDefaultReadTimeout(10l);
        Object actualRetVal = gs.readIfExists(template);

        assertEquals(retVal, actualRetVal);
    }

    public void testReadIfExistsOperationWithTimeoutParameter() {
        Object template = new Object();
        Object retVal = new Object();

        mockIJSpace.expects(once()).method("readIfExists").with(same(template), NULL, eq(11l),
                eq(ReadModifiers.READ_COMMITTED)).will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs), eq(gs.getSpace()));
        mockTxProvider.expects(once()).method("getCurrentTransactionIsolationLevel").with(eq(gs)).will(
                returnValue(TransactionDefinition.ISOLATION_READ_COMMITTED));

        Object actualRetVal = gs.readIfExists(template, 11l);

        assertEquals(retVal, actualRetVal);
    }

    public void testReadMultipleOperation() {
        Object template = new Object();
        Object[] retVal = new Object[] { new Object(), new Object() };

        mockIJSpace.expects(once()).method("readMultiple").with(same(template), NULL, eq(2),
                eq(ReadModifiers.READ_COMMITTED)).will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs), eq(gs.getSpace()));
        mockTxProvider.expects(once()).method("getCurrentTransactionIsolationLevel").with(eq(gs)).will(
                returnValue(TransactionDefinition.ISOLATION_READ_COMMITTED));

        Object actualRetVal = gs.readMultiple(template, 2);

        assertEquals(retVal, actualRetVal);
    }
    
    public void testReadMultipleNoLimitOperation() {
        Object template = new Object();
        Object[] retVal = new Object[] { new Object(), new Object(), new Object(), new Object()};

        mockIJSpace.expects(once()).method("readMultiple").with(same(template), NULL, eq(Integer.MAX_VALUE),
                eq(ReadModifiers.READ_COMMITTED)).will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs), eq(gs.getSpace()));
        mockTxProvider.expects(once()).method("getCurrentTransactionIsolationLevel").with(eq(gs)).will(
                returnValue(TransactionDefinition.ISOLATION_READ_COMMITTED));

        Object actualRetVal = gs.readMultiple(template);

        assertEquals(retVal, actualRetVal);
    }

    public void testTakeOperation() {
        Object template = new Object();
        Object retVal = new Object();

        Constraint[] constraints = new Constraint[] {
                same(template), 
                NULL, 
                eq(0l), 
                eq(ReadModifiers.READ_COMMITTED), 
                same(false)
                };
        mockIJSpace.expects(once()).method("take").with(constraints).will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs), eq(gs.getSpace()));
        mockTxProvider.expects(once()).method("getCurrentTransactionIsolationLevel").with(eq(gs)).will(
                returnValue(TransactionDefinition.ISOLATION_READ_COMMITTED));

        Object actualRetVal = gs.take(template);

        assertEquals(retVal, actualRetVal);
    }

    public void testTakeOperationWithDefaultTimeout() {
        Object template = new Object();
        Object retVal = new Object();

        Constraint[] constraints = new Constraint[] {
                same(template), 
                NULL, 
                eq(10l), 
                eq(ReadModifiers.READ_COMMITTED),
                same(false)};
        mockIJSpace.expects(once()).method("take").with(constraints).will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs), eq(gs.getSpace()));
        mockTxProvider.expects(once()).method("getCurrentTransactionIsolationLevel").with(eq(gs)).will(
                returnValue(TransactionDefinition.ISOLATION_READ_COMMITTED));

        gs.setDefaultTakeTimeout(10l);
        Object actualRetVal = gs.take(template);

        assertEquals(retVal, actualRetVal);
    }

    public void testTakeOperationWithTimeoutParameter() {
        Object template = new Object();
        Object retVal = new Object();

        Constraint[] constraints = new Constraint[] {
                same(template), 
                NULL, 
                eq(11l), 
                eq(ReadModifiers.READ_COMMITTED),
                same(false)
        };
        mockIJSpace.expects(once()).method("take").with(constraints).will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs), eq(gs.getSpace()));
        mockTxProvider.expects(once()).method("getCurrentTransactionIsolationLevel").with(eq(gs)).will(
                returnValue(TransactionDefinition.ISOLATION_READ_COMMITTED));

        Object actualRetVal = gs.take(template, 11l);

        assertEquals(retVal, actualRetVal);
    }

    public void testTakeIfExistsOperation() {
        Object template = new Object();
        Object retVal = new Object();

        Constraint[] constraints = new Constraint[] {
                same(template),
                NULL, 
                eq(0l),
                eq(ReadModifiers.READ_COMMITTED), 
                eq(Boolean.TRUE)
        };
        mockIJSpace.expects(once()).method("take").with(constraints).will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs), eq(gs.getSpace()));
        mockTxProvider.expects(once()).method("getCurrentTransactionIsolationLevel").with(eq(gs)).will(
                returnValue(TransactionDefinition.ISOLATION_READ_COMMITTED));

        Object actualRetVal = gs.takeIfExists(template);

        assertEquals(retVal, actualRetVal);
    }

    public void testTakeIfExistsOperationWithDefaultTimeout() {
        Object template = new Object();
        Object retVal = new Object();

        Constraint[] constraints = new Constraint[] {
                same(template), 
                NULL, 
                eq(10l),
                eq(ReadModifiers.READ_COMMITTED), 
                eq(Boolean.TRUE)
        };
        mockIJSpace.expects(once()).method("take").with(constraints).will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs), eq(gs.getSpace()));
        mockTxProvider.expects(once()).method("getCurrentTransactionIsolationLevel").with(eq(gs)).will(
                returnValue(TransactionDefinition.ISOLATION_READ_COMMITTED));

        gs.setDefaultTakeTimeout(10l);
        Object actualRetVal = gs.takeIfExists(template);

        assertEquals(retVal, actualRetVal);
    }

    public void testTakeIfExistsOperationWithTimeoutParameter() {
        Object template = new Object();
        Object retVal = new Object();

        Constraint[] constraints = new Constraint[] {
                same(template), 
                NULL, 
                eq(11l),
                eq(ReadModifiers.READ_COMMITTED), 
                eq(Boolean.TRUE)
        };

        mockIJSpace.expects(once()).method("take").with(constraints).will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs), eq(gs.getSpace()));
        mockTxProvider.expects(once()).method("getCurrentTransactionIsolationLevel").with(eq(gs)).will(
                returnValue(TransactionDefinition.ISOLATION_READ_COMMITTED));

        Object actualRetVal = gs.takeIfExists(template, 11l);

        assertEquals(retVal, actualRetVal);
    }
    
    public void testTakeMultipleNoLimit() {
        Object template = new Object();
        Object[] retVal = new Object[] { new Object(), new Object(), new Object(), new Object()};

        mockIJSpace.expects(once()).method("takeMultiple").with(same(template), NULL, eq(Integer.MAX_VALUE)).will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs), eq(gs.getSpace()));

        Object actualRetVal = gs.takeMultiple(template);

        assertEquals(retVal, actualRetVal);
    }

    public void testTakeMultiple() {
        Object template = new Object();
        Object[] retVal = new Object[] { new Object(), new Object() };

        mockIJSpace.expects(once()).method("takeMultiple").with(same(template), NULL, eq(2)).will(returnValue(retVal));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs), eq(gs.getSpace()));

        Object actualRetVal = gs.takeMultiple(template, 2);

        assertEquals(retVal, actualRetVal);
    }

    @SuppressWarnings("unchecked")
    public void testWriteOperation() {
        Object entry = new Object();
        Mock mockLeaseContext = mock(LeaseContext.class);
        LeaseContext<Object> leaseContext = (LeaseContext<Object>) mockLeaseContext.proxy();

        mockIJSpace.expects(once()).method("write").with(same(entry), NULL, eq(Long.MAX_VALUE)).will(
                returnValue(leaseContext));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs), eq(gs.getSpace()));

        LeaseContext<Object> actualLeaseContext = gs.write(entry);
        assertEquals(leaseContext, actualLeaseContext);
    }

    @SuppressWarnings("unchecked")
    public void testWriteOperationWithDefaultLease() {
        Object entry = new Object();
        Mock mockLeaseContext = mock(LeaseContext.class);
        LeaseContext<Object> leaseContext = (LeaseContext<Object>) mockLeaseContext.proxy();

        mockIJSpace.expects(once()).method("write").with(same(entry), NULL, eq(10l)).will(returnValue(leaseContext));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs), eq(gs.getSpace()));

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
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs), eq(gs.getSpace()));

        LeaseContext<Object> actualLeaseContext = gs.write(entry, 10l);

        assertEquals(leaseContext, actualLeaseContext);
    }

    @SuppressWarnings("unchecked")
    public void testWriteOperationWithLeaseTimeoutModifiersParameters() {
        Object entry = new Object();
        Mock mockLeaseContext = mock(LeaseContext.class);
        LeaseContext<Object> leaseContext = (LeaseContext<Object>) mockLeaseContext.proxy();

        mockIJSpace.expects(once())
            .method("write")
            .with(new Constraint[] { same(entry), NULL, eq(10l), eq(2l), eq(3) })
            .will(returnValue(leaseContext));
        mockTxProvider.expects(once()).method("getCurrentTransaction").with(eq(gs), eq(gs.getSpace()));

        LeaseContext actualLeaseContext = gs.write(entry, 10l, 2l, 3);

        assertEquals(leaseContext, actualLeaseContext);
    }
}
