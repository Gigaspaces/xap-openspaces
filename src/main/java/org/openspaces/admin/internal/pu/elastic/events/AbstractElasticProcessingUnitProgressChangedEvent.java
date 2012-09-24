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
package org.openspaces.admin.internal.pu.elastic.events;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.openspaces.admin.internal.zone.config.ZonesConfigUtils;
import org.openspaces.admin.zone.config.ZonesConfig;

import com.gigaspaces.internal.io.IOUtils;

public abstract class AbstractElasticProcessingUnitProgressChangedEvent implements InternalElasticProcessingUnitProgressChangedEvent {
    
    private static final long serialVersionUID = -3682386855602620479L;
    
    private boolean isComplete;
    private String processingUnitName;
    private boolean isUndeploying;
    private ZonesConfig gridServiceAgentZones;
   
    /**
     * de-serialization/reflection constructor
     */
    public AbstractElasticProcessingUnitProgressChangedEvent() {
    }
    
    @Override
    public boolean isComplete() {
        return isComplete;
    }
    
    /**
     * @return the processing units that requires new machines
     */
    @Override
    public String getProcessingUnitName() {
        return processingUnitName;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeBoolean(isComplete);
        out.writeBoolean(isUndeploying);
        IOUtils.writeString(out, processingUnitName);
        writeZonesConfig(out, gridServiceAgentZones);
    }

    @Override
    public boolean isUndeploying() {
        return isUndeploying;
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        isComplete = in.readBoolean();
        isUndeploying = in.readBoolean();
        processingUnitName = IOUtils.readString(in);
        gridServiceAgentZones = readZonesConfig(in);
    }

    @Override
    public void setComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }

    @Override
    public void setUndeploying(boolean isUndeploying) {
        this.isUndeploying = isUndeploying;
    }

    @Override
    public void setProcessingUnitName(String processingUnitName) {
        this.processingUnitName = processingUnitName;
    }
    
    protected String toStringHelper(final String op) {
        final String adverb = isComplete() ? "completed." : "is in progress.";
        final String verb = isUndeploying() ? "undeployment" : "SLA enforcement";
        return "["+getProcessingUnitName() + "] "+op+" "+ verb+" " + adverb;
    }
    
    private static void writeZonesConfig(ObjectOutput out, ZonesConfig gridServiceAgentZones) throws IOException {
        if (gridServiceAgentZones == null) {
            IOUtils.writeString(out, null);
        }
        else {
            IOUtils.writeString(out, ZonesConfigUtils.zonesToString(gridServiceAgentZones));
        }
    }

    private static ZonesConfig readZonesConfig(ObjectInput in) throws IOException, ClassNotFoundException {
        final String zonesToString = IOUtils.readString(in);
        if (zonesToString == null) {
            return null;
        }
        return ZonesConfigUtils.zonesFromString(zonesToString);
    }

    public ZonesConfig getGridServiceAgentZones() {
        return gridServiceAgentZones;
    }

    public void setGridServiceAgentZones(ZonesConfig gridServiceAgentZones) {
        this.gridServiceAgentZones = gridServiceAgentZones;
    }

}
