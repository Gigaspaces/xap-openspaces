/*******************************************************************************
 * 
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
 *  
 ******************************************************************************/
package org.openspaces.itest.core.space.filter;

import java.util.Map;

import org.openspaces.core.executor.Task;
import org.openspaces.itest.core.space.filter.security.SecurityFilter;

import com.j_spaces.core.filters.FilterOperationCodes;
import junit.framework.Assert;

public class AllOperationsFilterUtil {

    public static void restartStats(SimpleFilter[] filters) {
        for(SimpleFilter filter : filters){ 
            if(filter == null)
                continue;
            for(Integer key : filter.getStats().keySet())
                filter.getStats().put(key, null);
        }
    }   
    public static void restartStats(SecurityFilter[] filters) {
        for(SecurityFilter filter : filters){
            if(filter == null)
                continue;
            filter.getStats().clear();
        }
    }

    public static void initialAssert(Map<Integer,Integer> stats , String filterName) {
       
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_WRITE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_WRITE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_UPDATE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_UPDATE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_EXECUTE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_EXECUTE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_REMOVE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_REMOVE));        
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_AUTHENTICATION));
    }
    
    public static void assertAfterWrite(Map<Integer,Integer> stats , String filterName) {
          
            Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.BEFORE_WRITE));
            Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.AFTER_WRITE));
            Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.BEFORE_WRITE).intValue());
            Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.AFTER_WRITE).intValue());
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_CLEAN_SPACE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_UPDATE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_UPDATE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_EXECUTE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_EXECUTE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_REMOVE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_REMOVE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_AUTHENTICATION));
    }
    
    public static void assertAfterRead(Map<Integer,Integer> stats , String filterName) {
        
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_WRITE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_WRITE));
            Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ));
            Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.AFTER_READ));
            Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.BEFORE_READ).intValue());
            Assert.assertEquals(filterName , 1, stats.get(FilterOperationCodes.AFTER_READ).intValue());
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_UPDATE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_UPDATE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_EXECUTE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_EXECUTE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_REMOVE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_REMOVE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_AUTHENTICATION));
    }
    
    public static void assertAfterWriteWithAuthentication(Map<Integer,Integer> stats , String filterName) {
        
        Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.BEFORE_WRITE));
        Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.AFTER_WRITE));
        Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.BEFORE_WRITE).intValue());
        Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.AFTER_WRITE).intValue());
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_UPDATE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_UPDATE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_EXECUTE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_EXECUTE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ_MULTIPLE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_REMOVE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_REMOVE));
        Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.BEFORE_AUTHENTICATION));
        Assert.assertEquals(filterName , 1,stats.get(FilterOperationCodes.BEFORE_AUTHENTICATION).intValue());
    }
    
    public static void assertAfterReadWithAuthentication(Map<Integer,Integer> stats , String filterName) {
        
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_WRITE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_WRITE));
        Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ));
        Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.AFTER_READ));
        Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.BEFORE_READ).intValue());
        Assert.assertEquals(filterName , 1, stats.get(FilterOperationCodes.AFTER_READ).intValue());
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_UPDATE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_UPDATE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_EXECUTE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_EXECUTE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ_MULTIPLE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_REMOVE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_REMOVE));
        Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.BEFORE_AUTHENTICATION));
        Assert.assertEquals(filterName , 1,stats.get(FilterOperationCodes.BEFORE_AUTHENTICATION).intValue());
    }
    
    public static void assertAfterTakeWithAuthentication(Map<Integer,Integer> stats , String filterName) {
        
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_WRITE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_WRITE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ));
        Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE));
        Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE));
        Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.BEFORE_TAKE).intValue());
        Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.AFTER_TAKE).intValue());
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_UPDATE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_UPDATE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_EXECUTE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_EXECUTE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ_MULTIPLE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_AUTHENTICATION));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_REMOVE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_REMOVE)); 
        Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.BEFORE_AUTHENTICATION));
        Assert.assertEquals(filterName , 1,stats.get(FilterOperationCodes.BEFORE_AUTHENTICATION).intValue());
    }
    
    public static void assertAfterTakeMultipleWithAuthentication(Map<Integer, Integer> stats, String filterName) {
        
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_WRITE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_WRITE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_UPDATE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_UPDATE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_EXECUTE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_EXECUTE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ_MULTIPLE));
        Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
        Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
        Assert.assertEquals(filterName , 1, stats.get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE).intValue());
        Assert.assertEquals(filterName , 2, stats.get(FilterOperationCodes.AFTER_TAKE_MULTIPLE).intValue());
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
        Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.BEFORE_AUTHENTICATION));
        Assert.assertEquals(filterName , 1,stats.get(FilterOperationCodes.BEFORE_AUTHENTICATION).intValue());
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_REMOVE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_REMOVE)); 
        
    }
    
    public static void assertAfterExecuteWithAuthentication(Map<Integer, Integer> stats, String filterName) {
        
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_WRITE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_WRITE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_UPDATE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_UPDATE));
        Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.BEFORE_EXECUTE));
        Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.AFTER_EXECUTE));
        Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.BEFORE_EXECUTE).intValue());
        Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.AFTER_EXECUTE).intValue());
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ_MULTIPLE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_REMOVE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_REMOVE));
        Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.BEFORE_AUTHENTICATION));
        Assert.assertEquals(filterName , 1,stats.get(FilterOperationCodes.BEFORE_AUTHENTICATION).intValue());
        
    }
    
    public static void assertAfterUpdateWithAuthentication(Map<Integer, Integer> stats, String filterName) {
        
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_WRITE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_WRITE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY));
        Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.BEFORE_UPDATE));
        Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.AFTER_UPDATE));
        Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.BEFORE_UPDATE).intValue());
        Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.AFTER_UPDATE).intValue());
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_EXECUTE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_EXECUTE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ_MULTIPLE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_REMOVE));
        Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_REMOVE));
        Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.BEFORE_AUTHENTICATION));
        Assert.assertEquals(filterName , 1,stats.get(FilterOperationCodes.BEFORE_AUTHENTICATION).intValue());
        
    }

    public static void assertAfterUpdate(Map<Integer,Integer> stats , String filterName) {
         
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_WRITE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_WRITE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY));
            Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.BEFORE_UPDATE));
            Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.AFTER_UPDATE));
            Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.BEFORE_UPDATE).intValue());
            Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.AFTER_UPDATE).intValue());
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_EXECUTE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_EXECUTE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_REMOVE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_REMOVE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_AUTHENTICATION));
    }
    
    public static void assertAfterReadMultiple(Map<Integer,Integer> stats , String filterName) {
        
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_WRITE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_WRITE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_UPDATE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_UPDATE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_EXECUTE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_EXECUTE));
            Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
            Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.AFTER_READ_MULTIPLE));
            Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.BEFORE_READ_MULTIPLE).intValue());
            Assert.assertEquals(filterName , 2 , stats.get(FilterOperationCodes.AFTER_READ_MULTIPLE).intValue());
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_REMOVE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_REMOVE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_AUTHENTICATION));
    }

    public static void assertAfterTake(Map<Integer,Integer> stats , String filterName) {
          
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_WRITE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_WRITE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ));
            Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE));
            Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE));
            Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.BEFORE_TAKE).intValue());
            Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.AFTER_TAKE).intValue());
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_UPDATE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_UPDATE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_EXECUTE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_EXECUTE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_AUTHENTICATION));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_REMOVE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_REMOVE));       
    }
    
    public static void assertAfterTakeMultiple(Map<Integer,Integer> stats , String filterName) {
     
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_WRITE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_WRITE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_UPDATE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_UPDATE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_EXECUTE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_EXECUTE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ_MULTIPLE));
            Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
            Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
            Assert.assertEquals(filterName , 1, stats.get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE).intValue());
            Assert.assertEquals(filterName , 2, stats.get(FilterOperationCodes.AFTER_TAKE_MULTIPLE).intValue());
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_AUTHENTICATION));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_REMOVE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_REMOVE));             
    }
    
    public static void assertAfterExecute(Map<Integer,Integer> stats , String filterName) {
        
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_WRITE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_WRITE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_UPDATE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_UPDATE));
            Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.BEFORE_EXECUTE));
            Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.AFTER_EXECUTE));
            Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.BEFORE_EXECUTE).intValue());
            Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.AFTER_EXECUTE).intValue());
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_REMOVE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_REMOVE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_AUTHENTICATION));
    }

    
    public static void assertAfterNotify(Map<Integer,Integer> stats , String filterName) {
        
            Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.BEFORE_WRITE));
            Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.AFTER_WRITE));
            Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.BEFORE_WRITE).intValue());
            Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.AFTER_WRITE).intValue());
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE));
            Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.BEFORE_NOTIFY));
            Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.BEFORE_NOTIFY).intValue());
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_UPDATE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_UPDATE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_EXECUTE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_EXECUTE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_READ_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
            Assert.assertNotNull(filterName, stats.get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
            Assert.assertNotNull(filterName, stats.get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
            Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER).intValue());
            Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER).intValue());
            Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
            Assert.assertNotNull(filterName , stats.get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
            Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER).intValue());
            Assert.assertEquals(filterName , 1 , stats.get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER).intValue());
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_REMOVE));
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.AFTER_REMOVE));      
            Assert.assertNull(filterName , stats.get(FilterOperationCodes.BEFORE_AUTHENTICATION));
    }
    
    
    public static class MyTask implements Task<Integer> {
        
        private static final long serialVersionUID = 351353672928475600L;
        
        @Override
        public Integer execute() throws Exception {  
            return 1+1;
        }
    }


   

    

    
}
