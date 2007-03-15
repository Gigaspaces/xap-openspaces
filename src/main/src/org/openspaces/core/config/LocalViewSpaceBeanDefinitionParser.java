package org.openspaces.core.config;

import org.openspaces.core.space.cache.LocalViewSpaceFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

/**
 * A bean definition builder for {@link LocalViewSpaceFactoryBean}.
 * 
 * @author kimchy
 */
public class LocalViewSpaceBeanDefinitionParser extends AbstractLocalCacheSpaceBeanDefinitionParser {

    protected Class<LocalViewSpaceFactoryBean> getBeanClass(Element element) {
        return LocalViewSpaceFactoryBean.class;
    }

    @SuppressWarnings("unchecked")
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        List<Element> viewQueryElements = DomUtils.getChildElementsByTagName(element, "view-query");
        ManagedList list = new ManagedList(viewQueryElements.size());
        for (Element ele : viewQueryElements) {
            list.add(parserContext.getDelegate().parsePropertySubElement(ele, builder.getRawBeanDefinition(), null));
        }
        builder.addPropertyValue("localViews", list);
    }

}