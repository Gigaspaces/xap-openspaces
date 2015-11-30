package org.openspaces.leader_selector.zk.config;

import com.gigaspaces.cluster.activeelection.LeaderSelectorConfig;

import java.util.concurrent.TimeUnit;

/**
 * @author kobi on 11/22/15.
 * @since 10.2
 */
public class ZooKeeperLeaderSelectorConfigurer {
    private final LeaderSelectorConfig leaderSelectorConfig;

    public ZooKeeperLeaderSelectorConfigurer() {
        leaderSelectorConfig = new LeaderSelectorConfig();
        leaderSelectorConfig.getProperties().setProperty("leaderSelectorHandler", "org.openspaces.zk.leader_selector.ZooKeeperBasedLeaderSelectorHandler");
        leaderSelectorConfig.getProperties().setProperty("sessionTimeout", "60000");
        leaderSelectorConfig.getProperties().setProperty("connectionTimeout", "15000");
    }

    /**
     *
     * @param sessionTimeout - curator client session timeout
     * @param timeUnit
     * @return itself
     */
    public ZooKeeperLeaderSelectorConfigurer sessionTimeout(long sessionTimeout, TimeUnit timeUnit) {
        leaderSelectorConfig.getProperties().setProperty("sessionTimeout", String.valueOf(timeUnit.toMillis(sessionTimeout)));
        return this;
    }

    /**
     *
     * @param connectionTimeout - curator client connection timeout
     * @param timeUnit
     * @return itself
     */
    public ZooKeeperLeaderSelectorConfigurer connectionTimeout(long connectionTimeout, TimeUnit timeUnit) {
        leaderSelectorConfig.getProperties().setProperty("connectionTimeout", String.valueOf(timeUnit.toMillis(connectionTimeout)));
        return this;
    }

    public LeaderSelectorConfig create() {
        return leaderSelectorConfig;
    }

}
