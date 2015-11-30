package org.openspaces.core.space;

import com.gigaspaces.attribute_store.AttributeStore;
import com.gigaspaces.cluster.activeelection.LeaderSelectorConfig;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author kobi on 11/22/15.
 * @since 10.2
 */
public class LeaderSelectorFactoryBean implements InitializingBean {

    protected LeaderSelectorConfig config;


    public LeaderSelectorConfig getConfig() {
        return config;
    }

    public void setConfig(LeaderSelectorConfig config) {
        this.config = config;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

}
