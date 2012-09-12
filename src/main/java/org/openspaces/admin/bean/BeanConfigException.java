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

import java.rmi.RemoteException;

import org.openspaces.admin.AdminException;

/**
 * Base class for all admin Bean configuration exceptions.
 * 
 * @see BeanConfigManager
 * 
 * @author Moran Avigdor
 * @author Itai Frenkel
 * @since 8.0
 */
public class BeanConfigException extends AdminException {

	private static final long serialVersionUID = 1L;

	public BeanConfigException(String message) {
		super(message);
	}
	
	public BeanConfigException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * RemoteException sandwich model. 
	 * Derive and copy any required properties from cause.getCause() as needed.
	 * @param cause
	 */
    public BeanConfigException(RemoteException cause) {
        super(getMessage(cause),cause);
    }
    
    private static String getMessage(RemoteException remoteException) {
        String message = "remote exception occured";
        if (remoteException.getCause() != null) {
            message = remoteException.getCause().getMessage();
        }
        return message;
    }

}
