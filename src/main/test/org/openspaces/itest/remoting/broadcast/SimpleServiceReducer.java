package org.openspaces.itest.remoting.broadcast;

import org.openspaces.remoting.RemoteResultReducer;
import org.openspaces.remoting.SpaceRemotingInvocation;
import org.openspaces.remoting.SpaceRemotingResult;

/**
 * @author kimchy
 */
public class SimpleServiceReducer implements RemoteResultReducer<Integer, Integer> {

    public Integer reduce(SpaceRemotingResult<Integer>[] spaceRemotingResults, SpaceRemotingInvocation remotingInvocation) throws Exception {
        int sum = 0;
        for (SpaceRemotingResult<Integer> result : spaceRemotingResults) {
            if (result.getException() != null) {
                throw (Exception) result.getException();
            }
            sum += result.getResult();
        }
        return sum;
    }
}