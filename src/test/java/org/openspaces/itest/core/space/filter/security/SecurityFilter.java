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

package org.openspaces.itest.core.space.filter.security;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.SpaceContext;
import com.j_spaces.core.filters.ISpaceFilter;
import com.j_spaces.core.filters.entry.ISpaceFilterEntry;

import org.junit.Assert;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceLateContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kimchy
 */
public class SecurityFilter implements ISpaceFilter {

    private static Map<Integer, Integer> stats = new HashMap<Integer, Integer>();

    @GigaSpaceLateContext
    GigaSpace gigaSpace;

    public void init(IJSpace space, String filterId, String url, int priority) throws RuntimeException {

    }

    public void process(SpaceContext context, ISpaceFilterEntry entry, int operationCode) throws RuntimeException {
        if(operationCode == 6)
            Assert.assertNotNull(context);
        Integer counter = stats.get(operationCode);
        if (counter == null) {
            counter = 1;
        } else {
            counter = counter + 1;
        }
        stats.put(operationCode, counter);
    }

    public void process(SpaceContext context, ISpaceFilterEntry[] entries, int operationCode) throws RuntimeException {
        if(operationCode == 6)
            Assert.assertNotNull(context);
        Integer counter = stats.get(operationCode);
        if (counter == null) {
            counter = 1;
        } else {
            counter = counter + entries.length;
        }
        stats.put(operationCode, counter);
    }

    public void close() throws RuntimeException {
    }

    public Map<Integer, Integer> getStats() {
        return stats;
    }
}
