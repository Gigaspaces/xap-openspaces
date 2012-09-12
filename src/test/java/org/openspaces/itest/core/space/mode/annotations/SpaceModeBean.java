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

package org.openspaces.itest.core.space.mode.annotations;

import java.util.HashMap;

import org.openspaces.core.space.mode.AfterSpaceModeChangeEvent;
import org.openspaces.core.space.mode.BeforeSpaceModeChangeEvent;
import org.openspaces.core.space.mode.PostBackup;
import org.openspaces.core.space.mode.PostPrimary;
import org.openspaces.core.space.mode.PreBackup;
import org.openspaces.core.space.mode.PrePrimary;

import com.gigaspaces.cluster.activeelection.SpaceMode;

/**
 * @author shaiw
 */
public class SpaceModeBean {

    HashMap<String , SpaceMode> state = new HashMap<String, SpaceMode>();
    
    @PreBackup
    public void onBeforeBackup(BeforeSpaceModeChangeEvent event) {
        state.put(event.getSpace().getURL().toString(), event.getSpaceMode());
    }

    @PrePrimary
    public void onBeforePrimary(BeforeSpaceModeChangeEvent event) {
        state.put(event.getSpace().getURL().toString(), event.getSpaceMode());
    }

    @PostPrimary
    public void onAfterPrimary(AfterSpaceModeChangeEvent event) {
        state.put(event.getSpace().getURL().toString(), event.getSpaceMode());
    }

    @PostBackup
    public void onAfterBackup(AfterSpaceModeChangeEvent event) {
        state.put(event.getSpace().getURL().toString(), event.getSpaceMode());
    }
    
}
