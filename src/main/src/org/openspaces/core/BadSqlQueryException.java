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

    private static final long serialVersionUID = 2097131282296904506L;

    public BadSqlQueryException(SQLQueryException e) {
        super(e.getMessage(), e);
    }
}
