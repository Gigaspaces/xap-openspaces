/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.core.config;

import com.j_spaces.core.client.SQLQuery;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * A helper factory beans for {@link com.j_spaces.core.client.SQLQuery} so namespace based
 * configuration will be simpler.
 * 
 * @author kimchy
 */
public class SQLQueryFactoryBean implements FactoryBean, InitializingBean {

    private String where;

    private Object template;

    private Class<Object> type;

    private String className;

    private SQLQuery<Object> sqlQuery;

    public void setWhere(String where) {
        this.where = where;
    }
    
    protected String getWhere() {
        return this.where;
    }
    
    public void setTemplate(Object template) {
        this.template = template;
    }

    protected Object getTemplate() {
        return this.template;
    }

    public void setType(Class<Object> clazz) {
        this.type = clazz;
    }
    
    protected Class<Object> getType() {
        return this.type;
    }

    public void setClassName(String className) {
        this.className = className;
    }
    
    protected String getClassName() {
        return this.className;
    }

    public void afterPropertiesSet() throws Exception {
        validate();
        if (getTemplate() != null) {
            sqlQuery = new SQLQuery<Object>(getTemplate(), getWhere());
        } else if (type != null) {
            sqlQuery = new SQLQuery<Object>(getType(), getWhere());
        } else {
            sqlQuery = new SQLQuery<Object>(getClassName(), getWhere());
        }
    }

    protected void validate() throws IllegalArgumentException {
        Assert.notNull(where, "where property is requried");
        if (getTemplate() == null && getType() == null && getClassName() == null) {
            throw new IllegalArgumentException("either template property or type property or className must be set");
        }
    }

    public Object getObject() throws Exception {
        return this.sqlQuery;
    }

    @SuppressWarnings("unchecked")
    public Class<SQLQuery> getObjectType() {
        return SQLQuery.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
