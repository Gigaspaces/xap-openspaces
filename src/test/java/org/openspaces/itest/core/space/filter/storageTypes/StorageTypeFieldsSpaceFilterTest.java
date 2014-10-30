package org.openspaces.itest.core.space.filter.storageTypes;
/**
 * Tests the ability of space-filters to work with complicated objects that contain Object,Binary and Compressed storage-types.<br/>
 * Uses the annotations-based filter.
 * @author hagai
 * Date: 2/19/14
 * Known issue GS-11642
 */
//public class StorageTypeFieldsSpaceFilterTest  extends AbstractStorageTypeSpaceFilterTest {

//	 @Autowired protected SimpleAnnotationsSpaceFilter simpleSpaceFilter;
//
//	@Override
//	protected String[] getConfigLocations() {
//		return new String[]{"/org/openspaces/itest/core/space/filter/storageTypes/space-annotation-filter.xml"};
//	}
//
//	public StorageTypeFieldsSpaceFilterTest() {
 
//	}
//
//	@Override
//	 @Test public void testBasicSpaceOperations(){
//		beforeTest();
//
//		//write objects to the space
//		for (int i = 0 ; i < NUMBER_OF_OBJECTS; i++){
//			ComplicatedMessage message = new ComplicatedMessage();
//			message.setId(i);
//			message.setMessage(new Message("Message " + i));
//			message.setUrgency(new Urgency("Urgency " + i));
//			byte[] bytes = "some string".getBytes();
//			message.setBytes(bytes);
//			HashMap<String,ComplicatedMessage> relatedMessgaes = new HashMap<String, ComplicatedMessage>();
//			for (int j = 0 ; j<10 ; j++){//add messages to 'ComplicatedMessage.relatedMessaged'.
//				ComplicatedMessage relatedMessage = new ComplicatedMessage();
//				relatedMessage.setMessage(new Message("Related " + j));
//				relatedMessgaes.put("" + j,relatedMessage);
//			}
//			message.setRelatedMessages(relatedMessgaes);
//			gigaSpace.write(message);
//		}
//
//		assertEquals("Expected the space to contain " + NUMBER_OF_OBJECTS + " objects, but the space contain " + gigaSpace.count(new ComplicatedMessage() + " objects.")
//				, NUMBER_OF_OBJECTS, gigaSpace.count(new ComplicatedMessage()));
//
//		//update the objects on the space
//		for (Integer i = 0 ; i< NUMBER_OF_OBJECTS; i++){
//			ComplicatedMessage message = new ComplicatedMessage();
//			message.setId(i);
//			message.setMessage(new Message("UPDATED Message " + i));
//			message.setUrgency(new Urgency("UPDATED Urgency " + i ));
//			byte[] bytes = "some string".getBytes();
//			message.setBytes(bytes);
//			HashMap<String,ComplicatedMessage> relatedMessgaes = new HashMap<String, ComplicatedMessage>();
//			for (int j = 0 ; j<10 ; j++){
//				ComplicatedMessage relatedMessage = new ComplicatedMessage();
//				relatedMessage.setMessage(new Message("Related " + j));
//				relatedMessgaes.put("" + j,relatedMessage);
//			}
//			message.setRelatedMessages(relatedMessgaes);
//			gigaSpace.write(message, WriteModifiers.UPDATE_ONLY);
//		}
//
//		//read the objects from the space
//		for (Integer i = 0 ; i< NUMBER_OF_OBJECTS; i++){
//			ComplicatedMessage message = gigaSpace.readById(ComplicatedMessage.class,i);
//			assertEquals("Expected message-ID to be " + i + " but got message-ID " + message.getId(),i , message.getId());
//
//			assertEquals("The message with ID " + i + " contained a Wrong Message",
//					message.getMessage().getMessage(),"UPDATED Message " + i );
//
//			assertEquals("The message with ID " + i + " contained a Wrong Urgency",
//					message.getUrgency().getUrgency(),"UPDATED Urgency " + i );
//		}
//		verifyAndPrintFilterStats();
//	}
//
//	/**
//	 * prints the filter's stats and verify that it matches the expected stats.
//	 */
//	@Override
//	public void verifyAndPrintFilterStats(){
//		System.out.println("-----------------------------------------------------------------------------------------");
//		System.out.println("--------------------------------- FILTER STATS ------------------------------------------");
//		System.out.println("Before write: " + simpleSpaceFilter.getStatsbeforeWrite());
//		System.out.println("After write: " + simpleSpaceFilter.getStatsAfterWrite());
//		System.out.println("Before update: " + simpleSpaceFilter.getStatsBeforeUpdate());
//		System.out.println("After update: " + simpleSpaceFilter.getStatsAfterUpdate());
//		System.out.println("Before read: " + simpleSpaceFilter.getStatsBeforeRead());
//		System.out.println("After read: " + simpleSpaceFilter.getStatsAfterRead());
//		System.out.println("-----------------------------------------------------------------------------------------");
//
//		assertEquals("Expected before-write stats to be " + NUMBER_OF_OBJECTS + ", but got " + simpleSpaceFilter.getStatsbeforeWrite() + " instead.",
//				simpleSpaceFilter.getStatsbeforeWrite().intValue(), NUMBER_OF_OBJECTS);
//
//		assertEquals("Expected after-write stats to be " + NUMBER_OF_OBJECTS + ", but got " + simpleSpaceFilter.getStatsAfterWrite() + " instead.",
//				simpleSpaceFilter.getStatsAfterWrite().intValue(), NUMBER_OF_OBJECTS);
//
//		assertEquals("Expected before-update stats to be " + NUMBER_OF_OBJECTS + ", but got " + simpleSpaceFilter.getStatsBeforeUpdate() + " instead.",
//				simpleSpaceFilter.getStatsBeforeUpdate().intValue(), NUMBER_OF_OBJECTS);
//
//		assertEquals("Expected after-update stats to be " + NUMBER_OF_OBJECTS + ", but got " + simpleSpaceFilter.getStatsAfterUpdate() + " instead.",
//				simpleSpaceFilter.getStatsAfterUpdate().intValue(), NUMBER_OF_OBJECTS);
//
//		assertEquals("Expected before-read stats to be " + (NUMBER_OF_OBJECTS+1) + ", but got " + simpleSpaceFilter.getStatsBeforeRead() + " instead.",//one extra object for the gigaspace.count() call
//				simpleSpaceFilter.getStatsBeforeRead().intValue(), (NUMBER_OF_OBJECTS+1));
//
//		assertEquals("Expected after-read stats to be " + NUMBER_OF_OBJECTS + ", but got " + simpleSpaceFilter.getStatsAfterRead() + " instead.",
//				simpleSpaceFilter.getStatsAfterRead().intValue(), NUMBER_OF_OBJECTS);
//	}

//}

