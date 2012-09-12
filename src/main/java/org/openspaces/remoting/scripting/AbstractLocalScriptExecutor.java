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

/**
 * @author kimchy
 */
public abstract class AbstractLocalScriptExecutor<T> implements LocalScriptExecutor<T> {

    public T compile(Script script) throws ScriptCompilationException {
        if (script instanceof LazyLoadingScript) {
            LazyLoadingScript lazyLoadingScript = (LazyLoadingScript) script;
            if (!lazyLoadingScript.hasScript()) {
                throw new ScriptNotLoadedException("Script [" + script.getName() + "] not loaded");
            }
        }
        return doCompile(script);
    }

    protected abstract T doCompile(Script script) throws ScriptCompilationException;
}
