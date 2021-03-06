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

package org.openspaces.itest.core.context;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;

/**
 * A test bean that gets injected with two differnt {@link org.openspaces.core.GigaSpace} instances
 * based on the bean name.
 *
 * @author kimchy
 */
public class DoubleContextTestBean {

    @GigaSpaceContext(name = "gs1")
    GigaSpace gs1;

    GigaSpace gs2;

    @GigaSpaceContext(name = "gs2")
    public void setGs2(GigaSpace gs2) {
        this.gs2 = gs2;
    }
}
