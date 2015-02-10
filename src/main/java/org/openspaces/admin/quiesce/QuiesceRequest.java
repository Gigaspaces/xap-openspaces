package org.openspaces.admin.quiesce;

/**
 * Created by Barak Bar Orion
 * 1/28/15.
 */
public class QuiesceRequest {

    private String description;

    public QuiesceRequest() {
    }

    public QuiesceRequest(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QuiesceRequest that = (QuiesceRequest) o;

        if (description != null ? !description.equals(that.description) : that.description != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return description != null ? description.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "QuiesceRequest{" +
                "description='" + description + '\'' +
                '}';
    }
}
