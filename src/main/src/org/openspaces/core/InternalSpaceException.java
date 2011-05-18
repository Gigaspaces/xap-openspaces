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

package org.openspaces.core;

import com.j_spaces.core.exception.internal.ProxyInternalSpaceException;
import org.springframework.dao.DataAccessException;

/**
 * A wrapper for {@link net.jini.space.InternalSpaceException}.
 *
 * @author kimchy
 */
public class InternalSpaceException extends DataAccessException {

    private static final long serialVersionUID = -4486328522662794541L;

    private net.jini.space.InternalSpaceException e;

    public InternalSpaceException(net.jini.space.InternalSpaceException e) {
        super(e.getMessage(), e);
    }

    public InternalSpaceException(ProxyInternalSpaceException e) {
        super(e.getMessage(), e);
    }
}
