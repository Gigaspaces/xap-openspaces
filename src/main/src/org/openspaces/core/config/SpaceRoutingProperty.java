package org.openspaces.core.config;

import com.gigaspaces.metadata.index.SpaceIndexType;

public class SpaceRoutingProperty {

    private String propertyName;
    private SpaceIndexType index;
    
    public SpaceIndexType getIndex() {
        return index;
    }
    public void setIndex(SpaceIndexType index) {
        this.index = index;
    }
    public SpaceRoutingProperty() {
    }
    public String getPropertyName() {
        return propertyName;
    }
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
  
}
