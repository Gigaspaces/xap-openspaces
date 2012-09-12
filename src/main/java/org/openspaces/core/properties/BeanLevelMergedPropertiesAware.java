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

package org.openspaces.core.properties;

import java.util.Properties;

/**
 * A callback that sets the merged properties using
 * {@link BeanLevelProperties#getMergedBeanProperties(String) mergedProperties} and the bean name
 * that the bean implementing this interface is associated with.
 * 
 * @author kimchy
 */
public interface BeanLevelMergedPropertiesAware {

    /**
     * Sets the merged properties using
     * {@link BeanLevelProperties#getMergedBeanProperties(String) mergedProperties} and the bean
     * name that the bean implementing this interface is associated with.
     */
    void setMergedBeanLevelProperties(Properties beanLevelProperties);
}
