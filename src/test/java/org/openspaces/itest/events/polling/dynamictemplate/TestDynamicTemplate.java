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
package org.openspaces.itest.events.polling.dynamictemplate;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.openspaces.events.DynamicEventTemplateProvider;
import org.openspaces.itest.events.pojos.MockPojo;

/**
 * @author Itai Frenkel
 * @since 9.1.1
 *
 */
public class TestDynamicTemplate implements DynamicEventTemplateProvider {

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    final AtomicInteger routing = new AtomicInteger();
    
    @Override
    public Object getDynamicTemplate() {
        final MockPojo template = new MockPojo();
        template.setProcessed(false);
        template.setRouting(routing.incrementAndGet());
        logger.info("getDynamicTemplate() returns " + template);
        return template;
    }

}
