package org.openspaces.core.config;

import com.j_spaces.core.client.view.View;

/**
 * A helper factory beans for {@link com.j_spaces.core.client.view.View} so namespace based
 * configuration will be simpler.
 * 
 * @author kimchy
 */
public class ViewQueryFactoryBean extends SQLQueryFactoryBean {

    private View<Object> view;

    public void afterPropertiesSet() throws Exception {
        validate();
        if (getTemplate() != null) {
            view = new View<Object>(getTemplate(), getWhere());
        } else if (getType() != null) {
            view = new View<Object>(getType(), getWhere());
        } else {
            view = new View<Object>(getClassName(), getWhere());
        }
    }

    public Object getObject() throws Exception {
        return this.view;
    }

    @SuppressWarnings("unchecked")
    public Class getObjectType() {
        return View.class;
    }

    public boolean isSingleton() {
        return true;
    }
}