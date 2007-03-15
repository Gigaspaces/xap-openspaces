package org.openspaces.core.config;

import org.openspaces.core.space.filter.SpaceFilterProviderFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

/**
 * @author kimchy
 */
public class SpaceFilterBeanDefinitionParser extends AbstractFilterBeanDefinitionParser {

    protected Class getBeanClass(Element element) {
        return SpaceFilterProviderFactory.class;
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        List<Element> opeationCodesElements = DomUtils.getChildElementsByTagName(element, "operation");
        int[] operationCodes = new int[opeationCodesElements.size()];
        for (int i = 0; i < operationCodes.length; i++) {
            String operationCode = opeationCodesElements.get(i).getAttribute("code");
            operationCodes[i] = Integer.parseInt(operationCode);
        }
        builder.addPropertyValue("operationCodes", operationCodes);
    }
}
