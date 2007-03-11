package org.openspaces.core.config;

import org.openspaces.core.space.cache.LocalViewSpaceFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

/**
 * @author kimchy
 */
public class LocalViewSpaceBeanDefinitionParser extends AbstractLocalCacheSpaceBeanDefinitionParser {

    protected Class getBeanClass(Element element) {
        return LocalViewSpaceFactoryBean.class;
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        List viewQueryElements = DomUtils.getChildElementsByTagName(element, "view-query");
        ManagedList list = new ManagedList(viewQueryElements.size());
        for (int i = 0; i < viewQueryElements.size(); i++) {
            Element ele = (Element) viewQueryElements.get(i);
            list.add(parserContext.getDelegate().parsePropertySubElement(ele, builder.getRawBeanDefinition(), null));
        }
        builder.addPropertyValue("localViews", list);
    }

}