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

package org.openspaces.core.cluster;

/**
 * A marker interface allowing beans to indicate the liveliness of a cluster member.
 *
 * @author kimchy
 */
public interface MemberAliveIndicator {

    /**
     * Should this member be checked to see if it is alive or not.
     */
    boolean isMemberAliveEnabled();
    
    /**
     * Return <code>true</code> if the member is alive or not. An exception thrown
     * is considered as an indication that the member is not alive, and allows for
     * further information to be supplies as to the reason that the member is not
     * alive.
     */
    boolean isAlive() throws Exception;
}
