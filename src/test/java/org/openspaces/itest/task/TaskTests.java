package org.openspaces.itest.task;

import com.gigaspaces.async.AsyncFuture;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/task/context.xml")
public class TaskTests {

    @Autowired
    ApplicationContext applicationContext;

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/task/context.xml"};
    }

    @Test
    public void testSimpleTaskExecution() throws Exception {
        GigaSpace gigaSpace = (GigaSpace) applicationContext.getBean("clusteredGigaSpace");
        AsyncFuture<Long> future = gigaSpace.execute(new MyTask());
        long result = future.get();
        Assert.assertEquals(1, result);
    }
}
