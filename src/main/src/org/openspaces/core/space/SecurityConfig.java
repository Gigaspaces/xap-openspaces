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

package org.openspaces.core.space;

import com.j_spaces.core.SecurityContext;
import org.springframework.util.StringUtils;

/**
 * A configuration object allowing to configure security context (username, password) when
 * working with the Space.
 *
 * @author kimchy
 */
public class SecurityConfig {

    private String username = SecurityContext.ANONYMOUS_USER;

    private String password = SecurityContext.ANONYMOUS_USER;

    public SecurityConfig() {
    }

    public SecurityConfig(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Returns the username to connect to the Space with.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username to connect to the Space with.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the password to connect to the Space with.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password to connect to the Space with.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isFilled() {
        return StringUtils.hasText(username) && StringUtils.hasText(password);
    }
}
