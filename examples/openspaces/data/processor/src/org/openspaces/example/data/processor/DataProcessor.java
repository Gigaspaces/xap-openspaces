package org.openspaces.example.data.processor;

import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.example.data.common.Data;

/**
 * @author kimchy
 */
public class DataProcessor {

    private long workDuration = 1000;


    /**
     * Sets the simulated work duration (in milliseconds). Defaut to 1000 (1 second).
     */
    public void setWorkDuration(long workDuration) {
        this.workDuration = workDuration;
    }

    @SpaceDataEvent
    public Data processData(Data data) {
        try {
            Thread.sleep(workDuration);
        } catch (InterruptedException e) {
            // do nothing
        }
        data.setProcessed(true);
        data.setData("PROCESSED : " + data.getRawData());
        System.out.println(" ------ PROCESSED : " + data);
        // sleep to simluate some work
        // reset the id as we use auto generate true
        data.setId(null);
        return data;
    }
}
