package org.openspaces.itest.remoting.scripting;

import org.openspaces.remoting.scripting.AsyncScriptingExecutor;
import org.openspaces.remoting.scripting.ExecutorScriptingExecutor;
import org.openspaces.remoting.scripting.ScriptingExecutor;
import org.openspaces.remoting.scripting.StaticScript;
import org.openspaces.remoting.scripting.SyncScriptingExecutor;

/**
 * @author kimchy
 */
public class ScriptingAnnotationBean {

    @AsyncScriptingExecutor
    private ScriptingExecutor asyncScriptingExecutor;

    @SyncScriptingExecutor
    private ScriptingExecutor syncScriptingExecutor;

    @ExecutorScriptingExecutor
    private ScriptingExecutor executorScriptingExecutor;

    public Integer executeSyncScriptThatReturns2() {
        return (Integer) syncScriptingExecutor.execute(new StaticScript("executeSyncScriptThatReturns2", "groovy", "return 2"));
    }

    public Integer executeAsyncScriptThatReturns2() {
        return (Integer) syncScriptingExecutor.execute(new StaticScript("executeAsyncScriptThatReturns2", "groovy", "return 2"));
    }

    public Integer executeExecutorScriptThatReturns2() {
        return (Integer) executorScriptingExecutor.execute(new StaticScript("executeAsyncScriptThatReturns2", "groovy", "return 2"));
    }
}
