package org.openspaces.leader_selector.zookeeper.config;

import com.gigaspaces.cluster.activeelection.LeaderSelectorConfig;
import com.j_spaces.core.Constants;

import java.util.concurrent.TimeUnit;

/**
 * @author kobi on 11/22/15.
 * @since 10.2
 */
public class ZooKeeperLeaderSelectorConfigurer {
    private final LeaderSelectorConfig leaderSelectorConfig;

    public ZooKeeperLeaderSelectorConfigurer() {
        leaderSelectorConfig = new LeaderSelectorConfig();
        leaderSelectorConfig.getProperties().setProperty("leaderSelectorHandler", "org.openspaces.zookeeper.leader_selector.ZooKeeperBasedLeaderSelectorHandler");
        leaderSelectorConfig.getProperties().setProperty("sessionTimeout", Constants.LeaderSelector.ZOOKEEPER.CURATOR_SESSION_TIMEOUT_DEFAULT);
        leaderSelectorConfig.getProperties().setProperty("connectionTimeout", Constants.LeaderSelector.ZOOKEEPER.CURATOR_CONNECTION_TIMEOUT_DEFAULT);
        leaderSelectorConfig.getProperties().setProperty("retries", Constants.LeaderSelector.ZOOKEEPER.CURATOR_RETRIES_DEFAULT);
        leaderSelectorConfig.getProperties().setProperty("sleepMsBetweenRetries", Constants.LeaderSelector.ZOOKEEPER.CURATOR_SLEEP_MS_BETWEEN_RETRIES_DEFAULT);
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

    /**
     *
     * @param retries - curator client retries
     * @return itself
     */
    public ZooKeeperLeaderSelectorConfigurer retries(long retries) {
        leaderSelectorConfig.getProperties().setProperty("retries", String.valueOf(retries));
        return this;
    }

    /**
     *
     * @param sleepMsBetweenRetries - curator client sleep between retries
     * @param timeUnit
     * @return itself
     */
    public ZooKeeperLeaderSelectorConfigurer sleepMsBetweenRetries(long sleepMsBetweenRetries, TimeUnit timeUnit) {
        leaderSelectorConfig.getProperties().setProperty("sleepMsBetweenRetries", String.valueOf(timeUnit.toMillis(sleepMsBetweenRetries)));
        return this;
    }

    public LeaderSelectorConfig create() {
        return leaderSelectorConfig;
    }

}
