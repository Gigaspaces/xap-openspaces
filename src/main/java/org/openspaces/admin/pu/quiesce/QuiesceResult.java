package org.openspaces.admin.pu.quiesce;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Barak Bar Orion
 * 1/28/15.
 */
public class QuiesceResult implements Serializable {

    private static final long serialVersionUID = -128705742407213814L;
    private QuiesceState status;
    private UUID token;
    private String description;

    public QuiesceResult() {
    }

    public QuiesceResult(QuiesceState status, UUID token, String description) {
        this.status = status;
        this.token = token;
        this.description = description;
    }

    public QuiesceState getStatus() {
        return status;
    }

    public UUID getToken() {
        return token;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QuiesceResult that = (QuiesceResult) o;

        return token.equals(that.token);

    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }

    @Override
    public String toString() {
        return "QuiesceDetails{" +
                "status=" + status +
                ", token=" + token +
                ", description='" + description + '\'' +
                '}';
    }
}
