/*
 * Copyright 2014 the original author or authors.
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
package org.openspaces.core.space;

import com.j_spaces.core.IJSpace;

/**
 * @author Niv Ingberg
 * @since 10.0
 */
public abstract class AbstractSpaceConfigurer implements SpaceConfigurer {

    private IJSpace space;

    public IJSpace create() {
        if (space == null)
            space = createSpace();
        return space;
    }

    @Override
    public IJSpace space() {
        return create();
    }

    protected void validate() {
        if (space != null) {
            throw new IllegalArgumentException("Can't invoke method, space() has already been called");
        }
    }

    protected abstract IJSpace createSpace();
}
