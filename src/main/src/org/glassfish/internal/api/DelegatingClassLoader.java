/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */


package org.glassfish.internal.api;

import com.sun.enterprise.module.common_impl.CompositeEnumeration;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * This classloader has a list of classloaders called as delegates
 * that it uses to find classes. All those delegates must have the
 * same parent as this classloader in order to have a consistent class space.
 * By consistent class space, I mean a class space where no two loaded class
 * have same name. An inconsistent class space can lead to ClassCastException.
 * This classloader does not define any class, classes are always loaded
 * either by its parent or by one of the delegates.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */

/**
 * GLASSFISH CHANGE
 * Overridden in OpenSpaces so it will extends URLClassLoader so building of JSP classpath
 * of Tomcat will work. See org.apache.catalina.loader.WebappLoader#createClasspath
 */

public class DelegatingClassLoader extends URLClassLoader {
    /*
     * TODO(Sahoo):
     * 1. I18N
     * 2. Move to a more common package, as it has no dependency on kernel.
     */

    /**
     * findClass method of ClassLoader is usually a protected method.
     * Calling loadClass on a ClassLoader is expenssive, as it searches
     * the delegation hierarchy before searching in its private space.
     * Hence we add this interface as an optimization.
     */
    public interface ClassFinder {

        /**
         * @see ClassLoader#getParent()
         */
        ClassLoader getParent();

        /**
         * @see ClassLoader#findClass(String)
         */
        Class<?> findClass(String name) throws ClassNotFoundException;

        /**
         * @see ClassLoader#findResource(String)
         */
        URL findResource(String name);

        /**
         * @see ClassLoader#findResources(String)
         */
        Enumeration<URL> findResources(String name) throws IOException;
    }

    /**
     * Name of this class loader. Used mostly for reporting purpose.
     * No guarantee about its uniqueness.
     */
    private String name;

    private List<ClassFinder> delegates = new ArrayList<ClassFinder>();

    /**
     * @throws IllegalArgumentException when the delegate does not have same parent
     * as this classloader.
     */
    public DelegatingClassLoader(ClassLoader parent, List<ClassFinder> delegates)
            throws IllegalArgumentException{
        super(new URL[0], parent);
        for (ClassFinder d : delegates) {
            checkDelegate(d);
        }
        this.delegates.addAll(delegates);
    }

    public DelegatingClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    /**
     * Adds a ClassFinder to list of delegates. This method must not be used
     * once this classloader has beed used to load any class. If attempted to
     * do so, this method throws IllegalStateException. To have a consistent
     * class space (by consistent class space, I mean a classpace where there
     * does not exist two class with same name), this method does not allow
     * a delegate to be added that has a different parent.
     * @param d ClassFinder to add to the list of delegates
     * @return true if the delegate is added, false otherwise.
     * @throws IllegalStateException when this method is called after the
     * classloader has been used to load any class.
     * @throws IllegalArgumentException when the delegate does not have same parent
     * as this classloader.
     */
    public synchronized boolean addDelegate(ClassFinder d) throws
            IllegalStateException, IllegalArgumentException {
        checkDelegate(d);
        if (delegates.contains(d)) {
            return false;
        }
        return delegates.add(d);
    }

    /**
     * @throws IllegalArgumentException when the delegate does not have same parent
     * as this classloader.
     */
    private void checkDelegate(ClassFinder d) throws IllegalArgumentException {
        final ClassLoader dp = d.getParent();
        final ClassLoader p = getParent();
        if (dp != p) { // check for equals
            if ((dp != null && !dp.equals(p)) || !p.equals(dp)) {
                throw new IllegalArgumentException("Delegation hierarchy mismatch");
            }
        }
    }

    /**
     * Removes a ClassFinder from list of delegates. This method must not be used
     * once this classloader has beed used to load any class. If attempted to
     * do so, this method throws IllegalStateException
     * @param d ClassFinder to remove from the list of delegates
     * @return true if the delegate was removed, false otherwise.
     * @throws IllegalStateException when this method is called after the
     * classloader has been used to load any class.
     */
    public synchronized boolean removeDelegate(ClassFinder d) {
        return delegates.remove(d);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        for (ClassFinder d : delegates) {
            try {
                return d.findClass(name);
            } catch (ClassNotFoundException e) {
                // Ignore, as we search next in list
            }
        }
        throw new ClassNotFoundException(name);
    }

    @Override
    public URL findResource(String name) {
        for (ClassFinder d : delegates) {
            URL u = d.findResource(name);
            if (u!=null) return u;
        }
        return null;
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        List<Enumeration<URL>> enumerators = new ArrayList<Enumeration<URL>>();
        for (ClassFinder delegate : delegates) {
            Enumeration<URL> enumerator = delegate.findResources(name);
            enumerators.add(enumerator);
        }
        return new CompositeEnumeration(enumerators);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ClassFinder> getDelegates() {
        return Collections.unmodifiableList(delegates);
    }

    @Override
    public String toString() {
        if (name!=null) {
            return name;
        } else {
            return super.toString();
        }
    }
}
