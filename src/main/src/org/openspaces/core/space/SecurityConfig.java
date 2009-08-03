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

import com.gigaspaces.security.UserDetails;
import com.gigaspaces.security.User;
import org.springframework.util.StringUtils;

/**
 * A configuration object allowing to configure security context (username, password) when
 * working with the Space.
 *
 * @author kimchy
 */
public class SecurityConfig {

    private String username;

    private String password;

    private UserDetails userDetails;

    public SecurityConfig() {
    }

    public SecurityConfig(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public SecurityConfig(UserDetails userDetails) {
        this.userDetails = userDetails;
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

    public UserDetails toUserDetails() {
        if (!isFilled()) {
            return null;
        }
        if (userDetails != null) {
            return userDetails;
        }
        return new User(username, password);
    }

    public boolean isFilled() {
        if (userDetails != null) {
            return true;
        }
        return (StringUtils.hasText(username) && !"${security.username}".equals(username)) && (StringUtils.hasText(password) && !"${security.password}".equals(password));
    }
}
