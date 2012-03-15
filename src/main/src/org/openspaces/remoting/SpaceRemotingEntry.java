/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.remoting;

/**
 * 
 * @author Niv Ingberg
 * @since 8.0
 */
public interface SpaceRemotingEntry extends SpaceRemotingInvocation, SpaceRemotingResult, Cloneable {

    void setInvocation(Boolean invocation);
    
    void setInstanceId(Integer instanceId);
    
    void setLookupName(String lookupName);
    
    void setRouting(Object routing);

    void setMetaArguments(Object[] metaArguments);

    Boolean getOneWay();
    
    void setOneWay(Boolean oneWay);
        
    void setFifo(boolean isfifo);
    
    SpaceRemotingEntry buildResult(Object obj);
    
    SpaceRemotingEntry buildResult(Throwable e);
    
    SpaceRemotingEntry buildResultTemplate();
    
    Object clone() throws CloneNotSupportedException;
}
