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
package org.openspaces.maven.support;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.springframework.core.JdkVersion;

import com.j_spaces.kernel.PlatformVersion;

/**
 * @author kimchy
 */
public class POMGenerator {

    public static final String GS_GROUP = "com.gigaspaces";
    public static final String CLOUDIFY_GROUP = "org.cloudifysource";

    public static void main(String[] args) throws Exception {
        String templDir = System.getProperty("java.io.tmpdir");
        if (args.length > 0) {
            templDir = args[0];
        }

        String xapVersion = OutputVersion.computeXapVersion();
        String cloudifyVersion = null;
        boolean isCloudify = PlatformVersion.getEdition().equals(PlatformVersion.EDITION_CLOUDIFY);
        if (isCloudify) {
            cloudifyVersion = OutputVersion.computeCloudifyVersion();
        }
        
        File dir = new File(templDir);
        dir.mkdirs();

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, "gs-runtime-pom.xml")))));
        printHeader(writer, xapVersion, POMGenerator.GS_GROUP, "gs-runtime");

        // jmx
        if (!JdkVersion.isAtLeastJava15()) {
            printDependency(writer, "com.sun.jdmk", "jmxtools", "1.2.1");
            printDependency(writer, "javax.management", "jmxremote", "1.0.1_04");
            printDependency(writer, "javax.management", "jmxri", "1.2.1");
            printDependency(writer, "backport-util-concurrent", "backport-util-concurrent", "3.0");
        }

        printFooter(writer);
        writer.close();

        writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, "gs-openspaces-pom.xml")))));
        printHeader(writer, xapVersion, POMGenerator.GS_GROUP, "gs-openspaces");
        printDependency(writer, POMGenerator.GS_GROUP, "gs-runtime", xapVersion);
        printDependency(writer, "org.springframework", "spring-aop");
        printDependency(writer, "org.springframework", "spring-aspects");
        printDependency(writer, "org.springframework", "spring-beans");
        printDependency(writer, "org.springframework", "spring-context");
        printDependency(writer, "org.springframework", "spring-context-support");
        printDependency(writer, "org.springframework", "spring-core");
        printDependency(writer, "org.springframework", "spring-expression");
        printDependency(writer, "org.springframework", "spring-tx");
        printDependency(writer, "commons-logging", "commons-logging", "1.1.1");
        // add javax.annotations (@PostConstruct) for JDK 1.5 (no need for 1.6 since it is there)
        if (!JdkVersion.isAtLeastJava16() && JdkVersion.isAtLeastJava15()) {
            printDependency(writer, "org.apache.geronimo.specs", "geronimo-annotation_1.0_spec", "1.1.1");
        }
        printFooter(writer);
        writer.close();

        writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, "mule-os-pom.xml")))));
        printHeader(writer, xapVersion, POMGenerator.GS_GROUP, "mule-os");
        printFooter(writer);
        writer.close();
        
        writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, "jetty-os-pom.xml")))));
        printHeader(writer, xapVersion, POMGenerator.GS_GROUP, "jetty-os");
        printFooter(writer);
        writer.close();
        
        if ( isCloudify ) {
            writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, "dsl-pom.xml")))));
            printHeader(writer, cloudifyVersion, POMGenerator.CLOUDIFY_GROUP, "dsl");
            printDependency(writer, POMGenerator.GS_GROUP, "gs-openspaces", xapVersion);
            printFooter(writer);
            writer.close();
            
            writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, "usm-pom.xml")))));
            printHeader(writer, cloudifyVersion, POMGenerator.CLOUDIFY_GROUP, "usm");
            printDependency(writer, POMGenerator.GS_GROUP, "gs-openspaces", xapVersion);
            printDependency(writer, POMGenerator.CLOUDIFY_GROUP, "dsl", cloudifyVersion);
            printFooter(writer);
            writer.close();
            
            writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, "esc-pom.xml")))));
            printHeader(writer, cloudifyVersion, POMGenerator.CLOUDIFY_GROUP, "esc");
            printDependency(writer, POMGenerator.GS_GROUP, "gs-openspaces", xapVersion);
            printFooter(writer);
            writer.close();
        }
        
    }


    public static void printHeader(PrintWriter writer, String version, String groupId, String artifactId) throws Exception {
        writer.println("<project xmlns=\"http://maven.apache.org/POM/4.0.0\"");
        writer.println("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        writer.println("  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0");
        writer.println("                      http://maven.apache.org/xsd/maven-4.0.0.xsd\">");
        writer.println("  <modelVersion>4.0.0</modelVersion>");
        writer.println("  <groupId>" + groupId + "</groupId>");
        writer.println("  <artifactId>" + artifactId + "</artifactId>");
        writer.println("  <packaging>jar</packaging>");
        writer.println("  <version>" + version + "</version>");
        writer.println("  <url>http://www.gigaspaces.com</url>");
        writer.println("  <dependencies>");
    }

    public static void printDependency(PrintWriter writer, String groupId, String artifactId) {
    	final String version = null;
    	printDependency(writer, groupId, artifactId, version);
    }
    public static void printDependency(PrintWriter writer, String groupId, String artifactId, String version) {
        writer.println("    <dependency>");
        writer.println("      <groupId>" + groupId + "</groupId>");
        writer.println("      <artifactId>" + artifactId + "</artifactId>");
        if (version != null) {
        	writer.println("      <version>" + version + "</version>");
        }
        writer.println("    </dependency>");
    }

    public static void printFooter(PrintWriter writer) throws Exception {
        writer.println("  </dependencies>");
        writer.println("</project>");
    }
}
