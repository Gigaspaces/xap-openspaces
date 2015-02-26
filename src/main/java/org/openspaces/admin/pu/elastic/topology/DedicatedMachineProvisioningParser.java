package org.openspaces.admin.pu.elastic.topology;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author yohana
 * @since 10.1
 */
public class DedicatedMachineProvisioningParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected Class<?> getBeanClass(Element element) {
        return DedicatedMachineProvisioningInternal.class;
    }
    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        String config = element.getAttribute("elastic-machine-provisioning-config");
        if (StringUtils.hasLength(config)) {
            builder.addPropertyReference("elasticMachineProvisioningConfig", config);
        }
    }
}
