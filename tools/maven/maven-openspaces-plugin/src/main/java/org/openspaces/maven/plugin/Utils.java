package org.openspaces.maven.plugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import sun.misc.URLClassPath;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;


/**
 * This class contains static helper methods for the Plugin.
 *
 * @author shaiw
 */
public class Utils {

    static final String GS_TYPE = "gsType";

    static final String GS_TYPE_PU = "PU";

    /**
     * Uses reflection to replace all white spaces in the ClassLoader's URLs to %20.
     */
    static void changeClassLoaderToSupportWhiteSpacesRepository(ClassLoader mavenClassLoader)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException, MalformedURLException {
        // Get the class of the ClassLoader, this should be RealmClassLoader.
        Class mavenClassLoaderClass = mavenClassLoader.getClass();

        // Get the super class of RealmClassLoader, this should be URLClassLoader.
        Class clazz2 = mavenClassLoaderClass.getSuperclass();

        // get the ucp field of URLClassLoader that holds the URLs
        Field ucpField = clazz2.getDeclaredField("ucp");

        // make this field accessible
        if (!ucpField.isAccessible()) {
            ucpField.setAccessible(true);
        }

        // get the value of the field, this should be URLClassPath.
        Object value = ucpField.get(mavenClassLoader);

        // get the URLs
        URLClassPath urlClassPath = (URLClassPath) value;
        URL[] urls = urlClassPath.getURLs();
        URL[] urls2 = new URL[urls.length];

        // replace the white spaces of all URLs with %20
        for (int i = 0; i < urls.length; i++) {
            String path = urls[i].toExternalForm();
            path = path.replaceAll(" ", "%20");
            urls2[i] = new URL(path);
        }

        // create the new URLClassPath
        URLClassPath uRLClassPath2 = new URLClassPath(urls2);

        // update the ucf field value 
        ucpField.set(mavenClassLoader, uRLClassPath2);
    }


    /**
     * Checks if a project is a PU project.
     *
     * @param project the project
     * @return true if the project is a PU project, false otherwise.
     */
    static boolean isPUType(MavenProject project) {
        String gsType = project.getProperties().getProperty(Utils.GS_TYPE);
        if (gsType == null) {
            return false;
        }
        return gsType.equalsIgnoreCase(Utils.GS_TYPE_PU);
    }


    /**
     * Gets a list of projects and returns only those that are PU.
     *
     * @param projects a list of projects.
     * @return a list of projects and returns only those that are PU.
     */
    static List getProjectsToExecute(List projects, String moduleName) {
        List puProjects = new ArrayList();
        Iterator i = projects.iterator();
        while (i.hasNext()) {
            MavenProject proj = (MavenProject) i.next();
            if (Utils.isPUType(proj) &&
                    (moduleName == null || moduleName.equals(proj.getName()))) {
                puProjects.add(proj);
            }
        }
        return puProjects;
    }


    /**
     * Resolves the processing unit's dependencies classpath.
     * It includes the processing unit's executables and dependencies.
     *
     * @return a list containing all paths.
     */
    static List resolveClasspath(MavenProject project) throws Exception {
        List dependencyFiles = resolveDependencyClasspath(project);
        dependencyFiles.add(getURL(project.getArtifact().getFile()));
        return dependencyFiles;
    }


    /**
     * Resolves the processing unit's dependencies classpath.
     *
     * @return a list containing all dependencies paths.
     */
    static List resolveDependencyClasspath(MavenProject project) throws Exception {
        List dependencyFiles = new ArrayList();
        Collection dependencyArtifacts = project.getArtifacts();
        for (Iterator i = dependencyArtifacts.iterator(); i.hasNext();) {
            Artifact artifact = (Artifact) i.next();
            dependencyFiles.add(getURL(artifact.getFile()));
        }
        return dependencyFiles;
    }


