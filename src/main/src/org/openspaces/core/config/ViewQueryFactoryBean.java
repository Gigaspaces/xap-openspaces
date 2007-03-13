package org.openspaces.core.config;

import com.j_spaces.core.client.view.View;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * A helper factory beans for {@link com.j_spaces.core.client.view.View} so namespace
 * based configuration will be simpler.
 *
 * @author kimchy
 */
public class ViewQueryFactoryBean implements FactoryBean, InitializingBean {

    private String where;

    private Object template;

    private Class<Object> type;

    private String className;


    private View<Object> view;

    public void setWhere(String where) {
        this.where = where;
    }

    public void setType(Class<Object> clazz) {
        this.type = clazz;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setTemplate(Object template) {
        this.template = template;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(where, "where property is requried");
        if (template == null && type == null && className == null) {
            throw new IllegalArgumentException("either template property or type property or className must be set");
        }
        if (template != null) {
            view = new View<Object>(template, where);
        } else if (type != null) {
            view = new View<Object>(type, where);
        } else {
            view = new View<Object>(className, where);
        }
    }

    public Object getObject() throws Exception {
        return this.view;
    }

    public Class getObjectType() {
        return View.class;
    }

    public boolean isSingleton() {
        return true;
    }
}