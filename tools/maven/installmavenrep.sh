#!/bin/sh

# The call to setenv.sh can be commented out if necessary.
export XAP_HOME=`dirname $0`/../../
. `dirname $0`/../../bin/setenv.sh

if [ "${XAP_HOME}" = "" ] ; then
  XAP_HOME=`dirname $0`/..
fi
export XAP_HOME
export TMPDIR="/tmp"
export EDITION=`${JAVACMD} -cp ${GS_JARS} org.openspaces.maven.support.OutputVersion edition`

export XAP_VERSION=`${JAVACMD} -cp ${GS_JARS} org.openspaces.maven.support.OutputVersion XAP`

echo ""
echo ""
echo "Installing XAP $XAP_VERSION jars"
echo ""
echo ""

# GigaSpaces Jars
mvn install:install-file -DgroupId=com.gigaspaces -DcreateChecksum=true -DartifactId=gs-runtime -Dversion=$XAP_VERSION -DpomFile=${XAP_HOME}/tools/maven/poms/gs-runtime/pom.xml -Dpackaging=jar -Dfile=${XAP_HOME}/lib/required/gs-runtime.jar
mvn install:install-file -DgroupId=com.gigaspaces -DcreateChecksum=true -DartifactId=gs-openspaces -Dversion=$XAP_VERSION -DpomFile=${XAP_HOME}/tools/maven/poms/gs-openspaces/pom.xml -Dpackaging=jar -Dfile=${XAP_HOME}/lib/required/gs-openspaces.jar -Dsources="${XAP_HOME}/lib/optional/openspaces/gs-openspaces-sources.jar"
mvn install:install-file -DgroupId=com.gigaspaces -DcreateChecksum=true -DartifactId=mule-os -Dversion=$XAP_VERSION -Dpackaging=jar -DpomFile=${XAP_HOME}/tools/maven/poms/mule-os/pom.xml -Dfile=${XAP_HOME}/lib/optional/openspaces/mule-os.jar
mvn install:install-file -DgroupId=com.gigaspaces -DcreateChecksum=true -DartifactId=jetty-os -Dversion=$XAP_VERSION -Dpackaging=jar -DpomFile=${XAP_HOME}/tools/maven/poms/jetty-os/pom.xml -Dfile=${XAP_HOME}/lib/platform/openspaces/gs-openspaces-jetty.jar
mvn install:install-file -DgroupId=com.gigaspaces -DcreateChecksum=true -DartifactId=mongo-datasource -Dversion=$XAP_VERSION -Dpackaging=jar -DpomFile=${XAP_HOME}/tools/maven/poms/mongo-datasource/pom.xml -Dfile=${XAP_HOME}/lib/optional/datasource/mongo/mongo-datasource.jar

# Build and install OpenSpaces Maven Plugin
mvn -f maven-openspaces-plugin/pom.xml install -DcreateChecksum=true
