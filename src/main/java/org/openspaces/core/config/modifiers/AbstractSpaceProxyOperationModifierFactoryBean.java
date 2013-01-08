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
package org.openspaces.core.config.modifiers;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.Constants;

import com.gigaspaces.client.ChangeModifiers;
import com.gigaspaces.client.ClearModifiers;
import com.gigaspaces.client.ReadModifiers;
import com.gigaspaces.client.SpaceProxyOperationModifiers;
import com.gigaspaces.client.TakeModifiers;
import com.gigaspaces.client.WriteModifiers;

/**
 * Base class for different modifier types beans.
 * @author Dan Kilman
 * @since 9.5
 * @param <T> a subclass of {@link SpaceProxyOperationModifiers}
 */
abstract public class AbstractSpaceProxyOperationModifierFactoryBean<T extends SpaceProxyOperationModifiers> 
    implements FactoryBean<T> {

    private final Class<T> objectType;
    
    private T modifier;

    public AbstractSpaceProxyOperationModifierFactoryBean(Class<T> objectType) {
        this.objectType = objectType;
    }

    /**
     * @param modifierName The modifier constant name as defined in the relevant Modifiers class.
     * @see {@link WriteModifiers}, {@link ReadModifiers}, {@link TakeModifiers}, 
     *      {@link CoutModifiers}, {@link ClearModifiers}, {@link ChangeModifiers}
     */
    @SuppressWarnings("unchecked")
    public void setModifierName(String modifierName) {

        Constants constants = getConstants();

        if (!constants.getNames("").contains(modifierName)) {
            throw new IllegalArgumentException("Unknown modifier: " + modifierName + " for type: " + getObjectType());
        }
        
        modifier = (T) constants.asObject(modifierName);
    }

    @Override
    public Class<T> getObjectType() {
        return objectType;
    }
    
    @Override
    public T getObject() throws Exception {
        return modifier;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
    
    abstract protected Constants getConstants();
    
}
