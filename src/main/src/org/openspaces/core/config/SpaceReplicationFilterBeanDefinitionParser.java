package org.openspaces.core.config;

import org.openspaces.core.space.filter.replication.DefaultReplicationFilterProviderFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * A bean definition parser for {@link org.openspaces.core.space.filter.replication.DefaultReplicationFilterProviderFactory}.
 *
 * @author kimchy
 */
public class SpaceReplicationFilterBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    protected Class getBeanClass(Element element) {
        return DefaultReplicationFilterProviderFactory.class;
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        Element filterEle = DomUtils.getChildElementByTagName(element, "input-filter");
        if (filterEle != null) {
            Object filter = parserContext.getDelegate().parsePropertyValue(filterEle, builder.getRawBeanDefinition(), "input-filter");
            builder.addPropertyValue("inputFilter", filter);
        }

        filterEle = DomUtils.getChildElementByTagName(element, "output-filter");
        if (filterEle != null) {
            Object filter = parserContext.getDelegate().parsePropertyValue(filterEle, builder.getRawBeanDefinition(), "output-filter");
            builder.addPropertyValue("outputFilter", filter);
        }
    }
}
