/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.utest.core.bean;

import junit.framework.TestCase;
import org.openspaces.pu.container.jee.context.BootstrapWebApplicationContextListener;

import javax.servlet.*;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.descriptor.JspConfigDescriptor;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
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


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#addFilter(java.lang.String, java.lang.String)
         */
        @Override
        public Dynamic addFilter(String arg0, String arg1) {
            // TODO Auto-generated method stub
            return null;
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#addFilter(java.lang.String, javax.servlet.Filter)
         */
        @Override
        public Dynamic addFilter(String arg0, Filter arg1) {
            // TODO Auto-generated method stub
            return null;
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#addFilter(java.lang.String, java.lang.Class)
         */
        @Override
        public Dynamic addFilter(String arg0, Class<? extends Filter> arg1) {
            // TODO Auto-generated method stub
            return null;
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#addListener(java.lang.String)
         */
        @Override
        public void addListener(String arg0) {
            // TODO Auto-generated method stub
            
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#addListener(java.util.EventListener)
         */
        @Override
        public <T extends EventListener> void addListener(T arg0) {
            // TODO Auto-generated method stub
            
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#addListener(java.lang.Class)
         */
        @Override
        public void addListener(Class<? extends EventListener> arg0) {
            // TODO Auto-generated method stub
            
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#addServlet(java.lang.String, java.lang.String)
         */
        @Override
        public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0, String arg1) {
            // TODO Auto-generated method stub
            return null;
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#addServlet(java.lang.String, javax.servlet.Servlet)
         */
        @Override
        public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0, Servlet arg1) {
            // TODO Auto-generated method stub
            return null;
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#addServlet(java.lang.String, java.lang.Class)
         */
        @Override
        public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0, Class<? extends Servlet> arg1) {
            // TODO Auto-generated method stub
            return null;
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#createFilter(java.lang.Class)
         */
        @Override
        public <T extends Filter> T createFilter(Class<T> arg0) throws ServletException {
            // TODO Auto-generated method stub
            return null;
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#createListener(java.lang.Class)
         */
        @Override
        public <T extends EventListener> T createListener(Class<T> arg0) throws ServletException {
            // TODO Auto-generated method stub
            return null;
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#createServlet(java.lang.Class)
         */
        @Override
        public <T extends Servlet> T createServlet(Class<T> arg0) throws ServletException {
            // TODO Auto-generated method stub
            return null;
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#declareRoles(java.lang.String[])
         */
        @Override
        public void declareRoles(String... arg0) {
            // TODO Auto-generated method stub
            
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getClassLoader()
         */
        @Override
        public ClassLoader getClassLoader() {
            // TODO Auto-generated method stub
            return null;
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getDefaultSessionTrackingModes()
         */
        @Override
        public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
            // TODO Auto-generated method stub
            return null;
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getEffectiveMajorVersion()
         */
        @Override
        public int getEffectiveMajorVersion() {
            // TODO Auto-generated method stub
            return 0;
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getEffectiveMinorVersion()
         */
        @Override
        public int getEffectiveMinorVersion() {
            // TODO Auto-generated method stub
            return 0;
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getEffectiveSessionTrackingModes()
         */
        @Override
        public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
            // TODO Auto-generated method stub
            return null;
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getFilterRegistration(java.lang.String)
         */
        @Override
        public FilterRegistration getFilterRegistration(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getFilterRegistrations()
         */
        @Override
        public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
            // TODO Auto-generated method stub
            return null;
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getJspConfigDescriptor()
         */
        @Override
        public JspConfigDescriptor getJspConfigDescriptor() {
            // TODO Auto-generated method stub
            return null;
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getServletRegistration(java.lang.String)
         */
        @Override
        public ServletRegistration getServletRegistration(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getServletRegistrations()
         */
        @Override
        public Map<String, ? extends ServletRegistration> getServletRegistrations() {
            // TODO Auto-generated method stub
            return null;
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getSessionCookieConfig()
         */
        @Override
        public SessionCookieConfig getSessionCookieConfig() {
            // TODO Auto-generated method stub
            return null;
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#setInitParameter(java.lang.String, java.lang.String)
         */
        @Override
        public boolean setInitParameter(String arg0, String arg1) {
            // TODO Auto-generated method stub
            return false;
        }


        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#setSessionTrackingModes(java.util.Set)
         */
        @Override
        public void setSessionTrackingModes(Set<SessionTrackingMode> arg0) {
            // TODO Auto-generated method stub
            
        }

    }
}
