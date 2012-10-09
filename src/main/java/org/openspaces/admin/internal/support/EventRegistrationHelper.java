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
package org.openspaces.admin.internal.support;

import java.util.concurrent.TimeUnit;

import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.application.events.ApplicationAddedEventListener;
import org.openspaces.admin.application.events.ApplicationRemovedEventListener;
import org.openspaces.admin.esm.events.ElasticServiceManagerAddedEventListener;
import org.openspaces.admin.esm.events.ElasticServiceManagerRemovedEventListener;
import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningFailureEventListener;
import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningProgressChangedEventListener;
import org.openspaces.admin.gsa.events.GridServiceAgentAddedEventListener;
import org.openspaces.admin.gsa.events.GridServiceAgentRemovedEventListener;
import org.openspaces.admin.gsc.events.ElasticGridServiceContainerProvisioningFailureEventListener;
import org.openspaces.admin.gsc.events.ElasticGridServiceContainerProvisioningProgressChangedEventListener;
import org.openspaces.admin.gsc.events.GridServiceContainerAddedEventListener;
import org.openspaces.admin.gsc.events.GridServiceContainerRemovedEventListener;
import org.openspaces.admin.gsm.events.GridServiceManagerAddedEventListener;
import org.openspaces.admin.gsm.events.GridServiceManagerRemovedEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.lus.events.LookupServiceAddedEventListener;
import org.openspaces.admin.lus.events.LookupServiceRemovedEventListener;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningFailureEventListener;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningProgressChangedEventListener;
import org.openspaces.admin.machine.events.MachineAddedEventListener;
import org.openspaces.admin.machine.events.MachineRemovedEventListener;
import org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEventListener;
import org.openspaces.admin.os.events.OperatingSystemsStatisticsChangedEventListener;
import org.openspaces.admin.pu.elastic.events.ElasticAutoScalingFailureEventListener;
import org.openspaces.admin.pu.elastic.events.ElasticAutoScalingProgressChangedEventListener;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEventListener;
import org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitAddedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionStatusChangedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceStatisticsChangedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitRemovedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEventListener;
import org.openspaces.admin.space.events.ReplicationStatusChangedEventListener;
import org.openspaces.admin.space.events.SpaceAddedEventListener;
import org.openspaces.admin.space.events.SpaceInstanceAddedEventListener;
import org.openspaces.admin.space.events.SpaceInstanceRemovedEventListener;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEventListener;
import org.openspaces.admin.space.events.SpaceModeChangedEventListener;
import org.openspaces.admin.space.events.SpaceRemovedEventListener;
import org.openspaces.admin.space.events.SpaceStatisticsChangedEventListener;
import org.openspaces.admin.transport.events.TransportStatisticsChangedEventListener;
import org.openspaces.admin.transport.events.TransportsStatisticsChangedEventListener;
import org.openspaces.admin.vm.events.VirtualMachineAddedEventListener;
import org.openspaces.admin.vm.events.VirtualMachineRemovedEventListener;
import org.openspaces.admin.vm.events.VirtualMachineStatisticsChangedEventListener;
import org.openspaces.admin.vm.events.VirtualMachinesStatisticsChangedEventListener;
import org.openspaces.admin.zone.events.ZoneAddedEventListener;
import org.openspaces.admin.zone.events.ZoneRemovedEventListener;

/**
 * @author kimchy
 */
public abstract class EventRegistrationHelper {
    
