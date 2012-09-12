/*******************************************************************************
 * 
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
 *  
 ******************************************************************************/
package org.openspaces.core.gateway.config;

import org.openspaces.core.gateway.GatewayTarget;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.j_spaces.core.cluster.RedoLogCapacityExceededPolicy;

/**
 * 
 * @author Idan Moyal
 * @since 8.0.3
 *
 */
public class GatewayTargetBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {
    
    private static final String TARGET_NAME = "name";
    private static final String BULK_SIZE = "bulk-size";
    private static final String IDLE_TIME_THRESHOLD = "idle-time-threshold";
    private static final String MAX_REDOLOG_CAPACITY = "max-redo-log-capacity";
    private static final String PENDING_OPERATION_THRESHOLD = "pending-operation-threshold";
    private static final String ON_REDOLOG_CAPACITY_EXCEEDED = "on-redo-log-capacity-exceeded";
    
    @Override
    protected Class<GatewayTarget> getBeanClass(Element element) {
        return GatewayTarget.class;
    }

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }
    
    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        String targetName = element.getAttribute(TARGET_NAME);
        if (StringUtils.hasLength(targetName))
            builder.addPropertyValue("name", targetName);

        parseGatewayTargetAttributes(element, builder);
    }

    /**
     * Parse gateway target attributes.
     * Relevant for 'gateway-targets' & 'gateway-target' elements.
     * @param element The element to parse.
     * @param builder The builder representing the output object.
     */
    static void parseGatewayTargetAttributes(Element element, BeanDefinitionBuilder builder) {
        String bulkSize = element.getAttribute(BULK_SIZE);
        if (StringUtils.hasLength(bulkSize))
            builder.addPropertyValue("bulkSize", bulkSize);
        
        String idleTimeThreshold = element.getAttribute(IDLE_TIME_THRESHOLD);
        if (StringUtils.hasText(idleTimeThreshold))
            builder.addPropertyValue("idleTimeThreshold", idleTimeThreshold);
        
        String maxRedoLogCapacity = element.getAttribute(MAX_REDOLOG_CAPACITY);
        if (StringUtils.hasText(maxRedoLogCapacity))
            builder.addPropertyValue("maxRedoLogCapacity", maxRedoLogCapacity);
        
        String pendingOperationThreshold = element.getAttribute(PENDING_OPERATION_THRESHOLD);
        if (StringUtils.hasText(pendingOperationThreshold))
            builder.addPropertyValue("pendingOperationThreshold", pendingOperationThreshold);
        
        String onRedoLogCapacityExceeded = element.getAttribute(ON_REDOLOG_CAPACITY_EXCEEDED);
        if (StringUtils.hasText(onRedoLogCapacityExceeded))
            builder.addPropertyValue("onRedoLogCapacityExceeded", parseOnRedoLogCapacityExceededString(onRedoLogCapacityExceeded));
    }

    private static RedoLogCapacityExceededPolicy parseOnRedoLogCapacityExceededString(String onRedoLogCapacityExceededString) {
        final String DROP_OLDEST = "drop-oldest";
        if (DROP_OLDEST.equals(onRedoLogCapacityExceededString))
            return RedoLogCapacityExceededPolicy.DROP_OLDEST;
        final String BLOCK_OPERATIONS = "block-operations";
        if (BLOCK_OPERATIONS.equals(onRedoLogCapacityExceededString))
            return RedoLogCapacityExceededPolicy.BLOCK_OPERATIONS;
        throw new IllegalArgumentException("onRedoLogCapacityExceeded only accepts the following values: [" + DROP_OLDEST + ", " + BLOCK_OPERATIONS + "]");
    }
    
    
}
