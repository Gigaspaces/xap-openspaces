package org.openspaces.itest.core.space.filter.storageTypes;

import org.openspaces.core.GigaSpace;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * User: hagai
 * Date: 2/20/14
 */
public abstract class AbstractStorageTypeSpaceFilterTest extends AbstractDependencyInjectionSpringContextTests {
	protected final int NUMBER_OF_OBJECTS = 100;
	protected GigaSpace gigaSpace;


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
