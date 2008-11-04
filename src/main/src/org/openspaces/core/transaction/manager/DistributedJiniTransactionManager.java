/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.core.transaction.manager;

import com.j_spaces.kernel.ResourceLoader;
import com.sun.jini.admin.DestroyAdmin;
import com.sun.jini.start.LifeCycle;
import com.sun.jini.start.ServiceProxyAccessor;
import net.jini.admin.Administrable;
import net.jini.core.transaction.server.TransactionManager;
import org.openspaces.pu.container.servicegrid.PUServiceDetails;
import org.openspaces.pu.container.servicegrid.PlainPUServiceDetails;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Constructor;
import java.net.URL;

/**
 * A transaction manager that starts an embedded distributed mahalo transaction manager. This
 * transaction manager can be used to perform operations that span several Spaces or several
 * partitions within a Space.
 *
 * @author kimchy
 */
public class DistributedJiniTransactionManager extends AbstractJiniTransactionManager implements DisposableBean {

    private Object impl;

    private TransactionManager proxy;

    protected TransactionManager doCreateTransactionManager() throws Exception {
        URL mahaloConfig = ResourceLoader.getResourceURL("config/services/services.config");
        if (mahaloConfig == null)
            throw new IllegalArgumentException("Could not find Mahalo configuration (config/services/services.config)");

        Class mahaloClass = ClassUtils.forName("com.sun.jini.mahalo.TransientMahaloImpl");
        Constructor constructor = mahaloClass.getDeclaredConstructor(String[].class, LifeCycle.class, boolean.class);
        constructor.setAccessible(true);
        impl = constructor.newInstance(new String[] {mahaloConfig.toExternalForm()}, null, false);
        proxy = (TransactionManager) ((ServiceProxyAccessor) impl).getServiceProxy();
        return proxy;
    }

    public void destroy() throws Exception {
        if (proxy == null) {
            return;
        }
        try {
            Object adminObject = ((Administrable) proxy).getAdmin();
            if (adminObject instanceof DestroyAdmin) {
                ((DestroyAdmin) adminObject).destroy();
            }
        } finally {
            proxy = null;
            impl = null;
        }
    }

    public PUServiceDetails[] getServicesDetails() {
        return new PUServiceDetails[] {new PlainPUServiceDetails(SERVICE_TYPE, "distributed", "", "")};
    }
}
