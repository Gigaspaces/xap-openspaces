package org.openspaces.itest.task;

import com.gigaspaces.async.AsyncResult;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.executor.DistributedTask;
import org.openspaces.core.executor.TaskGigaSpace;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;

public class MyTask implements DistributedTask<Integer, Long>, ApplicationContextAware {

    @TaskGigaSpace
    private transient GigaSpace gigaSpace;
    private transient GigaSpace clusteredSpace;

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        clusteredSpace = (GigaSpace) applicationContext.getBean("clusteredGigaSpace");
        System.out.println("clusteredSpace is " + clusteredSpace == null ? "null" : "not null");
    }

    @Override
    public Integer execute() throws Exception {
        return clusteredSpace == null ? 0 : 1;
    }

    @Override
    public Long reduce(List<AsyncResult<Integer>> asyncResults) throws Exception {
        long result = 0;
        for (AsyncResult<Integer> asyncResult : asyncResults)
            result += asyncResult.getResult();
        return result;
    }
}
