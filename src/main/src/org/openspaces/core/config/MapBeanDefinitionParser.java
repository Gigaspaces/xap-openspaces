package org.openspaces.core.config;

import org.openspaces.core.map.MapFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author kimchy
 */
public class MapBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    public static final String SPACE = "space";

    protected Class getBeanClass(Element element) {
        return MapFactoryBean.class;
    }

    protected boolean isEligibleAttribute(String attributeName) {
        return super.isEligibleAttribute(attributeName) && !SPACE.equals(attributeName);
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        String space = element.getAttribute(SPACE);
        if (StringUtils.hasLength(space)) {
            builder.addPropertyReference("space", space);
        }

        String compression = element.getAttribute("compression");
        if (StringUtils.hasLength(compression)) {
            builder.addPropertyValue("compression", compression);
        }

        Element localCacheSettingEle = DomUtils.getChildElementByTagName(element, "local-cache-support");
        if (localCacheSettingEle != null) {
            Object template = parserContext.getDelegate().parsePropertySubElement(localCacheSettingEle, builder.getRawBeanDefinition());
            builder.addPropertyValue("localCacheSupport", template);
        }
    }

}
