package org.openspaces.core.config;

import com.gigaspaces.metadata.index.SpaceIndexType;

public class SpaceIdProperty {

    private String propertyName;
    private boolean autoGenerate;
    private SpaceIndexType index;
    
    public SpaceIndexType getIndex() {
        return index;
    }
    public void setIndex(SpaceIndexType index) {
        this.index = index;
    }
    public SpaceIdProperty() {
    }
    public String getPropertyName() {
        return propertyName;
    }
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
    public boolean isAutoGenerate() {
        return autoGenerate;
    }
    public void setAutoGenerate(boolean autoGenerate) {
        this.autoGenerate = autoGenerate;
    }
}
