package org.openspaces.itest.core.space.filter.storageTypes;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.metadata.StorageType;

/**
 * a wrapper for String, stored as {@link com.gigaspaces.metadata.StorageType#BINARY}
 */
@SpaceClass(storageType= StorageType.BINARY)
public class Message{

	private String _message;
	public Message(){}

	public Message(String message){_message = message;}

	public void setMessage(String message){_message = message;}
	public String getMessage(){return _message;}

}