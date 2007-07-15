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

package org.openspaces.events.adapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A space data event annotation allowing to mark methods as delegates to be executed when an event
 * occurs.
 * 
 * <p>
 * Note, methods can have no parameters. They can also have one or more parameters ordered based on
 * {@link org.openspaces.events.SpaceDataEventListener#onEvent(Object, org.openspaces.core.GigaSpace, org.springframework.transaction.TransactionStatus, Object)}.
 * 
 * @author kimchy
 * @see org.openspaces.events.adapter.AnnotationEventListenerAdapter
 */
@Target( { ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SpaceDataEvent {
}
