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

package org.openspaces.itest.core.space.filter.adapter;

import java.util.ArrayList;
import java.util.List;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceLateContext;
import org.openspaces.core.space.filter.AfterExecute;
import org.openspaces.core.space.filter.AfterNotifyTrigger;
import org.openspaces.core.space.filter.AfterRead;
import org.openspaces.core.space.filter.AfterReadMultiple;
import org.openspaces.core.space.filter.AfterTake;
import org.openspaces.core.space.filter.AfterTakeMultiple;
import org.openspaces.core.space.filter.AfterUpdate;
import org.openspaces.core.space.filter.AfterWrite;
import org.openspaces.core.space.filter.BeforeExecute;
import org.openspaces.core.space.filter.BeforeNotify;
import org.openspaces.core.space.filter.BeforeNotifyTrigger;
import org.openspaces.core.space.filter.BeforeRead;
import org.openspaces.core.space.filter.BeforeReadMultiple;
import org.openspaces.core.space.filter.BeforeTake;
import org.openspaces.core.space.filter.BeforeTakeMultiple;
import org.openspaces.core.space.filter.BeforeUpdate;
import org.openspaces.core.space.filter.BeforeWrite;
import org.openspaces.core.space.filter.OnFilterClose;
import org.openspaces.core.space.filter.OnFilterInit;

import com.j_spaces.core.filters.FilterOperationCodes;
import com.j_spaces.core.filters.entry.ISpaceFilterEntry;

/**
 * @author kimchy
 */
public class CustomFilter {

    @GigaSpaceLateContext(name = "gigaSpace")
    GigaSpace gigaSpace;

    private List<Object[]> lastExecutions = new ArrayList<Object[]>();

    private boolean onInitCalled;
    private boolean onCloseCalled;

    private boolean beforeAuthentication;

    public boolean isBeforeAuthentication(){
        return beforeAuthentication;
    }

    public boolean isOnInitCalled() {
        return onInitCalled;
    }

    public boolean isOnCloseCalled() {
        return onCloseCalled;
    }

    public List<Object[]> getLastExecutions() {
        return this.lastExecutions;
    }

    @OnFilterInit
    void onInit() {
        onInitCalled = true;
    }

    @OnFilterClose
    void onClose() {
        onCloseCalled = true;
    }
    
    @BeforeWrite
    public void beforeWrite(ISpaceFilterEntry entry, int operationCode) {
        lastExecutions.add(new Object[]{entry, operationCode});
    }

    @AfterWrite
    public void afterWrite(ISpaceFilterEntry entry, int operationCode) {
        lastExecutions.add(new Object[]{entry, operationCode});
    }

    @BeforeUpdate
    public void beforeUpdate(Message entry, int operationCode) {
        lastExecutions.add(new Object[]{entry, operationCode});
    }

    @AfterUpdate
    public void afterUpdate(Message beforeUpdate, Message afterUpdate, int operationCode) {
        lastExecutions.add(new Object[]{beforeUpdate, afterUpdate, operationCode});
    }

    @BeforeRead
    public void beforeRead(ISpaceFilterEntry entry, int operationCode) {
        lastExecutions.add(new Object[]{entry, operationCode});
    }

    @AfterRead
    public void afterRead(ISpaceFilterEntry entry, int operationCode) {
        lastExecutions.add(new Object[]{entry, operationCode});
    }

    @BeforeReadMultiple
    public void beforeReadMultiple(Message entry, int operationCode) {
        lastExecutions.add(new Object[]{entry, operationCode});
    }

    @AfterReadMultiple
    public void afterReadMultiple(Message entry, int operationCode) {
        lastExecutions.add(new Object[]{entry, operationCode});
    }

    @BeforeTake
    public void beforeTake(Message entry, int operationCode) {
        lastExecutions.add(new Object[]{entry, operationCode});
    }

    @AfterTake
    public void afterTake(Message entry, int operationCode) {
        lastExecutions.add(new Object[]{entry, operationCode});
    }

    @BeforeTakeMultiple
    public void beforeTakeMultiple(Message entry, int operationCode) {
        lastExecutions.add(new Object[]{entry, operationCode});
    }

    @AfterTakeMultiple
    public void afterTakeMultiple(Message entry, int operationCode) {
        lastExecutions.add(new Object[]{entry, operationCode});
    }

    @AfterExecute
    public void afterExecute(ISpaceFilterEntry entry, int operationCode) {
        lastExecutions.add(new Object[]{entry, operationCode});
    }

    @BeforeExecute
    public void beforeExecute(ISpaceFilterEntry entry, int operationCode) {
        lastExecutions.add(new Object[]{entry, operationCode});
    }

    @BeforeNotifyTrigger
    public void beforeNotifyTrigger(Message entry, Message entry2, int opCode) {
        lastExecutions.add(new Object[]{entry, entry2, FilterOperationCodes.operationCodeToString(opCode)});

    }

    @BeforeNotify
    public void beforeNotify(Message entry, int opCode) {
        lastExecutions.add(new Object[]{entry, FilterOperationCodes.operationCodeToString(opCode)});
    }

    @AfterNotifyTrigger
    public void afterNotifyTrigger(Message entry, Message entry2, int opCode) {
        lastExecutions.add(new Object[]{entry, entry2, FilterOperationCodes.operationCodeToString(opCode)});
    }

    public void clearExecutions() {
        this.lastExecutions.clear();
    }
}