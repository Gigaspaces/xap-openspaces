package org.openspaces.utest.core.bean;

import junit.framework.TestCase;
import org.openspaces.pu.container.jee.context.BootstrapWebApplicationContextListener;

import javax.servlet.*;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

public class InitAbstractBeanTest extends TestCase {

    public static void testAbstractBeanShouldIgnored() {

        BootstrapWebApplicationContextListener c = new BootstrapWebApplicationContextListener();
        c.contextInitialized(new ServletContextEvent(
                new StubServletContext("./org/openspaces/utest/core/bean/abstract-bean-context.xml")));
        System.out.println(c);

    }

    private static final class StubServletContext implements ServletContext {
        private String pathToPuXml;


        public StubServletContext(String pathToPuXml) {
            this.pathToPuXml = pathToPuXml;
        }


        public Object getAttribute(String arg0) {

            return null;
        }


        public Enumeration getAttributeNames() {

            return null;
        }


        public ServletContext getContext(String arg0) {

            return null;
        }


        public String getContextPath() {

            return null;
        }


        public String getInitParameter(String arg0) {

            return null;
        }


        public Enumeration getInitParameterNames() {

            return null;
        }


        public int getMajorVersion() {

            return 0;
        }


        public String getMimeType(String arg0) {

            return null;
        }


        public int getMinorVersion() {

            return 0;
        }


        public RequestDispatcher getNamedDispatcher(String arg0) {

            return null;
        }


        public String getRealPath(String arg0) {
            return pathToPuXml;
        }


        public RequestDispatcher getRequestDispatcher(String arg0) {

            return null;
        }


        public URL getResource(String arg0) throws MalformedURLException {

            return null;
        }


        public InputStream getResourceAsStream(String arg0) {

            return null;
        }


        public Set getResourcePaths(String arg0) {

            return null;
        }


        public String getServerInfo() {

            return null;
        }


        public Servlet getServlet(String arg0) throws ServletException {

            return null;
        }


        public String getServletContextName() {

            return null;
        }


        public Enumeration getServletNames() {

            return null;
        }


        public Enumeration getServlets() {

            return null;
        }


        public void log(String arg0) {

        }


        public void log(Exception arg0, String arg1) {

        }


        public void log(String arg0, Throwable arg1) {

        }


        public void removeAttribute(String arg0) {

        }


        public void setAttribute(String arg0, Object arg1) {

        }

    }
}
