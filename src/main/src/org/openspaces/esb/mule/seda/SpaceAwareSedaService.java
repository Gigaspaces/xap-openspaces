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

package org.openspaces.esb.mule.seda;

import org.mule.api.MuleException;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.model.seda.SedaService;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.space.mode.AfterSpaceModeChangeEvent;
import org.openspaces.core.space.mode.BeforeSpaceModeChangeEvent;
import org.openspaces.core.util.SpaceUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * An extension to Mule SEDA component that will only start when working with a PRIMARY space
 * and won't start when working with BACKUP space.
 *
 * @author kimchy
 */
public class SpaceAwareSedaService extends SedaService implements ApplicationListener {

    protected OpenSpacesSedaModel sedaModel;

    protected GigaSpace gigaSpace;

    public synchronized void doInitialise() throws InitialisationException {
        super.doInitialise();
        this.sedaModel = (OpenSpacesSedaModel) model;
        this.gigaSpace = sedaModel.getGigaSpaceObj();
    }

    public void start() throws MuleException {
        // do nothing here, it will be started based on the Space PRIMARY/BACKUP event
    }

    public void stop() throws MuleException {
        // do nothing here, it will be stopped based on the Space PRIMARY/BACKUP event
    }

    protected void doDispose() {
        try {
            // we need to stop here if was not stopped since during dispose it needs
            // to be stopped and we override stop to do nothing
            if (!stopped.get()) {
                stop();
            }
        }
        catch (MuleException e) {
            logger.error("Failed to stop component: " + name, e);
        }
    }

    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof AfterSpaceModeChangeEvent) {
            AfterSpaceModeChangeEvent spEvent = (AfterSpaceModeChangeEvent) applicationEvent;
            if (spEvent.isPrimary() && !isStarted()) {
                try {
                    if (gigaSpace != null) {
                        if (SpaceUtils.isSameSpace(spEvent.getSpace(), gigaSpace.getSpace())) {
                            super.start();
                        }
                    } else {
                        super.start();
                    }
                } catch (Exception e) {
                    logger.error("Failed to start component [" + name + "] when moving to primary mode", e);
                }
            }
        } else if (applicationEvent instanceof BeforeSpaceModeChangeEvent) {
            BeforeSpaceModeChangeEvent spEvent = (BeforeSpaceModeChangeEvent) applicationEvent;
            if (!spEvent.isPrimary() && isStarted()) {
                try {
                    if (gigaSpace != null) {
                        if (SpaceUtils.isSameSpace(spEvent.getSpace(), gigaSpace.getSpace())) {
                            super.stop();
                        }
                    } else {
                        super.stop();
                    }
                } catch (Exception e) {
                    logger.error("Failed to stop component [" + name + "] when moving to backup mode", e);
                }
            }
        }
    }
}
