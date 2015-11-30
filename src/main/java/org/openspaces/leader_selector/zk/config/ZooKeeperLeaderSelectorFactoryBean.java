package org.openspaces.leader_selector.zk.config;

import com.gigaspaces.cluster.activeelection.LeaderSelectorConfig;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author kobi on 11/22/15.
 * @since 11.0
 */
public class ZooKeeperLeaderSelectorFactoryBean implements InitializingBean, FactoryBean {

    protected LeaderSelectorConfig config;
    private long sessionTimeout;
    private long connectionTimeout;

    public void setSessionTimeout(long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        config = new LeaderSelectorConfig();
        config.getProperties().setProperty("leaderSelectorHandler", "org.openspaces.zk.leader_selector.ZooKeeperBasedLeaderSelectorHandler");
        config.getProperties().setProperty("sessionTimeout", String.valueOf(sessionTimeout));
        config.getProperties().setProperty("connectionTimeout", String.valueOf(connectionTimeout));
    }

    @Override
    public Object getObject() throws Exception {
        return config;
    }

    @Override
    public Class<?> getObjectType() {
        return (config == null ? LeaderSelectorConfig.class : config.getClass());
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
