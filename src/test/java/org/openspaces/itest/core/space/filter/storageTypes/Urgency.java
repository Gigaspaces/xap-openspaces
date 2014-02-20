package org.openspaces.itest.core.space.filter.storageTypes;


import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.metadata.StorageType;

/**
 * a wrapper for String, stored as {@link com.gigaspaces.metadata.StorageType#COMPRESSED}
 */
@SpaceClass(storageType = StorageType.COMPRESSED)
public class Urgency{
	private String _urgency;
	public Urgency(){}
	public Urgency(String urgency){_urgency = urgency;}
	public void setUrgency(String urgency){_urgency = urgency;}
	public String getUrgency(){return _urgency;}
}