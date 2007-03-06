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
import org.springframework.core.Constants;
import org.springframework.transaction.TransactionDefinition;
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
 * and if no transaction manager is provided, will use the space as the context.
 *
 * <p>When usin {@link org.openspaces.core.transaction.manager.LocalJiniTransactionManager} there is no need
 * to pass the transaction manager to this factory, since both by default will use the space as the
 * transactional context. When working with {@link org.openspaces.core.transaction.manager.LookupJiniTransactionManager}
 * (which probably means Mahalo and support for more than one space as transaction resources) the transaction
 * manager should be provided to this class.
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
 * <p>The factory also allows to set the default isolation level for read operations that will
 * be perfomed by {@link org.openspaces.core.GigaSpace} API. The isolation level can be set
 * either using {@link #setDefaultIsolationLevel(int)} or {@link #setDefaultIsolationLevelName(String)}.
 * Note, this setting will apply when not working under Spring declarative transactions or when using
 * Spring declarative transaction with the default isolation level
 * ({@link org.springframework.transaction.TransactionDefinition#ISOLATION_DEFAULT}).
 *
 * @author kimchy
 * @see org.openspaces.core.GigaSpace
 * @see org.openspaces.core.DefaultGigaSpace
 * @see org.openspaces.core.transaction.TransactionProvider
 * @see org.openspaces.core.exception.ExceptionTranslator
 * @see org.openspaces.core.transaction.manager.AbstractJiniTransactionManager
 */
public class GigaSpaceFactoryBean implements InitializingBean, FactoryBean {

    /**
     * Prefix for the isolation constants defined in TransactionDefinition
     */
    public static final String PREFIX_ISOLATION = "ISOLATION_";

    /**
     * Constants instance for TransactionDefinition
     */
    private static final Constants constants = new Constants(TransactionDefinition.class);

    private DefaultGigaSpace gigaSpace;

    private IJSpace space;

    private TransactionProvider txProvider;

    private ExceptionTranslator exTranslator;

    private JiniPlatformTransactionManager transactionManager;

    private boolean clustered = true;

    private long defaultReadTimeout = JavaSpace.NO_WAIT;

    private long defaultTakeTimeout = JavaSpace.NO_WAIT;

    private long defaultWriteLease = Lease.FOREVER;

    private int defaultIsolationLevel = TransactionDefinition.ISOLATION_DEFAULT;

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
     * Set the default isolation level by the name of the corresponding constant in
     * TransactionDefinition, e.g. "ISOLATION_DEFAULT".
     *
     * @param constantName name of the constant
     * @throws IllegalArgumentException if the supplied value is not resolvable
     *                                  to one of the <code>ISOLATION_</code> constants or is <code>null</code>
     * @see #setDefaultIsolationLevel(int)
     * @see org.springframework.transaction.TransactionDefinition#ISOLATION_DEFAULT
     */
    public final void setDefaultIsolationLevelName(String constantName) throws IllegalArgumentException {
        if (constantName == null || !constantName.startsWith(PREFIX_ISOLATION)) {
            throw new IllegalArgumentException("Only isolation constants allowed");
        }
        setDefaultIsolationLevel(constants.asNumber(constantName).intValue());
    }

    /**
     * Set the default isolation level. Must be one of the isolation constants
     * in the TransactionDefinition interface. Default is ISOLATION_DEFAULT.
     *
     * @throws IllegalArgumentException if the supplied value is not
     *                                  one of the <code>ISOLATION_</code> constants
     * @see org.springframework.transaction.TransactionDefinition#ISOLATION_DEFAULT
     */
    public void setDefaultIsolationLevel(int defaultIsolationLevel) {
        if (!constants.getValues(PREFIX_ISOLATION).contains(new Integer(defaultIsolationLevel))) {
            throw new IllegalArgumentException("Only values of isolation constants allowed");
        }
        this.defaultIsolationLevel = defaultIsolationLevel;
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
            // no transaciton context is set (probably since there is no transactionManager), use the space as the transaciton context
            if (transactionalContext == null) {
                transactionalContext = space;
            }
            txProvider = new DefaultTransactionProvider(transactionalContext);
        }
        gigaSpace = new DefaultGigaSpace(space, txProvider, exTranslator, defaultIsolationLevel);
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
