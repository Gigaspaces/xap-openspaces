package org.openspaces.admin.quiesce;

import com.gigaspaces.admin.quiesce.QuiesceState;
import com.gigaspaces.admin.quiesce.QuiesceToken;

/**
 * Created by Barak Bar Orion
 * 1/28/15.
 */
public class QuiesceResult{

    private QuiesceState status;
    private QuiesceToken token;
    private String description;

    public QuiesceResult() {
    }

    public QuiesceResult(QuiesceState status, QuiesceToken token, String description) {
        this.status = status;
        this.token = token;
        this.description = description;
    }

    public QuiesceState getStatus() {
        return status;
    }

    public QuiesceToken getToken() {
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
