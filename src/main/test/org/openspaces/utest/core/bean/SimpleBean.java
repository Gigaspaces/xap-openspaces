package org.openspaces.utest.core.bean;

import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.core.bean.Bean;

public class SimpleBean implements Bean {

    private Map<String, String> properties;
    private Admin admin;

    public void afterPropertiesSet() throws Exception {

    }

    public void destroy() throws Exception {

    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
