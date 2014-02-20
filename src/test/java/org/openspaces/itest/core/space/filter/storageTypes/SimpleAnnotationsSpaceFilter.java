package org.openspaces.itest.core.space.filter.storageTypes;
import junit.framework.Assert;
import org.openspaces.core.space.filter.*;

import java.util.HashMap;

/**
 *
 * This filter is used by {@link StorageTypeFieldsSpaceFilterTest}
 * @author : hagai
 * Date: 2/19/14
 */
public class SimpleAnnotationsSpaceFilter {
	private static final String BEFORE_WRITE_KEY = "BeforeWrite";
	private static final String AFTER_WRITE_KEY = "AfterWrite";
	private static final String BEFORE_UPDATE_KEY = "BeforeUpdate";
	private static final String AFTER_UPDATE_KEY = "AfterUpdate";
	private static final String BEFORE_READ_KEY = "BeforeRead";
	private static final String AFTER_READ_KEY = "AfterRead";
	protected HashMap<String,Integer> _stats;

	public SimpleAnnotationsSpaceFilter(){
		_stats = new HashMap<String,Integer>();
	}
	@BeforeWrite
	public void beforeWrite(ComplicatedMessage entry) {
		Integer currentValue = _stats.get(BEFORE_WRITE_KEY);
		if(currentValue==null){
			currentValue=0;
		}
		_stats.put(BEFORE_WRITE_KEY,currentValue + 1);
		assertMessageIsOfRightForm(entry);
	}


	@AfterWrite
	public void afterWrite(ComplicatedMessage entry) {
		Integer currentValue = _stats.get(AFTER_WRITE_KEY);
		if(currentValue==null){
			currentValue=0;
		}
		_stats.put(AFTER_WRITE_KEY,currentValue + 1);
		assertMessageIsOfRightForm(entry);
	}

	@BeforeUpdate
	public void beforeUpdate(ComplicatedMessage entry) {
		Integer currentValue = _stats.get(BEFORE_UPDATE_KEY);
		if(currentValue==null){
			currentValue=0;
		}
		_stats.put(BEFORE_UPDATE_KEY,currentValue + 1);

		assertMessageIsOfRightForm(entry);
	}

	@AfterUpdate
	public void afterUpdate(ComplicatedMessage beforeUpdate, ComplicatedMessage afterUpdate) {
		Integer currentValue = _stats.get(AFTER_UPDATE_KEY);
		if(currentValue==null){
			currentValue=0;
		}
		_stats.put(AFTER_UPDATE_KEY,currentValue + 1);

		assertMessageIsOfRightForm(beforeUpdate);
		assertMessageIsOfRightForm(afterUpdate);
	}

	@BeforeRead
	public void beforeRead(ComplicatedMessage entry) {
		Integer currentValue = _stats.get(BEFORE_READ_KEY);
		if(currentValue==null){
			currentValue=0;
		}
		_stats.put(BEFORE_READ_KEY,currentValue + 1);
		/*
		there is no need to access the parameter ComplicatedMessage, as it is only
		a template with which the user asked to read, thus nothing is tested here.
		 */
	}

	@AfterRead
	public void afterRead(ComplicatedMessage entry) {
		Integer currentValue = _stats.get(AFTER_READ_KEY);
		if(currentValue==null){
			currentValue=0;
		}
		_stats.put(AFTER_READ_KEY,currentValue + 1);

		assertMessageIsOfRightForm(entry);
	}

	public Integer getStatsbeforeWrite(){
		return _stats.get(BEFORE_WRITE_KEY);
	}

	public Integer getStatsAfterWrite(){
		return _stats.get(AFTER_WRITE_KEY);
	}

	public Integer getStatsBeforeUpdate(){
		return _stats.get(BEFORE_UPDATE_KEY);
	}

	public Integer getStatsAfterUpdate(){
		return _stats.get(AFTER_UPDATE_KEY);
	}

	public Integer getStatsBeforeRead() {
		return _stats.get(BEFORE_READ_KEY);
	}

	public Integer getStatsAfterRead() {
		return _stats.get(AFTER_READ_KEY);
	}

	private void assertMessageIsOfRightForm(ComplicatedMessage message) {
		Assert.assertTrue(message.getId() >= -1);

		Assert.assertTrue("message.getMessage().getMessage() didn't contain the string \"Message\" as expected, message.getMessage().getMessage()=" +
				message.getMessage().getMessage() , message.getMessage().getMessage().indexOf("Message") != -1);

		Assert.assertTrue("message.getUrgency().getUrgency() didn't contain the string \"Urgency\" as expected, message.getUrgency().getUrgency()=" +
				message.getUrgency().getUrgency() , message.getUrgency().getUrgency().indexOf("Urgency") != -1);

		Assert.assertTrue("Expected message.getBytes().length, got " + message.getBytes().length + " instead.",
				message.getBytes().length>0);
		Assert.assertTrue("Expected message.getRelatedMessages().size()>0, got message.getRelatedMessages().size()=" + message.getRelatedMessages().size()
				,message.getRelatedMessages().size()>0);

		HashMap<String,ComplicatedMessage> relatedMessages= message.getRelatedMessages();
		for(String key: relatedMessages.keySet()){
			Assert.assertTrue("a message in the '_relatedMessages' map didn't contain the string \"Related\" as expected.",
					relatedMessages.get(key).getMessage().getMessage().indexOf("Related") != -1);
		}

	}


	private void printComplicatedMesaage(ComplicatedMessage entry) {
		try {
		System.out.println(entry.getId() + ", " + entry.getMessage().getMessage() + ", " + entry.getUrgency().getUrgency());
		}
		catch (NullPointerException e) {
			System.out.println("*******************************************************");
			System.out.println("* NOTE: the message or one of it's components is null *");
			System.out.println("*******************************************************");
		}
		HashMap<String,ComplicatedMessage> relatedMessages = entry.getRelatedMessages();
		if(relatedMessages!=null){
			System.out.println("related messgaes: ");
			for (String key: relatedMessages.keySet()){
				System.out.print(relatedMessages.get(key).getMessage().getMessage() + ",");
			}
			System.out.println();
		}
	}

}
