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

package org.openspaces.remoting.scripting;

import java.util.Map;

/**
 * An SPI implementation for script executor ("on the server side") that can handle a specific script
 * type (or labguage).
 *
 * @author kimchy
 */
public interface LocalScriptExecutor<T> {

    /**
     * Compiles the given sctipt.
     */
    T compile(Script script) throws ScriptCompilationException;

    /**
     * Executes the given compiled script.
     */
    Object execute(Script script, T compiledScript, Map<String, Object> parameters) throws ScriptExecutionException;

    /**
     * Closes the compiled script.
     */
    void close(T compiledScript);

    /**
     * Returns <code>true</code> if the same compiled script can be used by different threads (note, parameres
     * or bindings usually make a scripting library not thread safe).
     */
    boolean isThreadSafe();
}
