package org.openspaces.itest.core.space.filter.storageTypes;

import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceStorageType;
import com.gigaspaces.metadata.StorageType;

import javax.persistence.ElementCollection;
import java.util.HashMap;

/**
 * This complicated Message encapsulates<br></br>
 * a regular 'OBJECT'-storage-type Integer (_id) <br></br>
 * a 'BINARY'-storage-type Message (_message)<br></br>
 * a 'COMPRESSED'-storage-type Urgency(_urgency).
 *
 * @author hagai
 * Date: 2/19/14
 *
 */
public class ComplicatedMessage {

	private HashMap<String,ComplicatedMessage> _relatedMessages;
	private Integer _id;// storage-type 'OBJECT'
	private Message _message;// storage-type 'BINARY'
	private Urgency _urgency;// storage-type 'COMPRESSED'
	private byte[] _bytes;// to check the space with an array

	/**
	 * Default  empty constructor
	 */
	public ComplicatedMessage(){
	}

	@SpaceId
	public Integer getId() {
		return _id;
	}

	public void setId(Integer id) {
		_id = id;
	}

	public Message getMessage(){
		return _message;
	}

	public void setMessage(Message message){
		_message = message;
	}

	public Urgency getUrgency(){
		return _urgency;
	}

	public void setUrgency(Urgency urgency){
		_urgency = urgency;
	}

	@ElementCollection
	public HashMap<String,ComplicatedMessage> getRelatedMessages(){
		return _relatedMessages;
	}

	public void setRelatedMessages(HashMap<String,ComplicatedMessage> relatedMessages){
		_relatedMessages = relatedMessages;
	}

	@SpaceStorageType(storageType = StorageType.BINARY)
	public byte[] getBytes() {
		return _bytes;
	}

	public void setBytes(byte[] bytes) {
		_bytes = bytes;
	}

	public String toString(){
		String res = new String();
		res = res + "id: " + _id + "\n";
		res = res + "message: ";
		if (_message!=null){
			res = res + _message.getMessage();
		}
		res = res + "\n";

		res = res + "urgency: ";
		if(_urgency!=null){
			res = res + _urgency.getUrgency();
		}
		res = res + "\n";
		if(_relatedMessages!=null){
			res = res + "related: ";
			for (String key:_relatedMessages.keySet()){
				res = res + _relatedMessages.get(key).getMessage() + ", ";
			}
		}
		return res;
	}


}