    /**
     * Replaces white spaces in URLs to %20.
     *
     * @param f the file
     * @return A URL with %20 where white space suppose to be.
     * @throws Exception
     */
    static URL getURL(File f) throws Exception {
        String path = f.toURL().toExternalForm();
        path = path.replaceAll(" ", "%20");
        return new URL(path);
    }


    /**
     * Creates the class loader of the PU application.
     *
     * @param classpathUrls the classpath to use by the class loader.
     * @param parent        the parent classloader
     * @return the class loader of the PU application.
     * @throws MalformedURLException
     */
    static ClassLoader createClassLoader(List classpathUrls, ClassLoader parent) throws Exception {
        // create the classloader
        ClassLoader urlCL = URLClassLoader.newInstance((URL[]) classpathUrls.toArray(new URL[classpathUrls.size()]), parent);
        return urlCL;
    }


    /**
     * In case there was a SecurityException about missing license file creates an exception with
     * a special message, explaining what to do. In casse of other exceptions, the original
     * exception is wrapped with MojoExecutionException.
     *
     * @param e the original exception
     * @return the new exception.
     * @throws MojoExecutionException
     */
    static void throwMissingLicenseException(Throwable e, ArtifactRepository localRepository) throws MojoExecutionException {
        System.out.println("rep: " + localRepository.getBasedir());
        Throwable cause = (Throwable) e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
            if (cause instanceof SecurityException) {
                if (cause.getMessage() != null && cause.getMessage().indexOf("gslicense.xml") > 0) {
                    String msg =
                            "\nThe GigaSpaces license file - gslicense.xml - was not found in Maven repository.\n" +
                                    "This file should be placed in the directory where gs-boot.jar resides.\n" +
                                    "Please try to reinstall OpenSpaces Plugin for Maven by running the installmavenrep' script again." +
                                    "Alternatively, copy gslicense.xml manually to " + localRepository.getBasedir() + "/com/gigaspaces/core/gs-boot/[version].";
                    throw new MojoExecutionException(msg);
                }
            }
        }
    }


    /**
     * Adds an attribute with all of its parameters to the list.
     *
     * @param list  the list
     * @param name  the attribute's name
     * @param value contains the attributes value or parameters
     */
    static void addAttributeToList(ArrayList list, String name, String value) {
        if (value != null) {
            list.add(name);
            StringTokenizer st = new StringTokenizer(value);
            String next;
            while (st.hasMoreTokens()) {
                next = st.nextToken();
                list.add(next);
            }
        }
    }


    /**
     * Finds the relative path from current directory to the PU jar file.
     *
     * @param project the Maven project
     * @return the relative path from current directory to the PU jar file.
     */
    static String getProcessingUnitJar(MavenProject project) {
        String targetDir = project.getBuild().getDirectory();
        String curDir = System.getProperty("user.dir");
        String relativePath = targetDir.substring(curDir.length() + 1);
        relativePath = relativePath.replace('\\', '/');
        String finalName = project.getBuild().getFinalName();
        return relativePath + "/" + finalName + ".jar";
    }


    /**
     * Finds the relative path from current directory to the PU dir.
     *
     * @param project the Maven project
     * @return the relative path from current directory to the PU dir.
     */
    static String getPURelativePathDir(MavenProject project) {
        String targetDir = project.getBuild().getDirectory();
        String curDir = System.getProperty("user.dir");
        String relativePath = targetDir.substring(curDir.length() + 1);
        relativePath = relativePath.replace('\\', '/');
        String finalName = project.getBuild().getFinalName();
        return relativePath + "/" + finalName;
    }


    static void handleSecurity() throws MojoExecutionException {
        if (System.getProperty("java.security.policy") == null) {
            try {
                Class secLoaderClass = Class.forName("com.j_spaces.kernel.SecurityPolicyLoader", true, Thread.currentThread().getContextClassLoader());
                secLoaderClass.getMethod("loadPolicy", new Class[]{String.class}).invoke(null, new Object[]{"policy/policy.all"});
            } catch (Exception e) {
                throw new MojoExecutionException("Failed to load security file", e);
            }
        }
    }
}