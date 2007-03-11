package org.openspaces.core.config;

import com.j_spaces.core.client.SQLQuery;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * @author kimchy
 */
public class SQLQueryFactoryBean implements FactoryBean, InitializingBean {

    private String where;

    private Object template;

    private String type;


    private SQLQuery<Object> sqlQuery;

    public void setWhere(String where) {
        this.where = where;
    }

    public void setTemplate(Object template) {
        this.template = template;
    }

    public void setType(String clazz) {
        this.type = clazz;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(where, "where property is requried");
        if (template == null && type == null) {
            throw new IllegalArgumentException("either template property or type property must be set");
        }
        if (template != null) {
            sqlQuery = new SQLQuery<Object>(template, where);
        } else {
            sqlQuery = new SQLQuery<Object>(type, where);
        }
    }

    public Object getObject() throws Exception {
        return this.sqlQuery;
    }

    public Class getObjectType() {
        return SQLQuery.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
