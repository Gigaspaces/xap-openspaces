package org.openspaces.core;

import com.j_spaces.core.IJSpace;
import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;
import org.openspaces.core.exception.DefaultExceptionTranslator;
import org.openspaces.core.exception.ExceptionTranslator;
import org.openspaces.core.transaction.DefaultTransactionProvider;
import org.openspaces.core.transaction.TransactionProvider;
import org.openspaces.core.transaction.manager.JiniPlatformTransactionManager;
import org.openspaces.core.util.SpaceUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>A factory bean creating {@link org.openspaces.core.GigaSpace} implementation.
 * The implementation created is {@link org.openspaces.core.DefaultGigaSpace} which
 * allows for pluggable {@link com.j_spaces.core.IJSpace},
 * {@link org.openspaces.core.transaction.TransactionProvider}, and
 * {@link org.openspaces.core.exception.ExceptionTranslator}.
 *
 * <p>The factory requires an {@link com.j_spaces.core.IJSpace} which can be either
 * directly aquired or build using one of the several space factory beans provided in
 * <code>org.openspaces.core.space</code>.
 *
 * <p>The factory accepts an optional {@link org.openspaces.core.transaction.TransactionProvider}
 * which defaults to {@link org.openspaces.core.transaction.DefaultTransactionProvider}. The transactional
 * context used is based on {@link #setTransactionManager(org.openspaces.core.transaction.manager.JiniPlatformTransactionManager)},
 * so if transactional support is required, both the transaction manager need to be defined AND it needs to
 * be passed to this factory bean. Otherwise, operations will not execute transactionaly.
 *
 * <p>The factory accepts an optional {@link org.openspaces.core.exception.ExceptionTranslator}
 * which defaults to {@link org.openspaces.core.exception.DefaultExceptionTranslator}.
 *
 * <p>A clustered flag (default to <code>true</code>) allows to control if this GigaSpace
 * instance will work against a clustered view of the space or directly against a clustered
 * memeber. This flag has no affect when not working in a clustered mode (partitioned or
 * primary/backup).
 *
 * <p>The factory allows to set the default read/take timeout and write lease when using
 * the same operations without the relevant parameters.
 *
 * @author kimchy
 * @see org.openspaces.core.GigaSpace
 * @see org.openspaces.core.DefaultGigaSpace
 * @see org.openspaces.core.transaction.TransactionProvider
 * @see org.openspaces.core.exception.ExceptionTranslator
 * @see org.openspaces.core.transaction.manager.AbstractJiniTransactionManager
 */
public class GigaSpaceFactoryBean implements InitializingBean, FactoryBean {

    private DefaultGigaSpace gigaSpace;

    private IJSpace space;

    private TransactionProvider txProvider;

    private ExceptionTranslator exTranslator;

    private JiniPlatformTransactionManager transactionManager;

    private boolean clustered = true;

    private long defaultReadTimeout = JavaSpace.NO_WAIT;

    private long defaultTakeTimeout = JavaSpace.NO_WAIT;

    private long defaultWriteLease = Lease.FOREVER;

    /**
     * <p>Sets the space that will be used by the created {@link org.openspaces.core.GigaSpace}.
     * This is a required paramter to the factory.
     *
     * @param space The space used
     */
    public void setSpace(IJSpace space) {
        this.space = space;
    }

    /**
     * <p>Sets the transaction provider that will be used by the created {@link org.openspaces.core.GigaSpace}.
     * This is an optional paramter and defaults to {@link org.openspaces.core.transaction.DefaultTransactionProvider}.
     *
     * @param txProvider The transaction provider to use
     */
    public void setTxProvider(TransactionProvider txProvider) {
        this.txProvider = txProvider;
    }

    /**
     * <p>Sets the exception translator that will be used by the created {@link org.openspaces.core.GigaSpace}.
     * This is an optional parameter and defaults to {@link org.openspaces.core.exception.DefaultExceptionTranslator}.
     *
     * @param exTranslator The exception translator to use
     */
    public void setExTranslator(ExceptionTranslator exTranslator) {
        this.exTranslator = exTranslator;
    }

    /**
     * <p>Sets the cluster flag controlling if this {@link org.openspaces.core.GigaSpace} will work with a clustered
     * view of the space or directly with a cluster member. The flag default to <code>true</code>.
     *
     * @param clustered If the {@link org.openspaces.core.GigaSpace} is going to work with a clsutered view of the
     *                  space or directly with a cluster memeber
     */
    public void setClustered(boolean clustered) {
        this.clustered = clustered;
    }

    /**
     * <p>Sets the default read timeout for {@link org.openspaces.core.GigaSpace#read(Object)} and
     * {@link org.openspaces.core.GigaSpace#readIfExists(Object)} operations. Default to
     * {@link net.jini.space.JavaSpace#NO_WAIT}.
     */
    public void setDefaultReadTimeout(long defaultReadTimeout) {
        this.defaultReadTimeout = defaultReadTimeout;
    }

    /**
     * <p>Sets the default take timeout for {@link org.openspaces.core.GigaSpace#take(Object)} and
     * {@link org.openspaces.core.GigaSpace#takeIfExists(Object)} operations. Default to
     * {@link net.jini.space.JavaSpace#NO_WAIT}.
     */
    public void setDefaultTakeTimeout(long defaultTakeTimeout) {
        this.defaultTakeTimeout = defaultTakeTimeout;
    }

    /**
     * <p>Sets the default write lease for {@link org.openspaces.core.GigaSpace#write(Object)} operaiton.
     * Default to {@link net.jini.core.lease.Lease#FOREVER}.
     */
    public void setDefaultWriteLease(long defaultWriteLease) {
        this.defaultWriteLease = defaultWriteLease;
    }


    /**
     * <p>Set the transaction manager to enable transactional operations. Can be <code>null</code>
     * if transactional support is not required. NOTE: In order to enable transaction support both
     * the transaction manager needs to be defined as well as providing it to this factory bean.
     */
    public void setTransactionManager(JiniPlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * Constructs the {@link org.openspaces.core.GigaSpace} instance using the
     * {@link org.openspaces.core.DefaultGigaSpace} implementation. Uses the clustered flag to
     * get a cluster member directly (if set to <code>false</code>) and applies the differt
     * defaults).
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.space, "space property is required");
        IJSpace space = this.space;
        if (!clustered) {
            space = SpaceUtils.getClusterMemberSpace(space, true);
        }
        if (exTranslator == null) {
            exTranslator = new DefaultExceptionTranslator();
        }
        if (txProvider == null) {
            Object transactionalContext = null;
            if (transactionManager != null) {
                transactionalContext = transactionManager.getTransactionalContext();
            }
            txProvider = new DefaultTransactionProvider(transactionalContext);
        }
        gigaSpace = new DefaultGigaSpace(space, txProvider, exTranslator);
        gigaSpace.setDefaultReadTimeout(defaultReadTimeout);
        gigaSpace.setDefaultTakeTimeout(defaultTakeTimeout);
        gigaSpace.setDefaultWriteLease(defaultWriteLease);
    }

    /**
     * Return {@link org.openspaces.core.GigaSpace} implementation constructed in
     * the {@link #afterPropertiesSet()} phase.
     */
    public Object getObject() throws Exception {
        return this.gigaSpace;
    }

    public Class getObjectType() {
        return (gigaSpace == null ? GigaSpace.class : gigaSpace.getClass());
    }

    /**
     * Returns <code>true</code> as this is a singleton.
     */
    public boolean isSingleton() {
        return true;
    }
}
