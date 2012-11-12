/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.openspaces.archive;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

import org.openspaces.core.GigaSpace;
import org.openspaces.events.SpaceDataEventListener;
import org.openspaces.events.polling.SimplePollingEventListenerContainer;
import org.openspaces.events.polling.receive.MultiTakeReceiveOperationHandler;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.TransactionStatus;
import org.springframework.util.ReflectionUtils;

/**
 * Takes objects specified in the template into the archive handler defined by {@link #setArchiveHandler(ArchiveOperationHandler)}
 * This container can be used to take (remove) objects from the Space and persist them into an external service.
 * 
 * @author Itai Frenkel
 * @since 9.1.1
 */
public class ArchivePollingContainer 
    extends SimplePollingEventListenerContainer 
    implements SpaceDataEventListener<Object> {

    private ArchiveOperationHandler archiveHandler;
    private Object archiveHandlerProvider;
    private boolean atomicArchiveOfMultipleObjects;
    
    public ArchivePollingContainer() {
        super.setEventListener(this);
    }
    
    @Required
    public void setArchiveHandler(ArchiveOperationHandler archiveHandler) {
       this.archiveHandler = archiveHandler;
    }
    
    
    @Override
    protected void validateConfiguration() {
        super.validateConfiguration();
        if (archiveHandler == null) {
            throw new IllegalStateException("Archive handler cannot be null");
        }
    }
    
    
    @Override
    public void afterPropertiesSet() {
        
        initArchiveHandler();
        
        atomicArchiveOfMultipleObjects = archiveHandler.supportsAtomicBatchArchiving();
        if (atomicArchiveOfMultipleObjects) {
            MultiTakeReceiveOperationHandler receiveHandler = new MultiTakeReceiveOperationHandler();
            //receiveHandler.setMaxEntries(/*Configuration Driven*/);
            setReceiveOperationHandler(receiveHandler);
            super.setPassArrayAsIs(true);
        }
        super.afterPropertiesSet();
    }

    /**
     * Validate there is a valid archiveHandler or extract one from a valid archiveHandlerProvider.
     */
    private void initArchiveHandler() {
        if (archiveHandler == null && archiveHandlerProvider != null) {
            
            if (archiveHandlerProvider instanceof ArchiveOperationHandlerProvider) {
                // implements an interface to provide the archiveHandler
                setArchiveHandler(((ArchiveOperationHandlerProvider)archiveHandlerProvider).getArchiveOperationHandler());
            }
            else {
                // check if there is an annotated method for it
                final AtomicReference<Method> ref = new AtomicReference<Method>();
                ReflectionUtils.doWithMethods(AopUtils.getTargetClass(archiveHandlerProvider), new ReflectionUtils.MethodCallback() {
                    public void doWith(final Method method) throws IllegalArgumentException, IllegalAccessException {
                        if (method.isAnnotationPresent(ArchiveHandler.class)) {
                            ref.set(method);
                        }
                    }
                });
                if (ref.get() == null) {
                    throw new IllegalArgumentException(
                            "archiveHandlerProvider must either implement " + ArchiveOperationHandlerProvider.class.getName() +" "+
                            "or provide a method annotated with @" + ArchiveHandler.class.getSimpleName());
                }
                ref.get().setAccessible(true);
                try {
                    final Object archiveHandlerInstance = ref.get().invoke(archiveHandlerProvider);
                    if (archiveHandlerInstance == null) {
                        throw new IllegalArgumentException("Return value cannot be null");
                    }
                    if (!(archiveHandlerInstance instanceof ArchiveOperationHandler)) {
                        throw new IllegalArgumentException("Return value must implement " + ArchiveOperationHandler.class.getName());
                    }
                    setArchiveHandler((ArchiveOperationHandler)archiveHandlerInstance);
                } catch (final Exception e) {
                    throw new IllegalArgumentException("Failed to get archiveHandler from method [" + ref.get().getName() + "]", e);
                }
            }
        }
        
        if (archiveHandler == null) {
            throw new IllegalStateException("archiveHandler cannot be null");
        }
    }

    @Override
    public void onEvent(Object data, GigaSpace gigaSpace, TransactionStatus txStatus, Object source) {
        if (atomicArchiveOfMultipleObjects) {
            archiveHandler.archive((Object[])data);
        }
        else {
            archiveHandler.archive(data);
        }
    }

    /**
     * Expects an object that either implements {@link ArchiveOperationHandlerProvider}
     * or has a method annotated with {@link ArchiveHandler} which returns an instance of {@link ArchiveOperationHandler}
     */
    public void setArchiveHandlerProvider(Object archiveHandlerProvider) {
        this.archiveHandlerProvider = archiveHandlerProvider;
    }
    
}