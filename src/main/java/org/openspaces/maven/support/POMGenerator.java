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

import java.io.*;

/**
 * @author kimchy
 */
public class POMGenerator {

	public static final String GS_GROUP = "com.gigaspaces";
    public static final String POM_FILE_NAME = "gs-dependencies-pom.xml";
    public static final String POM_ARTIFACT_ID = "gs-dependencies";

    public static void main(String[] args) throws Exception {
        String templDir;
        String dependencies;
        if (args.length == 2) {
            templDir = args[0];
            dependencies = args[1];
        }
        else {
            printUsage();
            return;
        }

        String xapVersion = OutputVersion.computeXapVersion();

        File dir = new File(templDir);
        dir.mkdirs();

        String[] dependencyList = dependencies.split(",");

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(new File(dir, POM_FILE_NAME)))));

        printHeader(writer, xapVersion, POMGenerator.GS_GROUP, POM_ARTIFACT_ID);

        printOpenspacesMavenRepository(writer);

        printDependenciesHeader(writer);

        for (String dependency : dependencyList) {
            printDependency(writer, GS_GROUP, dependency , xapVersion);
        }

        printDependenciesFooter(writer);

        printProjectFooter(writer);

        writer.close();
    }

    private static void printOpenspacesMavenRepository(PrintWriter writer) {
        writer.println("<repositories>");
        writer.println("    <repository>");
        writer.println("        <id>org.openspaces</id>");
        writer.println("        <name>OpenSpaces</name>");
        writer.println("        <url>http://maven-repository.openspaces.org</url>");
        writer.println("        <releases>");
        writer.println("            <enabled>true</enabled>");
        writer.println("            <updatePolicy>daily</updatePolicy>");
        writer.println("            <checksumPolicy>warn</checksumPolicy>");
        writer.println("        </releases>");
        writer.println("        <snapshots>");
        writer.println("            <enabled>true</enabled>");
        writer.println("            <updatePolicy>daily</updatePolicy>");
        writer.println("            <checksumPolicy>warn</checksumPolicy>");
        writer.println("        </snapshots>");
        writer.println("    </repository>");
        writer.println("</repositories>");
    }


    public static void printHeader(PrintWriter writer, String version, String groupId, String artifactId) throws Exception {
        writer.println("<project xmlns=\"http://maven.apache.org/POM/4.0.0\"");
        writer.println("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        writer.println("  xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0");
        writer.println("                      http://maven.apache.org/xsd/maven-4.0.0.xsd\">");
        writer.println("  <modelVersion>4.0.0</modelVersion>");
        writer.println("  <groupId>" + groupId + "</groupId>");
        writer.println("  <artifactId>" + artifactId + "</artifactId>");
        writer.println("  <version>" + version + "</version>");
    }
    
    public static void printDependenciesHeader(PrintWriter writer) throws Exception {
        writer.println("  <dependencies>");
    }
    
    public static void printDependency(PrintWriter writer, String groupId, String artifactId) {
    	final String version = null;
    	printDependency(writer, groupId, artifactId, version);
    }
    
    public static void printDependency(PrintWriter writer, String groupId, String artifactId, String version) {
    	final String scope = null;
    	printDependency(writer, groupId, artifactId, version, scope);
    }
    
    public static void printTestDependency(PrintWriter writer, String groupId, String artifactId, String version) {
    	final String scope = "test";
    	printDependency(writer, groupId, artifactId, version, scope);
    }

    public static void printProvidedDependency(PrintWriter writer, String groupId, String artifactId, String version) {
    	final String scope = "provided";
    	printDependency(writer, groupId, artifactId, version, scope);
    }
    
    public static void printCompileDependency(PrintWriter writer, String groupId, String artifactId) {
    	final String version = null;
    	printCompileDependency(writer, groupId, artifactId, version);
    }
    
    public static void printCompileDependency(PrintWriter writer, String groupId, String artifactId, String version) {
    	final String scope = "compile";
    	printDependency(writer, groupId, artifactId, version, scope);
    }
    
    public static void printDependency(PrintWriter writer, String groupId, String artifactId, String version, String scope) {
        writer.println("    <dependency>");
        writer.println("      <groupId>" + groupId + "</groupId>");
        writer.println("      <artifactId>" + artifactId + "</artifactId>");
        if (version != null) {
        	writer.println("      <version>" + version + "</version>");
        }
        if (scope != null) {
        	writer.println("      <scope>" + scope + "</scope>");
        }
        writer.println("    </dependency>");
    }

    public static void printDependenciesFooter(PrintWriter writer) throws Exception {
        writer.println("  </dependencies>");
    }
    
    public static void printProjectFooter(PrintWriter writer) throws Exception {
        writer.println("</project>");
    }

    private static void printUsage() {
        System.out.println("Usage: %JAVACMD% -cp %GS_JARS% org.openspaces.maven.support.POMGenerator <directoryPlacingPomFile> <dependencyList>");
        System.out.println("Exiting...");
    }
}
