package org.openspaces.core;

import com.j_spaces.core.client.sql.SQLQueryException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * An exception indicating wrong SQL query usage.
 *
 * @author kimchy
 * @see com.j_spaces.core.client.sql.SQLQueryException
 */
public class BadSqlQueryException extends InvalidDataAccessResourceUsageException {

    public BadSqlQueryException(SQLQueryException e) {
        super(e.getMessage(), e);
    }
}
