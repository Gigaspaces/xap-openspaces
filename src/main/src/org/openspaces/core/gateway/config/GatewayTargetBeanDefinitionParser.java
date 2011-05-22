package org.openspaces.core.gateway.config;

import org.openspaces.core.gateway.GatewayTarget;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

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
    }
    
}
