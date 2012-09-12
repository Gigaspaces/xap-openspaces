package org.openspaces.core.util;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import org.jini.rio.boot.ServiceClassLoader;

public class ClassLoaderUtils {
	
	public static String getClassPathString(ClassLoader classLoader) {
        StringBuilder classpath = new StringBuilder();
        for (;classLoader != null; classLoader=classLoader.getParent()) {
            classpath.append("[classloader "+classLoader);
            if (classLoader instanceof URLClassLoader) {
                final URLClassLoader ucl = (URLClassLoader)classLoader;
                classpath.append(" URL=")
                         .append(getURLsString(ucl.getURLs()));
            }
            if (classLoader instanceof ServiceClassLoader) {
                final ServiceClassLoader scl = (ServiceClassLoader) classLoader;
                classpath.append(" searchPath=")
                         .append(getURLsString(scl.getSearchPath()))
                         .append(" slashPath=")
                         .append(getURLsString(scl.getSlashPath()))
                         .append(" libPath=")
                         .append(getURLsString(scl.getLibPath()));
            }
            classpath.append("]");
        }
        return classpath.toString();
    }
    
    private static String getURLsString(URL url) {
        return getURLsString(new URL[] { url });
    }
    
    private static String getURLsString(URL[] urls) {
        StringBuilder classpath = new StringBuilder();
        for (final URL url : urls) {
            final String file = url.getFile();
            classpath.append(file);
            if (!new File(file).exists()) {
                classpath.append("(not exists)");
            }
            classpath.append(",");
        }
        return classpath.toString();
    }
    
    public static boolean isClassLoaderProblem(Throwable t) {
        boolean classLoaderProblem = false;
        for (; !classLoaderProblem && t != null; t=t.getCause())  {
            if (t instanceof NoClassDefFoundError) {
                classLoaderProblem = true;
            }
        }
        return classLoaderProblem;
    }

}
