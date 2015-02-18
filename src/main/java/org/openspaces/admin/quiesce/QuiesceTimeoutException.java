/*
 * Copyright 2002-2006 the original author or authors.
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

package org.openspaces.admin.quiesce;

/**
 * @author Boris
 * @since 10.1.0
 */
public class QuiesceTimeoutException extends Exception {

    private final QuiesceResult result;

    public QuiesceTimeoutException(String message, QuiesceResult result) {
        super(message + ", Current Quiesce Results: " + result);
        this.result = result;
    }

    public QuiesceResult getQuiesceResult() {
        return result;
    }

}
