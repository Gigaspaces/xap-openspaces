/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.itest.core.space.filter;

import org.openspaces.core.GigaSpace;
import org.openspaces.itest.core.space.filter.adapter.Message;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import com.j_spaces.core.LeaseContext;
import com.j_spaces.core.client.UpdateModifiers;
import com.j_spaces.core.filters.FilterOperationCodes;

/**
 * @author kimchy
 */
public class allFilterTests extends AbstractDependencyInjectionSpringContextTests {

    protected SimpleFilter simpleFilterCode;
    protected SimpleFilter simpleFilterCodeName;

    protected GigaSpace gigaSpace;

    public allFilterTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/core/space/filter/all-filter.xml"};
    }
    

 
    public void testFilter() {
        Message message = new Message(1);
        SimpleFilter[] filters = new SimpleFilter[2];
        filters[0] = simpleFilterCode;
        filters[1] = simpleFilterCodeName;
        
        initialAssert(filters); 
        LeaseContext<Message> lease = gigaSpace.write(message);
        assertNotNull(lease);
        assertAfterWrite(filters);
        
        Message readMessage = gigaSpace.read(message);
        assertNotNull(readMessage);
        assertAfterRead(filters);
        
        message.setMessage("message");
        lease = gigaSpace.write(message, 1000 * 20, 0, UpdateModifiers.UPDATE_ONLY);
        assertNotNull(lease);
        assertAfterUpdate(filters);
        
        Message[] messages = gigaSpace.readMultiple(message , 1);
        assertNotSame(new Message[0], messages);
        assertEquals(1, messages.length);
        assertAfterReadMultiple(filters);
       
        Message takenMessage = gigaSpace.take(message);
        assertNotNull(takenMessage);
        assertAfterTake(filters);
    }

    private void initialAssert(SimpleFilter[] filters) {
        for(SimpleFilter filter : filters){   
            assertNotNull(filter.gigaSpace);
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_WRITE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_WRITE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_READ));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_READ));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_TAKE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_TAKE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_CLEAN_SPACE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_UPDATE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_UPDATE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_EXECUTE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_EXECUTE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_READ_MULTIPLE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_REMOVE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_REMOVE));
        }
    }
    
    private void assertAfterWrite(SimpleFilter[] filters) {
        for(SimpleFilter filter : filters){  
            assertEquals("",1 , filter.getStats().get(FilterOperationCodes.BEFORE_WRITE).intValue());
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.AFTER_WRITE).intValue());
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_READ));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_READ));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_TAKE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_TAKE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_CLEAN_SPACE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_UPDATE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_UPDATE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_EXECUTE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_EXECUTE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_READ_MULTIPLE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_REMOVE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_REMOVE));
        }        
    }
    
    private void assertAfterRead(SimpleFilter[] filters) {
        for(SimpleFilter filter : filters){  
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.BEFORE_WRITE).intValue());
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.AFTER_WRITE).intValue());
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.BEFORE_READ).intValue());
            assertEquals(1, filter.getStats().get(FilterOperationCodes.AFTER_READ).intValue());
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_TAKE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_TAKE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_CLEAN_SPACE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_UPDATE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_UPDATE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_EXECUTE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_EXECUTE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_READ_MULTIPLE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_REMOVE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_REMOVE));
        }
    }

    private void assertAfterUpdate(SimpleFilter[] filters) {
        for(SimpleFilter filter : filters){  
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.BEFORE_WRITE).intValue());
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.AFTER_WRITE).intValue());
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.BEFORE_READ).intValue());
            assertEquals(1, filter.getStats().get(FilterOperationCodes.AFTER_READ).intValue());
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_TAKE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_TAKE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_CLEAN_SPACE));
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.BEFORE_UPDATE).intValue());
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.AFTER_UPDATE).intValue());
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_EXECUTE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_EXECUTE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_READ_MULTIPLE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_REMOVE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_REMOVE));
        } 
    }
    
    private void assertAfterReadMultiple(SimpleFilter[] filters) {
        for(SimpleFilter filter : filters){
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.BEFORE_WRITE).intValue());
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.AFTER_WRITE).intValue());
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.BEFORE_READ).intValue());
            assertEquals(1, filter.getStats().get(FilterOperationCodes.AFTER_READ).intValue());
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_TAKE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_TAKE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_CLEAN_SPACE));
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.BEFORE_UPDATE).intValue());
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.AFTER_UPDATE).intValue());
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_EXECUTE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_EXECUTE));
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.BEFORE_READ_MULTIPLE).intValue());
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.AFTER_READ_MULTIPLE).intValue());
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_REMOVE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_REMOVE));
        } 
    }

    private void assertAfterTake(SimpleFilter[] filters) {
        for(SimpleFilter filter : filters){  
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.BEFORE_WRITE).intValue());
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.AFTER_WRITE).intValue());
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.BEFORE_READ).intValue());
            assertEquals(1, filter.getStats().get(FilterOperationCodes.AFTER_READ).intValue());
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.BEFORE_TAKE).intValue());
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.AFTER_TAKE).intValue());
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_CLEAN_SPACE));
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.BEFORE_UPDATE).intValue());
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.AFTER_UPDATE).intValue());
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_EXECUTE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_EXECUTE));
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.BEFORE_READ_MULTIPLE).intValue());
            assertEquals(1 , filter.getStats().get(FilterOperationCodes.AFTER_READ_MULTIPLE).intValue());
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
            assertNull(filter.getStats().get(FilterOperationCodes.BEFORE_REMOVE));
            assertNull(filter.getStats().get(FilterOperationCodes.AFTER_REMOVE));
        }
    }

   
   
}