    public static void addEventListener( final InternalAdmin admin, final AdminEventListener eventListener) {
        if (eventListener instanceof ZoneAddedEventListener) {
            admin.getZones().getZoneAdded().add((ZoneAddedEventListener) eventListener);
        }
        if (eventListener instanceof ZoneRemovedEventListener) {
            admin.getZones().getZoneRemoved().add((ZoneRemovedEventListener) eventListener);
        }
        if (eventListener instanceof MachineAddedEventListener) {
            admin.getMachines().getMachineAdded().add((MachineAddedEventListener) eventListener);
        }
        if (eventListener instanceof MachineRemovedEventListener) {
            admin.getMachines().getMachineRemoved().add((MachineRemovedEventListener) eventListener);
        }
        if (eventListener instanceof VirtualMachineAddedEventListener) {
            admin.getVirtualMachines().getVirtualMachineAdded().add((VirtualMachineAddedEventListener) eventListener);
        }
        if (eventListener instanceof VirtualMachineRemovedEventListener) {
            admin.getVirtualMachines().getVirtualMachineRemoved().add((VirtualMachineRemovedEventListener) eventListener);
        }
        if (eventListener instanceof LookupServiceAddedEventListener) {
            admin.getLookupServices().getLookupServiceAdded().add((LookupServiceAddedEventListener) eventListener);
        }
        if (eventListener instanceof LookupServiceRemovedEventListener) {
            admin.getLookupServices().getLookupServiceRemoved().add((LookupServiceRemovedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceManagerAddedEventListener) {
            admin.getGridServiceManagers().getGridServiceManagerAdded().add((GridServiceManagerAddedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceManagerRemovedEventListener) {
            admin.getGridServiceManagers().getGridServiceManagerRemoved().add((GridServiceManagerRemovedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceAgentAddedEventListener) {
            admin.getGridServiceAgents().getGridServiceAgentAdded().add((GridServiceAgentAddedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceAgentRemovedEventListener) {
            admin.getGridServiceAgents().getGridServiceAgentRemoved().add((GridServiceAgentRemovedEventListener) eventListener);
        }
        if (eventListener instanceof ElasticServiceManagerAddedEventListener) {
            admin.getElasticServiceManagers().getElasticServiceManagerAdded().add((ElasticServiceManagerAddedEventListener) eventListener);
        }
        if (eventListener instanceof ElasticServiceManagerRemovedEventListener) {
            admin.getElasticServiceManagers().getElasticServiceManagerRemoved().add((ElasticServiceManagerRemovedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceContainerAddedEventListener) {
            admin.getGridServiceContainers().getGridServiceContainerAdded().add((GridServiceContainerAddedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceContainerRemovedEventListener) {
            admin.getGridServiceContainers().getGridServiceContainerRemoved().add((GridServiceContainerRemovedEventListener) eventListener);
        }
        
        /*
         * Space listeners
         */
        
        if (eventListener instanceof SpaceAddedEventListener) {
            admin.getSpaces().getSpaceAdded().add((SpaceAddedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceRemovedEventListener) {
            admin.getSpaces().getSpaceRemoved().add((SpaceRemovedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceInstanceAddedEventListener) {
            admin.getSpaces().getSpaceInstanceAdded().add((SpaceInstanceAddedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceInstanceRemovedEventListener) {
            admin.getSpaces().getSpaceInstanceRemoved().add((SpaceInstanceRemovedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceModeChangedEventListener) {
            admin.getSpaces().getSpaceModeChanged().add((SpaceModeChangedEventListener) eventListener);
        }
        if (eventListener instanceof ReplicationStatusChangedEventListener) {
            admin.getSpaces().getReplicationStatusChanged().add((ReplicationStatusChangedEventListener) eventListener);
        }
        
        /*
         * Processing Unit listeners
         */
        
        if (eventListener instanceof ProcessingUnitAddedEventListener) {
            admin.getProcessingUnits().getProcessingUnitAdded().add((ProcessingUnitAddedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitRemovedEventListener) {
            admin.getProcessingUnits().getProcessingUnitRemoved().add((ProcessingUnitRemovedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitStatusChangedEventListener) {
            admin.getProcessingUnits().getProcessingUnitStatusChanged().add((ProcessingUnitStatusChangedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitInstanceAddedEventListener) {
            admin.getProcessingUnits().getProcessingUnitInstanceAdded().add((ProcessingUnitInstanceAddedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitInstanceRemovedEventListener) {
            admin.getProcessingUnits().getProcessingUnitInstanceRemoved().add((ProcessingUnitInstanceRemovedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitInstanceProvisionStatusChangedEventListener) {
            admin.getProcessingUnits().getProcessingUnitInstanceProvisionStatusChanged().add((ProcessingUnitInstanceProvisionStatusChangedEventListener)eventListener);
        }
        if (eventListener instanceof ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener) {
            admin.getProcessingUnits().getProcessingUnitInstanceMemberAliveIndicatorStatusChanged().add((ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener) eventListener);
        }
        if (eventListener instanceof ManagingGridServiceManagerChangedEventListener) {
            admin.getProcessingUnits().getManagingGridServiceManagerChanged().add((ManagingGridServiceManagerChangedEventListener) eventListener);
        }
        if (eventListener instanceof BackupGridServiceManagerChangedEventListener) {
            admin.getProcessingUnits().getBackupGridServiceManagerChanged().add((BackupGridServiceManagerChangedEventListener) eventListener);
        }
        
        /*
         * Application listeners
         */
        
        if (eventListener instanceof ApplicationAddedEventListener ){
            admin.getApplications().getApplicationAdded().add( (ApplicationAddedEventListener ) eventListener);
        }
        if (eventListener instanceof ApplicationRemovedEventListener ){
            admin.getApplications().getApplicationRemoved().add( ( ApplicationRemovedEventListener ) eventListener);
        }

        /*
         * Statistics listeners
         */
        if( eventListener instanceof StatisticsListenersRegistrationDelayAware ){
            
            long statisticsListenerRegistrationDelay = 
                ( ( StatisticsListenersRegistrationDelayAware )eventListener ).getStatisticsRegistrationDelay();
            
            admin.scheduleOneTimeWithDelayNonBlockingStateChange( new Runnable() {
                
                @Override
                public void run() {
                    addStatisticsListeners( admin, eventListener );
                }
            }   , statisticsListenerRegistrationDelay, TimeUnit.MILLISECONDS );
        }
        else{
            addStatisticsListeners( admin, eventListener );
        }
        
        /**
         * Elastic PU listeners
         */
        if (eventListener instanceof ElasticMachineProvisioningProgressChangedEventListener) {
            admin.getMachines().getElasticMachineProvisioningProgressChanged().add(
                    (ElasticMachineProvisioningProgressChangedEventListener) eventListener);
        }
        if (eventListener instanceof ElasticMachineProvisioningFailureEventListener) {
            admin.getMachines().getElasticMachineProvisioningFailure().add(
                    (ElasticMachineProvisioningFailureEventListener) eventListener);
        }
        if (eventListener instanceof ElasticGridServiceAgentProvisioningProgressChangedEventListener) {
            admin.getGridServiceAgents().getElasticGridServiceAgentProvisioningProgressChanged().add(
                    (ElasticGridServiceAgentProvisioningProgressChangedEventListener) eventListener);
        }
        if (eventListener instanceof ElasticGridServiceAgentProvisioningFailureEventListener) {
            admin.getGridServiceAgents().getElasticGridServiceAgentProvisioningFailure().add(
                    (ElasticGridServiceAgentProvisioningFailureEventListener) eventListener);
        }
        if (eventListener instanceof ElasticGridServiceContainerProvisioningProgressChangedEventListener) {
            admin.getGridServiceContainers().getElasticGridServiceContainerProvisioningProgressChanged().add(
                    (ElasticGridServiceContainerProvisioningProgressChangedEventListener) eventListener);
        }
        if (eventListener instanceof ElasticGridServiceContainerProvisioningFailureEventListener) {
            admin.getGridServiceContainers().getElasticGridServiceContainerProvisioningFailure().add(
                    (ElasticGridServiceContainerProvisioningFailureEventListener) eventListener);
        }
        if (eventListener instanceof ElasticAutoScalingProgressChangedEventListener) {
            admin.getProcessingUnits().getElasticAutoScalingProgressChanged().add(
                    (ElasticAutoScalingProgressChangedEventListener) eventListener);
        }
        if (eventListener instanceof ElasticAutoScalingFailureEventListener) {
            admin.getProcessingUnits().getElasticAutoScalingFailure().add(
                    (ElasticAutoScalingFailureEventListener) eventListener);
        }
    }
    

    private static void addStatisticsListeners(InternalAdmin admin, AdminEventListener eventListener) {

        if (eventListener instanceof VirtualMachineStatisticsChangedEventListener) {
            admin.getVirtualMachines().getVirtualMachineStatisticsChanged().add(
                    (VirtualMachineStatisticsChangedEventListener) eventListener, true);
        }
        if (eventListener instanceof VirtualMachinesStatisticsChangedEventListener) {
            admin.getVirtualMachines().getStatisticsChanged().add(
                    (VirtualMachinesStatisticsChangedEventListener) eventListener, true);
        }
        if (eventListener instanceof TransportsStatisticsChangedEventListener) {
            admin.getTransports().getStatisticsChanged().add(
                    (TransportsStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof TransportStatisticsChangedEventListener) {
            admin.getTransports().getTransportStatisticsChanged().add(
                    (TransportStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof OperatingSystemsStatisticsChangedEventListener) {
            admin.getOperatingSystems().getStatisticsChanged().add(
                    (OperatingSystemsStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof OperatingSystemStatisticsChangedEventListener) {
            admin.getOperatingSystems().getOperatingSystemStatisticsChanged().add(
                    (OperatingSystemStatisticsChangedEventListener) eventListener);
        }
        if( eventListener instanceof ProcessingUnitInstanceStatisticsChangedEventListener ){
            admin.getProcessingUnits().getProcessingUnitInstanceStatisticsChanged().add(
                    (ProcessingUnitInstanceStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceStatisticsChangedEventListener) {
            admin.getSpaces().getSpaceStatisticsChanged().add(
                    (SpaceStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceInstanceStatisticsChangedEventListener) {
            admin.getSpaces().getSpaceInstanceStatisticsChanged().add(
                    (SpaceInstanceStatisticsChangedEventListener) eventListener);
        }
    }



    public static void removeEventListener(InternalAdmin admin, AdminEventListener eventListener) {
        if (eventListener instanceof ZoneAddedEventListener) {
            admin.getZones().getZoneAdded().remove((ZoneAddedEventListener) eventListener);
        }
        if (eventListener instanceof ZoneRemovedEventListener) {
            admin.getZones().getZoneRemoved().remove((ZoneRemovedEventListener) eventListener);
        }
        if (eventListener instanceof MachineAddedEventListener) {
            admin.getMachines().getMachineAdded().remove((MachineAddedEventListener) eventListener);
        }
        if (eventListener instanceof MachineRemovedEventListener) {
            admin.getMachines().getMachineRemoved().remove((MachineRemovedEventListener) eventListener);
        }
        if (eventListener instanceof VirtualMachineAddedEventListener) {
            admin.getVirtualMachines().getVirtualMachineAdded().remove((VirtualMachineAddedEventListener) eventListener);
        }
        if (eventListener instanceof VirtualMachineRemovedEventListener) {
            admin.getVirtualMachines().getVirtualMachineRemoved().remove((VirtualMachineRemovedEventListener) eventListener);
        }
        if (eventListener instanceof VirtualMachineStatisticsChangedEventListener) {
            admin.getVirtualMachines().getVirtualMachineStatisticsChanged().remove((VirtualMachineStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof VirtualMachinesStatisticsChangedEventListener) {
            admin.getVirtualMachines().getStatisticsChanged().remove((VirtualMachinesStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof TransportsStatisticsChangedEventListener) {
            admin.getTransports().getStatisticsChanged().remove((TransportsStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof TransportStatisticsChangedEventListener) {
            admin.getTransports().getTransportStatisticsChanged().remove((TransportStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof OperatingSystemsStatisticsChangedEventListener) {
            admin.getOperatingSystems().getStatisticsChanged().remove((OperatingSystemsStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof OperatingSystemStatisticsChangedEventListener) {
            admin.getOperatingSystems().getOperatingSystemStatisticsChanged().remove((OperatingSystemStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof LookupServiceAddedEventListener) {
            admin.getLookupServices().getLookupServiceAdded().remove((LookupServiceAddedEventListener) eventListener);
        }
        if (eventListener instanceof LookupServiceRemovedEventListener) {
            admin.getLookupServices().getLookupServiceRemoved().remove((LookupServiceRemovedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceManagerAddedEventListener) {
            admin.getGridServiceManagers().getGridServiceManagerAdded().remove((GridServiceManagerAddedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceManagerRemovedEventListener) {
            admin.getGridServiceManagers().getGridServiceManagerRemoved().remove((GridServiceManagerRemovedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceAgentAddedEventListener) {
            admin.getGridServiceAgents().getGridServiceAgentAdded().remove((GridServiceAgentAddedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceAgentRemovedEventListener) {
            admin.getGridServiceAgents().getGridServiceAgentRemoved().remove((GridServiceAgentRemovedEventListener) eventListener);
        }
        if (eventListener instanceof ElasticServiceManagerAddedEventListener) {
            admin.getElasticServiceManagers().getElasticServiceManagerAdded().remove((ElasticServiceManagerAddedEventListener) eventListener);
        }
        if (eventListener instanceof ElasticServiceManagerRemovedEventListener) {
            admin.getElasticServiceManagers().getElasticServiceManagerRemoved().remove((ElasticServiceManagerRemovedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceContainerAddedEventListener) {
            admin.getGridServiceContainers().getGridServiceContainerAdded().remove((GridServiceContainerAddedEventListener) eventListener);
        }
        if (eventListener instanceof GridServiceContainerRemovedEventListener) {
            admin.getGridServiceContainers().getGridServiceContainerRemoved().remove((GridServiceContainerRemovedEventListener) eventListener);
        }
        

        /**
         * Space listeners
         */
        if (eventListener instanceof SpaceAddedEventListener) {
            admin.getSpaces().getSpaceAdded().remove((SpaceAddedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceRemovedEventListener) {
            admin.getSpaces().getSpaceRemoved().remove((SpaceRemovedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceInstanceAddedEventListener) {
            admin.getSpaces().getSpaceInstanceAdded().remove((SpaceInstanceAddedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceInstanceRemovedEventListener) {
            admin.getSpaces().getSpaceInstanceRemoved().remove((SpaceInstanceRemovedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceModeChangedEventListener) {
            admin.getSpaces().getSpaceModeChanged().remove((SpaceModeChangedEventListener) eventListener);
        }
        if (eventListener instanceof ReplicationStatusChangedEventListener) {
            admin.getSpaces().getReplicationStatusChanged().remove((ReplicationStatusChangedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceStatisticsChangedEventListener) {
            admin.getSpaces().getSpaceStatisticsChanged().remove((SpaceStatisticsChangedEventListener) eventListener);
        }
        if (eventListener instanceof SpaceInstanceStatisticsChangedEventListener) {
            admin.getSpaces().getSpaceInstanceStatisticsChanged().remove((SpaceInstanceStatisticsChangedEventListener) eventListener);
        }
        
        
        /*
         * Processing Unit listeners
         */
        if (eventListener instanceof ProcessingUnitAddedEventListener) {
            admin.getProcessingUnits().getProcessingUnitAdded().remove((ProcessingUnitAddedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitRemovedEventListener) {
            admin.getProcessingUnits().getProcessingUnitRemoved().remove((ProcessingUnitRemovedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitStatusChangedEventListener) {
            admin.getProcessingUnits().getProcessingUnitStatusChanged().remove((ProcessingUnitStatusChangedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitInstanceAddedEventListener) {
            admin.getProcessingUnits().getProcessingUnitInstanceAdded().remove((ProcessingUnitInstanceAddedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitInstanceRemovedEventListener) {
            admin.getProcessingUnits().getProcessingUnitInstanceRemoved().remove((ProcessingUnitInstanceRemovedEventListener) eventListener);
        }
        if (eventListener instanceof ProcessingUnitInstanceProvisionStatusChangedEventListener) {
            admin.getProcessingUnits().getProcessingUnitInstanceProvisionStatusChanged().remove((ProcessingUnitInstanceProvisionStatusChangedEventListener)eventListener);
        }
        if (eventListener instanceof ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener) {
            admin.getProcessingUnits().getProcessingUnitInstanceMemberAliveIndicatorStatusChanged().remove((ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener) eventListener);
        }
        if (eventListener instanceof ManagingGridServiceManagerChangedEventListener) {
            admin.getProcessingUnits().getManagingGridServiceManagerChanged().remove((ManagingGridServiceManagerChangedEventListener) eventListener);
        }
        if (eventListener instanceof BackupGridServiceManagerChangedEventListener) {
            admin.getProcessingUnits().getBackupGridServiceManagerChanged().remove((BackupGridServiceManagerChangedEventListener) eventListener);
        }
        if( eventListener instanceof ProcessingUnitInstanceStatisticsChangedEventListener ){
            admin.getProcessingUnits().getProcessingUnitInstanceStatisticsChanged().remove((ProcessingUnitInstanceStatisticsChangedEventListener) eventListener);
        }
        
        /*
         * Application listeners
         */
        if (eventListener instanceof ApplicationAddedEventListener ){
            admin.getApplications().getApplicationAdded().remove( (ApplicationAddedEventListener ) eventListener);
        }
        if (eventListener instanceof ApplicationRemovedEventListener ){
            admin.getApplications().getApplicationRemoved().remove( ( ApplicationRemovedEventListener ) eventListener);
        }
        
        /**
         * Elastic PU listeners
         */
        if (eventListener instanceof ElasticMachineProvisioningProgressChangedEventListener) {
            admin.getMachines().getElasticMachineProvisioningProgressChanged().remove(
                    (ElasticMachineProvisioningProgressChangedEventListener) eventListener);
        }
        if (eventListener instanceof ElasticMachineProvisioningFailureEventListener) {
            admin.getMachines().getElasticMachineProvisioningFailure().remove(
                    (ElasticMachineProvisioningFailureEventListener) eventListener);
        }
        if (eventListener instanceof ElasticGridServiceAgentProvisioningProgressChangedEventListener) {
            admin.getGridServiceAgents().getElasticGridServiceAgentProvisioningProgressChanged().remove(
                    (ElasticGridServiceAgentProvisioningProgressChangedEventListener) eventListener);
        }
        if (eventListener instanceof ElasticGridServiceAgentProvisioningFailureEventListener) {
            admin.getGridServiceAgents().getElasticGridServiceAgentProvisioningFailure().remove(
                    (ElasticGridServiceAgentProvisioningFailureEventListener) eventListener);
        }
        if (eventListener instanceof ElasticGridServiceContainerProvisioningProgressChangedEventListener) {
            admin.getGridServiceContainers().getElasticGridServiceContainerProvisioningProgressChanged().remove(
                    (ElasticGridServiceContainerProvisioningProgressChangedEventListener) eventListener);
        }
        if (eventListener instanceof ElasticGridServiceContainerProvisioningFailureEventListener) {
            admin.getGridServiceContainers().getElasticGridServiceContainerProvisioningFailure().remove(
                    (ElasticGridServiceContainerProvisioningFailureEventListener) eventListener);
        }
        if (eventListener instanceof ElasticAutoScalingProgressChangedEventListener) {
            admin.getProcessingUnits().getElasticAutoScalingProgressChanged().remove(
                    (ElasticAutoScalingProgressChangedEventListener) eventListener);
        }
        if (eventListener instanceof ElasticAutoScalingFailureEventListener) {
            admin.getProcessingUnits().getElasticAutoScalingFailure().remove(
                    (ElasticAutoScalingFailureEventListener) eventListener);
        }
    }
}
