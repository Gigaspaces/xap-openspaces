/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.openspaces.persistency.cassandra.archive;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.openspaces.archive.ArchiveOperationHandler;
import org.openspaces.core.GigaSpace;
import org.openspaces.persistency.cassandra.HectorCassandraClient;
import org.openspaces.persistency.cassandra.error.SpaceCassandraException;
import org.openspaces.persistency.cassandra.meta.ColumnFamilyMetadata;
import org.openspaces.persistency.cassandra.meta.conversion.ColumnFamilyNameConverter;
import org.openspaces.persistency.cassandra.meta.data.ColumnFamilyRow;
import org.openspaces.persistency.cassandra.meta.data.ColumnFamilyRow.ColumnFamilyRowType;
import org.openspaces.persistency.cassandra.meta.mapping.DefaultSpaceDocumentColumnFamilyMapper;
import org.openspaces.persistency.cassandra.meta.mapping.filter.FlattenedPropertiesFilter;
import org.openspaces.persistency.cassandra.meta.types.dynamic.PropertyValueSerializer;
import org.springframework.beans.factory.annotation.Required;

import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;

/**
 * 
 * Known Limitation:
 *  1. The archiver must not write two different entries with the same ID.
 *     This would corrupt the entry in Cassandra.
 *  2. Only Space Documents are supported
 *  3. The archiver is thread safe
 *  4. The archiver is idempotent as long as there are no two threads that are writing two different objects with the same space id.
 *  
 * @author Dan Kilman
 * @since 9.1.1
 */
public class CassandraArchiveOperationHandler implements ArchiveOperationHandler {

    private static final int DEFAULT_MAX_NESTING_LEVEL = 10;

	private static final int DEFAULT_CASSANDRA_PORT = 9160;
    
    //injected (required)
    private GigaSpace gigaSpace;
    
    //injected (overrides default value)
    private Integer maxNestingLevel;
    private PropertyValueSerializer fixedPropertyValueSerializer;
    private FlattenedPropertiesFilter flattenedPropertiesFilter;
    private ColumnFamilyNameConverter columnFamilyNameConverter;
    private String host;
    private Integer port;
    private String keyspace;
    
    //lifecycle objects
    private HectorCassandraClient hectorClient;
    private DefaultSpaceDocumentColumnFamilyMapper mapper;


    @Required
    public void setGigaSpace(GigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
    }

    public void setMaxNestingLevel(Integer maxNestingLevel) {
        this.maxNestingLevel = maxNestingLevel;
    }

    @Required
	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	@Required
	public void setKeyspace(String keyspace) {
		this.keyspace = keyspace;
	}
	
    @PostConstruct
    public void afterPropertiesSet() {
        
        if (gigaSpace == null) {
            throw new IllegalArgumentException("gigaSpace cannot be null");
        }
        
        createMapper();
        createHectorClient();
        
    }

	private void createHectorClient() {
		if (host == null) {
			throw new IllegalArgumentException("Cassandra host name cannot be null");
		}
		if (port == null) {
			port = DEFAULT_CASSANDRA_PORT;
		}
		if (port <= 0) {
			throw new IllegalArgumentException("Cassnadra port must be a positive integer");
		}
		if (keyspace == null) {
			throw new IllegalArgumentException("Cassandra keyspace cannot be null");
		}
		
		final String hectorClusterName = host+":"+port;
        hectorClient = new HectorCassandraClient(host, port, keyspace, hectorClusterName);
	}

	private void createMapper() {
		if (maxNestingLevel == null) {
            //default value
            maxNestingLevel = DEFAULT_MAX_NESTING_LEVEL;
        }
        
        final PropertyValueSerializer dynamicPropertyValueSerializer = null;
        
        mapper = new DefaultSpaceDocumentColumnFamilyMapper(
        		fixedPropertyValueSerializer, // can be null
        		dynamicPropertyValueSerializer, //not used, can be null                                         
                flattenedPropertiesFilter, // can be null
                columnFamilyNameConverter, // can be null
                maxNestingLevel);
	}
    
    @PreDestroy
    public void destroy() {
    	if (hectorClient != null) {
    		hectorClient.close();
    	}
    }
    
    /**
     * @see ArchiveOperationHandler#archive(Object...)
     * 
     *  @throws SpaceCassandraException - Problem encountered while archiving to cassandra
     */
    @Override
    public void archive(Object... objects) {
        List<ColumnFamilyRow> rows = new LinkedList<ColumnFamilyRow>();
        
        for (Object object : objects) {

            if (!(object instanceof SpaceDocument)) {
            	throw new SpaceCassandraArchiveOperationHandlerException(object.getClass() + " is not supported since it is not a " + SpaceDocument.class.getName());
            }
            
            SpaceDocument spaceDoc = (SpaceDocument) object;
            String typeName = spaceDoc.getTypeName();
            ColumnFamilyMetadata metadata = hectorClient.getColumnFamilyMetadata(typeName);
            if (metadata == null) {
                metadata = createColumnFamilyMetadata(typeName);
                //thread safe call
                hectorClient.createColumnFamilyIfNecessary(metadata, false /* persist metadata */);
            }

            String keyName = metadata.getKeyName();
            Object keyValue = spaceDoc.getProperty(keyName);
                
            if (keyValue == null) {
            	throw new SpaceCassandraArchiveOperationHandlerException(object.getClass() + " entry is illegal since SpaceId property is undefined");
            }
            ColumnFamilyRow columnFamilyRow;
        
            columnFamilyRow = 
                    mapper.toColumnFamilyRow(metadata, 
                                             spaceDoc, 
                                             ColumnFamilyRowType.Write,
                                             false);
            rows.add(columnFamilyRow);
        }

        hectorClient.performBatchOperation(rows);
    }

    private ColumnFamilyMetadata createColumnFamilyMetadata(String typeName)
    {
        SpaceTypeDescriptor typeDesc = gigaSpace.getTypeManager().getTypeDescriptor(typeName);
        String keyName = typeDesc.getIdPropertyName();
        Class<?> keyType = typeDesc.getFixedProperty(keyName).getType();
        SpaceTypeDescriptor dynamicTypeDesc = new SpaceTypeDescriptorBuilder(typeName)
            .addFixedProperty(keyName, keyType)
            .idProperty(keyName)
            // TODO CAS: handle column families with no columns
            .addFixedProperty("stub", Object.class)
            .create();
        return mapper.toColumnFamilyMetadata(dynamicTypeDesc);
    }
    
    /**
     * @see ArchiveOperationHandler#supportsBatchArchiving() 
     * @return true - 
     *         Since Multiple archiving of the exact same objects is supported (idempotent).
     */
    @Override
    public boolean supportsBatchArchiving() {
        return true;
    }
}
