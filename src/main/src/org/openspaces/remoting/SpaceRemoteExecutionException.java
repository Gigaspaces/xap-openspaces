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

import java.util.concurrent.ExecutionException;

/**
 * An extension for {@link java.util.concurrent.ExecutionException} holding both the
 * {@link SpaceRemotingInvocation remoteInvocation} and the
 * {@link SpaceRemotingResult remoteResult} remote result.
 *
 * @author kimchy
 */
public class SpaceRemoteExecutionException extends ExecutionException {

    private static final long serialVersionUID = -8990362587695315894L;

    private SpaceRemotingInvocation remoteInvocation;

    private SpaceRemotingResult remoteResult;

    public SpaceRemoteExecutionException(SpaceRemotingInvocation remoteInvocation, String message, Throwable cause) {
        super(message, cause);
        this.remoteInvocation = remoteInvocation;
    }

    public SpaceRemoteExecutionException(SpaceRemotingInvocation remoteInvocation, SpaceRemotingResult remoteResult) {
        super("Remote Invocation failed with invocation [" + remoteInvocation + "]", remoteResult.getException());
        this.remoteInvocation = remoteInvocation;
        this.remoteResult = remoteResult;
    }

    /**
     * Returns the remote invocation that caused this execution exception.
     */
    public SpaceRemotingInvocation getRemoteInvocation() {
        return remoteInvocation;
    }

    /**
     * Returns the remote result that caused this execution exception.
     */
    public SpaceRemotingResult getRemoteResult() {
        return remoteResult;
    }
}
