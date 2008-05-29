package org.openspaces.itest.remoting.simple;

import org.openspaces.remoting.RemotingService;

import java.util.concurrent.Future;

/**
 * @author kimchy
 */
@RemotingService
public class DefaultSimpleService implements SimpleService {

    public String say(String message) {
        return "SAY " + message;
    }

    public Future asyncSay(String message) {
        return null;
    }
}
