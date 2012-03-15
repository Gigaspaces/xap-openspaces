/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.core.internal;

import com.gigaspaces.async.AsyncFuture;
import net.jini.core.transaction.Transaction;
import org.openspaces.core.GigaSpace;

/**
 * An internal API of {@link org.openspaces.core.GigaSpace}
 *
 * @author kimchy
 */
public interface InternalGigaSpace extends GigaSpace {

    <T> AsyncFuture<T> wrapFuture(AsyncFuture<T> future, Transaction tx);
}
