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

package org.openspaces.remoting;

import com.gigaspaces.async.AsyncResult;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.executor.DistributedTask;
import org.openspaces.core.executor.TaskRoutingProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.List;

/**
 * A {@link org.openspaces.core.executor.Task} that can be used to simulate remote invcation with
 * {@link org.openspaces.remoting.SpaceRemotingServiceExporter}. When executed, the task searches
 * for a service exporter (first under the hardwired name <code>serviceExporter</code>, then any
 * bean that define this class), and based on paramters passed on the task itself (such as method
 * name, lookup name and arguments) invokes service methods that are registered with the service
 * exporter.
 *
 * @author kimchy
 */
public class ExecutorRemotingTask<T extends Serializable> implements DistributedTask<ExecutorRemotingTask.InternalExecutorResult<T>, List<AsyncResult<ExecutorRemotingTask.InternalExecutorResult<T>>>>,
        ApplicationContextAware, ClusterInfoAware, TaskRoutingProvider, SpaceRemotingInvocation, Externalizable {

    private String lookupName;

    private String methodName;

    private Object[] arguments;

    private Object[] metaArguments;

    private Integer routing;

    private transient ApplicationContext applicationContext;

    private transient Integer instanceId;

    public ExecutorRemotingTask(String lookupName, String methodName, Object[] arguments) {
        this.lookupName = lookupName;
        this.methodName = methodName;
        this.arguments = arguments;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setClusterInfo(ClusterInfo clusterInfo) {
        this.instanceId = clusterInfo.getInstanceId();
    }

    public InternalExecutorResult<T> execute() throws Exception {
        SpaceRemotingServiceExporter serviceExporter = (SpaceRemotingServiceExporter) applicationContext.getBean("serviceExporter");
        if (serviceExporter == null) {
            String[] names = applicationContext.getBeanNamesForType(SpaceRemotingServiceExporter.class, false, true);
            if (names != null && names.length > 0) {
                serviceExporter = (SpaceRemotingServiceExporter) applicationContext.getBean(names[0]);
            }
        }
        if (serviceExporter == null) {
            throw new IllegalStateException("Failed to find remoting service exporter defined within the application context");
        }
        try {
            Object result = serviceExporter.invokeExecutor(this);
            return new InternalExecutorResult<T>((T) result, instanceId);
        } catch (Throwable e) {
            throw new InternalExecutorException(e, instanceId);
        }
    }

    public List<AsyncResult<InternalExecutorResult<T>>> reduce(List<AsyncResult<InternalExecutorResult<T>>> results) throws Exception {
        return results;
    }

    public Integer getRouting() {
        return routing;
    }

    void setRouting(Object routing) {
        this.routing = routing.hashCode();
    }

    public String getLookupName() {
        return lookupName;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public Object[] getMetaArguments() {
        return metaArguments;
    }

    void setMetaArguments(Object[] metaArguments) {
        this.metaArguments = metaArguments;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(lookupName);
        out.writeUTF(methodName);
        if (arguments == null) {
            out.writeInt(0);
        } else {
            out.writeInt(arguments.length);
            for (Object arg : arguments) {
                out.writeObject(arg);
            }
        }
        if (metaArguments == null) {
            out.writeInt(0);
        } else {
            out.writeInt(metaArguments.length);
            for (Object arg : metaArguments) {
                out.writeObject(arg);
            }
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        lookupName = in.readUTF();
        methodName = in.readUTF();
        int size = in.readInt();
        if (size > 0) {
            arguments = new Object[size];
            for (int i = 0; i < size; i++) {
                arguments[i] = in.readObject();
            }
        }
        size = in.readInt();
        if (size > 0) {
            metaArguments = new Object[size];
            for (int i = 0; i < size; i++) {
                metaArguments[i] = in.readObject();
            }
        }
    }

    public static class InternalExecutorResult<T extends Serializable> implements Externalizable {

        private T result;

        private Integer instanceId;

        public InternalExecutorResult() {
        }

        public InternalExecutorResult(T result, Integer instanceId) {
            this.result = result;
            this.instanceId = instanceId;
        }

        public T getResult() {
            return result;
        }

        public int getInstanceId() {
            return instanceId;
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            if (result == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                out.writeObject(result);
            }
            if (instanceId == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                out.writeInt(instanceId);
            }
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            if (in.readBoolean()) {
                result = (T) in.readObject();
            }
            if (in.readBoolean()) {
                instanceId = in.readInt();
            }
        }
    }

    public static class InternalExecutorException extends Exception implements Externalizable {

        private Throwable exception;

        private Integer instanceId;

        public InternalExecutorException() {
        }

        public InternalExecutorException(Throwable exception, Integer instanceId) {
            this.exception = exception;
            this.instanceId = instanceId;
        }

        public Throwable getException() {
            return exception;
        }

        public int getInstanceId() {
            return instanceId;
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(exception);
            if (instanceId == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                out.writeInt(instanceId);
            }
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            exception = (Throwable) in.readObject();
            if (in.readBoolean()) {
                instanceId = in.readInt();
            }
        }
    }
}
