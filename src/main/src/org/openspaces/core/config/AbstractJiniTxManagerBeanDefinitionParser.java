package org.openspaces.core.config;

import org.openspaces.core.transaction.manager.TransactionLeaseRenewalConfig;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author kimchy
 */
public abstract class AbstractJiniTxManagerBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        Element renewEle = DomUtils.getChildElementByTagName(element, "renew");
        if (renewEle != null) {
            TransactionLeaseRenewalConfig renewalConfig = new TransactionLeaseRenewalConfig();
            String duration = renewEle.getAttribute("duration");
            if (StringUtils.hasText(duration)) {
                renewalConfig.setRenewDuration(Long.parseLong(duration));
            }
            String rtt = renewEle.getAttribute("round-trip-time");
            if (StringUtils.hasText(rtt)) {
                renewalConfig.setRenewRTT(Long.parseLong(rtt));
            }
            String poolSize = renewEle.getAttribute("pool-getSize");
            if (StringUtils.hasText(poolSize)) {
                renewalConfig.setPoolSize(Integer.parseInt(poolSize));
            }
            builder.addPropertyValue("leaseRenewalConfig", renewalConfig);
        }
    }
}
