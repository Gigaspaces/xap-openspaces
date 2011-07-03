package org.openspaces.core.gateway;

import java.io.IOException;
import java.net.ServerSocket;

public class GatewayUtils {
    /***
     * Checks is a post is available on the current machine, using new
     * ServerSocket(port).
     * 
     * @param port
     *            the port number.
     * @return true if available, false otherwise.
     */
    public static boolean checkPortAvailable(final int port) {
        if (port == 0)
            return true;
        ServerSocket sock = null;

        try {
            sock = new ServerSocket(port);
            sock.setReuseAddress(true);
            return true;
        } catch (final IOException e) {
            return false;
        } finally {
            if (sock != null) {
                try {
                    sock.close();
                } catch (final IOException e) {
                    // ignore
                }
            }
        }
    }       
}
