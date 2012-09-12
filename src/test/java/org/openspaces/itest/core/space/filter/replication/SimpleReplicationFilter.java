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

package org.openspaces.itest.core.space.filter.replication;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.cluster.IReplicationFilter;
import com.j_spaces.core.cluster.IReplicationFilterEntry;
import com.j_spaces.core.cluster.ReplicationPolicy;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceLateContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kimchy
 */
public class SimpleReplicationFilter implements IReplicationFilter {

    @GigaSpaceLateContext(name = "gigaSpace1")
    GigaSpace gigaSpace1;

    @GigaSpaceLateContext(name = "gigaSpace2")
    GigaSpace gigaSpace2;

    AtomicInteger initCalled = new AtomicInteger();

    AtomicInteger closeCalled = new AtomicInteger();

    boolean processCalled;

    List<ProcessEntry> processEntries = new ArrayList<ProcessEntry>();

    public void init(IJSpace space, String paramUrl, ReplicationPolicy replicationPolicy) {
        initCalled.incrementAndGet();
    }

    public void process(int direction, IReplicationFilterEntry replicationFilterEntry, String remoteSpaceMemberName) {
        ProcessEntry processEntry = new ProcessEntry();
        processEntry.direction = direction;
        processEntry.replicationFilterEntry = replicationFilterEntry;
        processEntry.remoteSpaceMemberName = remoteSpaceMemberName;
        processEntries.add(processEntry);
    }

    public void close() {
        closeCalled.incrementAndGet();
    }

    public static class ProcessEntry {
        int direction;

        IReplicationFilterEntry replicationFilterEntry;

        String remoteSpaceMemberName;
    }
}