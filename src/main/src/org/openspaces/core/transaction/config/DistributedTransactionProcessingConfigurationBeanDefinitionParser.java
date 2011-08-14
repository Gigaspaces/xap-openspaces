package org.openspaces.core.transaction.config;

import org.openspaces.core.transaction.DistributedTransactionProcessingConfigurationFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * A bean definition parser for distributed transaction support configuration for mirror & sink components.
 * 
 * @author idan
 * @since 8.0.4
 *
 */
public class DistributedTransactionProcessingConfigurationBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    final private static String WAIT_FOR_OPERATIONS = "dist-tx-wait-for-opers";
    final private static String WAIT_TIMEOUT = "dist-tx-wait-timeout-millis";
    
    
    @Override
    protected Class<DistributedTransactionProcessingConfigurationFactoryBean> getBeanClass(Element element) {
        return DistributedTransactionProcessingConfigurationFactoryBean.class;
    }
    
    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
    
        final String distributedTransactionWaitTimeout = element.getAttribute(WAIT_TIMEOUT);
        if (StringUtils.hasLength(distributedTransactionWaitTimeout))
            builder.addPropertyValue("distributedTransactionWaitTimeout", distributedTransactionWaitTimeout);

        final String distributedTransactionWaitForOperations = element.getAttribute(WAIT_FOR_OPERATIONS);
        if (StringUtils.hasLength(distributedTransactionWaitTimeout))
            builder.addPropertyValue("distributedTransactionWaitForOperations", distributedTransactionWaitForOperations);
        
    }        

}
