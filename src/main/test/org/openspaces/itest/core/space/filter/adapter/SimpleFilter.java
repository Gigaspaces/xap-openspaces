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

import com.j_spaces.core.filters.FilterOperationCodes;
import com.j_spaces.core.filters.entry.ISpaceFilterEntry;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceLateContext;
import org.openspaces.core.space.filter.*;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kimchy
 */
public class SimpleFilter {

    @GigaSpaceLateContext(name = "gigaSpace")
    GigaSpace gigaSpace;

    @GigaSpaceLateContext(name = "txnGigaSpace")
    GigaSpace txnGigaSpace;

    PlatformTransactionManager mahaloTxManager;

    private static List<Object[]> lastExecutions = new ArrayList<Object[]>();

    private boolean onInitCalled;
    private boolean onCloseCalled;

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
    public void beforeWrite(Message entry, int operationCode) {
        lastExecutions.add(new Object[]{entry, operationCode});
    }

    @AfterWrite
    public void afterWrite(Message entry, int operationCode) {
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
    public void beforeRead(Message entry, int operationCode) {
        lastExecutions.add(new Object[]{entry, operationCode});
    }

    @AfterRead
    public void afterRead(Message entry, int operationCode) {
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
    public void afterExecute(ISpaceFilterEntry entry) {
        lastExecutions.add(new Object[]{entry});
    }

    @BeforeExecute
    public void beforeExecute(ISpaceFilterEntry entry) {
        lastExecutions.add(new Object[]{entry});
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

    @AfterRemoveByLease
    public void afterRemoveByLease(Message entry, int operationCode) {
        lastExecutions.add(new Object[]{entry, operationCode});
    }

    @BeforeRemoveByLease
    public void beforeRemoveByLease(Message entry, int operationCode) {
        lastExecutions.add(new Object[]{entry, operationCode});
    }
}