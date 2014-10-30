package org.openspaces.itest.core.space.filter.storageTypes;

import org.junit.runner.RunWith;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * User: hagai
 * Date: 2/20/14
 */
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractStorageTypeSpaceFilterTest   { 
	protected final int NUMBER_OF_OBJECTS = 100;
	 @Autowired protected GigaSpace gigaSpace;


	protected abstract String[] getConfigLocations();
	/**
	 * empties the space.
	 */
	public void beforeTest(){
		gigaSpace.take(new Object());
	}
	/**
	 * writes 100 {@link ComplicatedMessage} to the space, updates them and reads back
	 */
	public abstract void testBasicSpaceOperations();

	/**
	 * Verifies that the filter's stats match the expected result and prints them.
	 */
	public abstract void verifyAndPrintFilterStats();
}

