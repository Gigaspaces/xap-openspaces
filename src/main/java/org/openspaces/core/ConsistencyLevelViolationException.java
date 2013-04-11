/*******************************************************************************
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 *
 * The software source code is proprietary and confidential information of GigaSpaces. 
 * You may use the software source code solely under the terms and limitations of 
 * The license agreement granted to you by GigaSpaces.
 *******************************************************************************/
package org.openspaces.core;

import org.springframework.dao.DataAccessException;

import com.gigaspaces.cluster.replication.ConsistencyLevel;

/**
 * Thrown when an operation is rejected since the {@link ConsistencyLevel} for that operation cannot be maintained.
 * {@link com.gigaspaces.cluster.replication.ConsistencyLevelViolationException}
 * @author eitany
 * @since 9.5.1
 */
public class ConsistencyLevelViolationException
        extends DataAccessException
{
    private static final long serialVersionUID = 1L;

    public ConsistencyLevelViolationException(com.gigaspaces.cluster.replication.ConsistencyLevelViolationException e)
    {
        super(e.getMessage(), e);
    }


}
