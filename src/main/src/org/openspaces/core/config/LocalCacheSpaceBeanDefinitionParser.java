package org.openspaces.core.config;

import org.openspaces.core.space.cache.LocalCacheSpaceFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * A bean definition builder for {@link LocalCacheSpaceFactoryBean}.
 * 
 * @author kimchy
 */
public class LocalCacheSpaceBeanDefinitionParser extends AbstractLocalCacheSpaceBeanDefinitionParser {

    public static final String UPDATE_MODE = "update-mode";

    protected Class<LocalCacheSpaceFactoryBean> getBeanClass(Element element) {
        return LocalCacheSpaceFactoryBean.class;
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        String updateMode = element.getAttribute(UPDATE_MODE);
        if (StringUtils.hasLength(updateMode)) {
            builder.addPropertyValue("updateModeName", updateMode);
        }
    }

}
