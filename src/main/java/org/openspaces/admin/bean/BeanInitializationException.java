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

package org.openspaces.admin.bean;

/**
 * Exception indicating that an initialization of a bean failed.
 * 
 * @see BeanConfigManager#enableBean(Class)
 * 
 * @author Moran Avigdor
 * @author Itai Frenkel
 * @since 8.0
 */
public class BeanInitializationException extends BeanConfigException {

	private static final long serialVersionUID = 1L;

	public BeanInitializationException(String message) {
		super(message);
	}
	
	public BeanInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
