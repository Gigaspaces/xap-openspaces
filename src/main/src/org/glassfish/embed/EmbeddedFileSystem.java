/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 */
package org.glassfish.embed;

import static com.sun.enterprise.universal.glassfish.SystemPropertyConstants.INSTALL_ROOT_PROPERTY;
import static com.sun.enterprise.universal.glassfish.SystemPropertyConstants.INSTANCE_ROOT_PROPERTY;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.io.FileUtils;
import org.openspaces.pu.container.jee.glassfish.GlassfishJeeProcessingUnitContainerProvider;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * There are currently some ugly things we MUST do:
 * <ul>
 * <li>write out our hard-wired domain.xml to disk for use by core V3
 * <li>write out magic JDBC xml files to disk
 * </ul>
 * We are concentrating these things here for ease of maintenance.
 *
 * @author bnevins
 */

/**
 * We change this one in order to set the work directory
 */
public final class EmbeddedFileSystem {

    public static void setSystemProps() {
        efs.setSystemPropsInternal();
    }

    public static File getInstallRoot() {
        return efs.installRoot;
    }

    public static File getInstanceRoot() {
        return efs.instanceRoot;
    }

    public static void setRoot(File f) throws EmbeddedException {
        setInstallRoot(f);
        setInstanceRoot(f);
    }

    public static void setInstallRoot(File f) throws EmbeddedException {
        efs.installRoot = SmartFile.sanitize(f);

        f.mkdirs();

        if (!f.isDirectory()) {
            throw new EmbeddedException("bad_install_root", f);
        }

        efs.setSystemPropsInternal();
    }

    public static void setInstanceRoot(File f) throws EmbeddedException {
        efs.instanceRoot = SmartFile.sanitize(f);

        f.mkdirs();

        if (!f.isDirectory()) {
            throw new EmbeddedException("bad_instance_root", f);
        }

        efs.setSystemPropsInternal();
    }

    public static void setAutoDelete(boolean b) {
        efs.autoDelete = b;
    }

    static void cleanup() {
        if (efs.autoDelete) {
            FileUtils.whack(efs.installRoot);
            FileUtils.whack(efs.instanceRoot);
        }
    }

    static URL getDomainXmlUrl() {
        File dom = new File(getInstanceRoot(), "domain.xml");

        if(!dom.exists())
            return null;
        try {
            return dom.toURI().toURL();
        }
        catch (MalformedURLException ex) {
            return null;
        }
    }

    private EmbeddedFileSystem() {
        defaultRoots.mkdirs();
        installRoot = SmartFile.sanitize(defaultRoots);
        instanceRoot = SmartFile.sanitize(defaultRoots);
        setSystemPropsInternal();
    }

    private void setSystemPropsInternal() {
        System.setProperty(INSTANCE_ROOT_PROPERTY, instanceRoot.getPath());
        System.setProperty(INSTALL_ROOT_PROPERTY, installRoot.getPath());
    }
    // GIGASPACES CHANGE
    private static final File defaultRoots = GlassfishJeeProcessingUnitContainerProvider.getGlassfishInstanceWorkDir();
    private static final EmbeddedFileSystem efs = new EmbeddedFileSystem();
    private File installRoot;
    private File instanceRoot;
    private boolean autoDelete = true;
}
