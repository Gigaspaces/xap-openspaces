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

package org.openspaces.itest.core.space.mode.listeners;

import java.util.HashMap;

import org.openspaces.core.space.mode.AfterSpaceModeChangeEvent;
import org.openspaces.core.space.mode.BeforeSpaceModeChangeEvent;
import org.openspaces.core.space.mode.SpaceAfterBackupListener;
import org.openspaces.core.space.mode.SpaceAfterPrimaryListener;
import org.openspaces.core.space.mode.SpaceBeforeBackupListener;
import org.openspaces.core.space.mode.SpaceBeforePrimaryListener;

import com.gigaspaces.cluster.activeelection.SpaceMode;

/**
 * @author shaiw
 */
public class SpaceModeListenerBean implements SpaceBeforeBackupListener,
                                              SpaceBeforePrimaryListener, 
                                              SpaceAfterPrimaryListener,
                                              SpaceAfterBackupListener {

    HashMap<String , SpaceMode> state = new HashMap<String, SpaceMode>();
    
    public void onBeforeBackup(BeforeSpaceModeChangeEvent event) {
        state.put(event.getSpace().getURL().toString(), event.getSpaceMode());
    }

    public void onBeforePrimary(BeforeSpaceModeChangeEvent event) {
        state.put(event.getSpace().getURL().toString(), event.getSpaceMode());
    }

    public void onAfterPrimary(AfterSpaceModeChangeEvent event) {
        state.put(event.getSpace().getURL().toString(), event.getSpaceMode());
    }

    public void onAfterBackup(AfterSpaceModeChangeEvent event) {
        state.put(event.getSpace().getURL().toString(), event.getSpaceMode());
    }
    
}
