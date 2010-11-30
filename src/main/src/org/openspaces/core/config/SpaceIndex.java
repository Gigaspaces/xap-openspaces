package org.openspaces.core.config;

/**
 * Super class for space indexes
 * @author anna
 * @since 8.0
 */
public class SpaceIndex {

    private String path;

    public SpaceIndex() {
        super();
    }

    public SpaceIndex(String path) {
        super();
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
