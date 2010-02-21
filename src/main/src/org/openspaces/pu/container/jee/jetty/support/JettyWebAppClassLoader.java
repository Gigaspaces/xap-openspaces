package org.openspaces.pu.container.jee.jetty.support;

import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jini.rio.boot.LoggableClassLoader;

import java.io.IOException;

/**
 * A simple extension for Jetty WebApp class loader just to make it loggable in the Service Grid.
 *
 * @author kimchy
 */
public class JettyWebAppClassLoader extends WebAppClassLoader implements LoggableClassLoader {

    private final String loggableName;

    public JettyWebAppClassLoader(ClassLoader parent, WebAppContext context, String loggableName) throws IOException {
        super(parent, context);
        this.loggableName = loggableName;
    }

    public String getLogName() {
        return loggableName; 
    }
}
