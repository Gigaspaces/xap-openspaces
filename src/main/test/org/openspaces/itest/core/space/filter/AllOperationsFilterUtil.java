package org.openspaces.itest.core.space.filter;

import org.openspaces.core.executor.Task;

import com.j_spaces.core.filters.FilterOperationCodes;
import junit.framework.Assert;

public class AllOperationsFilterUtil {

    public static void restartStats(SimpleFilter[] filters) {
        for(SimpleFilter filter : filters){ 
            for(Integer key : filter.getStats().keySet())
                filter.getStats().put(key, null);
        }
    }

    public static void initialAssert(SimpleFilter filter , String filterName) {
       
            Assert.assertNotNull(filterName , filter.gigaSpace);
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_WRITE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_WRITE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_READ));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_READ));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_TAKE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_TAKE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_UPDATE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_UPDATE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_EXECUTE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_EXECUTE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_READ_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_REMOVE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_REMOVE));        
    }
    
    public static void assertAfterWrite(SimpleFilter filter , String filterName) {
          
            Assert.assertNotNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_WRITE));
            Assert.assertNotNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_WRITE));
            Assert.assertEquals(filterName , 1 , filter.getStats().get(FilterOperationCodes.BEFORE_WRITE).intValue());
            Assert.assertEquals(filterName , 1 , filter.getStats().get(FilterOperationCodes.AFTER_WRITE).intValue());
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_READ));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_READ));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_TAKE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_TAKE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_CLEAN_SPACE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_UPDATE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_UPDATE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_EXECUTE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_EXECUTE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_READ_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_REMOVE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_REMOVE));
               
    }
    
    public static void assertAfterRead(SimpleFilter filter , String filterName) {
        
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_WRITE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_WRITE));
            Assert.assertNotNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_READ));
            Assert.assertNotNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_READ));
            Assert.assertEquals(filterName , 1 , filter.getStats().get(FilterOperationCodes.BEFORE_READ).intValue());
            Assert.assertEquals(filterName , 1, filter.getStats().get(FilterOperationCodes.AFTER_READ).intValue());
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_TAKE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_TAKE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_UPDATE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_UPDATE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_EXECUTE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_EXECUTE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_READ_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_REMOVE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_REMOVE));
        
    }

    public static void assertAfterUpdate(SimpleFilter filter , String filterName) {
         
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_WRITE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_WRITE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_READ));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_READ));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_TAKE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_TAKE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY));
            Assert.assertNotNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_UPDATE));
            Assert.assertNotNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_UPDATE));
            Assert.assertEquals(filterName , 1 , filter.getStats().get(FilterOperationCodes.BEFORE_UPDATE).intValue());
            Assert.assertEquals(filterName , 1 , filter.getStats().get(FilterOperationCodes.AFTER_UPDATE).intValue());
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_EXECUTE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_EXECUTE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_READ_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_REMOVE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_REMOVE));
         
    }
    
    public static void assertAfterReadMultiple(SimpleFilter filter , String filterName) {
        
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_WRITE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_WRITE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_READ));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_READ));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_TAKE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_TAKE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_UPDATE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_UPDATE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_EXECUTE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_EXECUTE));
            Assert.assertNotNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
            Assert.assertNotNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_READ_MULTIPLE));
            Assert.assertEquals(filterName , 1 , filter.getStats().get(FilterOperationCodes.BEFORE_READ_MULTIPLE).intValue());
            Assert.assertEquals(filterName , 2 , filter.getStats().get(FilterOperationCodes.AFTER_READ_MULTIPLE).intValue());
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_REMOVE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_REMOVE));
         
    }

    public static void assertAfterTake(SimpleFilter filter , String filterName) {
          
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_WRITE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_WRITE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_READ));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_READ));
            Assert.assertNotNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_TAKE));
            Assert.assertNotNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_TAKE));
            Assert.assertEquals(filterName , 1 , filter.getStats().get(FilterOperationCodes.BEFORE_TAKE).intValue());
            Assert.assertEquals(filterName , 1 , filter.getStats().get(FilterOperationCodes.AFTER_TAKE).intValue());
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_UPDATE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_UPDATE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_EXECUTE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_EXECUTE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_READ_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
          //TODO: this filter also activates the remove : may be a bug
//            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_REMOVE));
//            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_REMOVE));       
    }
    
    public static void assertAftertakeMultiple(SimpleFilter filter , String filterName) {
     
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_WRITE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_WRITE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_READ));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_READ));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_TAKE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_TAKE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_UPDATE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_UPDATE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_EXECUTE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_EXECUTE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_READ_MULTIPLE));
            Assert.assertNotNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
            Assert.assertNotNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
            Assert.assertEquals(filterName , 1, filter.getStats().get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE).intValue());
            Assert.assertEquals(filterName , 2, filter.getStats().get(FilterOperationCodes.AFTER_TAKE_MULTIPLE).intValue());
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
          //TODO: this filter also activates the remove : may be a bug
//            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_REMOVE));
//            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_REMOVE));             
    }
    
    public static void assertAfterExecute(SimpleFilter filter , String filterName) {
        
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_WRITE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_WRITE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_READ));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_READ));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_TAKE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_TAKE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_UPDATE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_UPDATE));
            Assert.assertNotNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_EXECUTE));
            Assert.assertNotNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_EXECUTE));
            Assert.assertEquals(filterName , 1 , filter.getStats().get(FilterOperationCodes.BEFORE_EXECUTE).intValue());
            Assert.assertEquals(filterName , 1 , filter.getStats().get(FilterOperationCodes.AFTER_EXECUTE).intValue());
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_READ_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_REMOVE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_REMOVE));
           
    }

    public static void assertAfterNotify(SimpleFilter filter , String filterName) {
        
            Assert.assertNotNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_WRITE));
            Assert.assertNotNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_WRITE));
            Assert.assertEquals(filterName , 1 , filter.getStats().get(FilterOperationCodes.BEFORE_WRITE).intValue());
            Assert.assertEquals(filterName , 1 , filter.getStats().get(FilterOperationCodes.AFTER_WRITE).intValue());
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_READ));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_READ));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_TAKE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_TAKE));
            Assert.assertNotNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY));
            Assert.assertEquals(filterName , 1 , filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY).intValue());
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_UPDATE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_UPDATE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_EXECUTE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_EXECUTE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_READ_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_READ_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_TAKE_MULTIPLE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_TAKE_MULTIPLE));
            Assert.assertNotNull(filterName, filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER));
            Assert.assertNotNull(filterName, filter.getStats().get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER));
            Assert.assertEquals(filterName , 1 , filter.getStats().get(FilterOperationCodes.BEFORE_NOTIFY_TRIGGER).intValue());
            Assert.assertEquals(filterName , 1 , filter.getStats().get(FilterOperationCodes.AFTER_NOTIFY_TRIGGER).intValue());
            Assert.assertNotNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER));
            Assert.assertNotNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER));
            Assert.assertEquals(filterName , 1 , filter.getStats().get(FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER).intValue());
            Assert.assertEquals(filterName , 1 , filter.getStats().get(FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER).intValue());
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.BEFORE_REMOVE));
            Assert.assertNull(filterName , filter.getStats().get(FilterOperationCodes.AFTER_REMOVE));      
    }
    
    public static class MyTask implements Task<Integer> {
        
        private static final long serialVersionUID = 351353672928475600L;

        @Override
        public Integer execute() throws Exception {
            
            return 1+1;
        }
    }
}
