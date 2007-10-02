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

package org.openspaces.core.transaction.manager;

import net.jini.core.transaction.server.TransactionManager;
import org.openspaces.core.jini.JiniServiceFactoryBean;
import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;

import java.util.Arrays;

/**
 * Springs transaction manager ({@link org.springframework.transaction.PlatformTransactionManager}
 * using Jini in order to lookup the transaction manager based on a name (can have <code>null</code>
 * value).
 *
 * <p>Uses {@link JiniServiceFactoryBean} in order to perform the lookup based on the specified
 * {@link #setTransactionManagerName(String)} and {@link #setLookupTimeout(Long)}. This usually
 * works with Jini Mahalo transaction manager.
 *
 * @author kimchy
 */
public class LookupJiniTransactionManager extends AbstractJiniTransactionManager {

    private static final long serialVersionUID = -917940171952237730L;

    private String transactionManagerName;

    private Long lookupTimeout;

    private String[] groups;

    private String[] locators;

    /**
     * Sets the transaction manager name to perform the lookup by.
     */
    public void setTransactionManagerName(String transactionManagerName) {
        this.transactionManagerName = transactionManagerName;
    }

    /**
     * Sets the lookupTimeout for the transaction manager lookup operation.
     */
    public void setLookupTimeout(Long lookupTimeout) {
        this.lookupTimeout = lookupTimeout;
    }

    /**
     * Sets the groups that will be used to look up the Jini transaction manager. Default to ALL groups.
     */
    public void setGroups(String[] groups) {
        this.groups = groups;
    }

    /**
     * Sets specific locators to find the jini transaction manger.
     */
    public void setLocators(String[] locators) {
        this.locators = locators;
    }

    /**
     * Returns a Jini {@link TransactionManager} that is lookup up using
     * {@link JiniServiceFactoryBean}. The lookup can use a specified
     * {@link #setTransactionManagerName(String)} and a {@link #setLookupTimeout(Long)}.
     */
    protected TransactionManager doCreateTransactionManager() throws Exception {
        JiniServiceFactoryBean serviceFactory = new JiniServiceFactoryBean();
        serviceFactory.setServiceClass(TransactionManager.class);
        serviceFactory.setServiceName(transactionManagerName);
        if (lookupTimeout != null) {
            serviceFactory.setTimeout(lookupTimeout);
        }
        if (groups != null) {
            serviceFactory.setGroups(groups);
        }
        if (locators != null) {
            serviceFactory.setLocators(locators);
        }
        serviceFactory.afterPropertiesSet();
        TransactionManager transactionManager = (TransactionManager) serviceFactory.getObject();
        if (transactionManager == null) {
            String groups = "ALL";
            if (this.groups != null) {
                groups = Arrays.asList(groups).toString();
            }
            throw new TransactionSystemException("Failed to find Jini transaction manager using groups [" + groups
                    + "], timeout [" + lookupTimeout + "] and name [" + transactionManagerName + "]");
        }
        return transactionManager;
    }

    protected void applyIsolationLevel(JiniTransactionObject txObject, int isolationLevel)
            throws InvalidIsolationLevelException {
        if (isolationLevel == TransactionDefinition.ISOLATION_SERIALIZABLE) {
            throw new InvalidIsolationLevelException(
                    "Local TransactionManager does not support serializable isolation level");
        }
    }
}
