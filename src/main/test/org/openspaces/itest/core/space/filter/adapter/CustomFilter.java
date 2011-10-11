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

import java.util.ArrayList;
import java.util.List;

/**
 * @author kimchy
 */
public class CustomFilter {

    @GigaSpaceLateContext(name = "gigaSpace")
    GigaSpace gigaSpace;

    private List<Object[]> lastExecutions = new ArrayList<Object[]>();

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
    public void beforeWrite(ISpaceFilterEntry entry, int operationCode) {
        entry.setFieldValue("message", "changed at beforeWrite");
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
        entry.setFieldValue("message", "test2");
        lastExecutions.add(new Object[]{entry, operationCode});
    }

    @AfterRead
    public void afterRead(ISpaceFilterEntry entry, int operationCode) {
        entry.setFieldValue("message", "changed at afterRead");
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
}