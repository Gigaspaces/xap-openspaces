package org.openspaces.itest.core.space.filter.storageTypes;

import com.gigaspaces.client.WriteModifiers;
import com.gigaspaces.metadata.StorageType;
import com.j_spaces.core.filters.FilterOperationCodes;
import com.j_spaces.core.filters.ISpaceFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 *
 * Tests {@link ISpaceFilter} while using complicated objects that contain {@link StorageType#OBJECT}, {@link StorageType#COMPRESSED} and {@link StorageType#BINARY} fields.
 * Writes {@link #NUMBER_OF_OBJECTS} objects to the space, updates them and reads them.
 * User: hagai
 * Date: 2/20/14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/core/space/filter/storageTypes/space-ISpace-filter.xml")
public class StorageTypeISpaceFilterTest  extends AbstractStorageTypeSpaceFilterTest { 

	 @Autowired protected SimpleFilter simpleSpaceFilter;

	//@Override
	protected String[] getConfigLocations () {
		return new String[]{"/org/openspaces/itest/core/space/filter/storageTypes/space-ISpace-filter.xml"};
	}

	public StorageTypeISpaceFilterTest() {
 
	}

	@Override
	 @Test public void testBasicSpaceOperations(){
		beforeTest();

		//write objects to the space
		for (int i = 0 ; i < NUMBER_OF_OBJECTS; i++){
			ComplicatedMessage message = new ComplicatedMessage();
			message.setId(i);
			message.setMessage(new Message("Message " + i));
			message.setUrgency(new Urgency("Urgency " + i));
			byte[] bytes = "some string".getBytes();
			message.setBytes(bytes);
			HashMap<String,ComplicatedMessage> relatedMessgaes = new HashMap<String, ComplicatedMessage>();
			for (int j = 0 ; j<10 ; j++){
				ComplicatedMessage relatedMessage = new ComplicatedMessage();
				relatedMessage.setMessage(new Message("Related " + j));
				relatedMessgaes.put("" + j,relatedMessage);
			}
			message.setRelatedMessages(relatedMessgaes);
			gigaSpace.write(message);
		}

		assertEquals("Expected the space to contain " + NUMBER_OF_OBJECTS + " objects, but the space contain " + gigaSpace.count(new ComplicatedMessage() + " objects.")
				, NUMBER_OF_OBJECTS, gigaSpace.count(new ComplicatedMessage()));

		//update the objects on the space
		for (Integer i = 0 ; i< NUMBER_OF_OBJECTS; i++){
			ComplicatedMessage message = new ComplicatedMessage();
			message.setId(i);
			message.setMessage(new Message("UPDATED Message " + i));
			message.setUrgency(new Urgency("UPDATED Urgency " + i ));
			byte[] bytes = "some string".getBytes();
			message.setBytes(bytes);
			HashMap<String,ComplicatedMessage> relatedMessgaes = new HashMap<String, ComplicatedMessage>();
			for (int j = 0 ; j<10 ; j++){
				ComplicatedMessage relatedMessage = new ComplicatedMessage();
				relatedMessage.setMessage(new Message("Related " + j));
				relatedMessgaes.put("" + j,relatedMessage);
			}
			message.setRelatedMessages(relatedMessgaes);
			gigaSpace.write(message, WriteModifiers.UPDATE_ONLY);
		}

		//read the objects from the space
		for (Integer i = 0 ; i< NUMBER_OF_OBJECTS; i++){
			ComplicatedMessage message = gigaSpace.readById(ComplicatedMessage.class,i);
			assertEquals("Expected message-ID to be " + i + " but got message-ID " + message.getId(),i , message.getId());

			assertEquals("The message with ID " + i + " contained a Wrong Message",
					message.getMessage().getMessage(),"UPDATED Message " + i );

			assertEquals("The message with ID " + i + " contained a Wrong Urgency",
					message.getUrgency().getUrgency(),"UPDATED Urgency " + i );
		}
		verifyAndPrintFilterStats();
	}

	/**
	 * prints the filter's stats and verify that it matches the expected stats.
	 */
	@Override
	public void verifyAndPrintFilterStats(){
		Map<Integer, Integer> stats = simpleSpaceFilter.getStats();
		System.out.println("-----------------------------------------------------------------------------------------");
		System.out.println("--------------------------------- FILTER STATS ------------------------------------------");
		for (Integer key: stats.keySet()){
			System.out.println("Key: " + key + ", Value: " + stats.get(key));
		}
		System.out.println("NOTE: key '" + FilterOperationCodes.AFTER_WRITE + "' is for AFTER_WRITE op, '" +
				FilterOperationCodes.AFTER_UPDATE + "' is for AFTER_UPDATE op, '" +
				FilterOperationCodes.AFTER_READ + "' is for AFTER_READ op." );
		System.out.println("-----------------------------------------------------------------------------------------");

		assertEquals("Expected " + NUMBER_OF_OBJECTS + " \"AFTER-WRITE\"s, but got " + stats.get(FilterOperationCodes.AFTER_WRITE) +".",
				NUMBER_OF_OBJECTS,stats.get(FilterOperationCodes.AFTER_WRITE).intValue());

		assertEquals("Expected " + NUMBER_OF_OBJECTS + " \"AFTER-UPDATE\"s, but got " + stats.get(FilterOperationCodes.AFTER_UPDATE) +".",
				NUMBER_OF_OBJECTS,stats.get(FilterOperationCodes.AFTER_UPDATE).intValue());

		assertEquals("Expected " + NUMBER_OF_OBJECTS + " \"AFTER-READ\"s, but got " + stats.get(FilterOperationCodes.AFTER_READ) +".",
				NUMBER_OF_OBJECTS,stats.get(FilterOperationCodes.AFTER_READ).intValue());

		assertEquals("Expected the stats HashMap to contain 3 keys, instead it contains " + stats.size() + " keys.",
				stats.size(),3);
	}

}

