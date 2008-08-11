package org.openspaces.itest.remoting.broadcast;

import java.util.concurrent.Future;

/**
 * @author kimchy
 */
public interface SimpleService {

    int sum(int value);

    Future<Integer> asyncSum(int value);
}