#!/bin/sh

# The call to setenv.sh can be commented out if necessary.
export JSHOMEDIR=`dirname $0`/../../
. `dirname $0`/../../bin/setenv.sh

if [ "${JSHOMEDIR}" = "" ] ; then
  JSHOMEDIR=`dirname $0`/..
fi
export JSHOMEDIR
export TMPDIR="/tmp"
export EDITION=`${JAVACMD} -cp ${GS_JARS} org.openspaces.maven.support.OutputVersion edition`

export XAP_VERSION=`${JAVACMD} -cp ${GS_JARS} org.openspaces.maven.support.OutputVersion XAP`

echo ""
echo ""
echo "Installing XAP $XAP_VERSION jars"
echo ""
echo ""

# GigaSpaces Jars
mvn install:install-file -DgroupId=com.gigaspaces -DcreateChecksum=true -DartifactId=gs-runtime -Dversion=$XAP_VERSION -DpomFile=${JSHOMEDIR}/tools/maven/poms/gs-runtime/pom.xml -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/required/gs-runtime.jar
mvn install:install-file -DgroupId=com.gigaspaces -DcreateChecksum=true -DartifactId=gs-openspaces -Dversion=$XAP_VERSION -DpomFile=${JSHOMEDIR}/tools/maven/poms/gs-openspaces/pom.xml -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/required/gs-openspaces.jar -Dsources="${JSHOMEDIR}/lib/optional/openspaces/gs-openspaces-sources.jar"
mvn install:install-file -DgroupId=com.gigaspaces -DcreateChecksum=true -DartifactId=mule-os -Dversion=$XAP_VERSION -Dpackaging=jar -DpomFile=${JSHOMEDIR}/tools/maven/poms/mule-os/pom.xml -Dfile=${JSHOMEDIR}/lib/optional/openspaces/mule-os.jar
mvn install:install-file -DgroupId=com.gigaspaces -DcreateChecksum=true -DartifactId=jetty-os -Dversion=$XAP_VERSION -Dpackaging=jar -DpomFile=${JSHOMEDIR}/tools/maven/poms/jetty-os/pom.xml -Dfile=${JSHOMEDIR}/lib/platform/openspaces/gs-openspaces-jetty.jar
mvn install:install-file -DgroupId=com.gigaspaces -DcreateChecksum=true -DartifactId=mongo-datasource -Dversion=$XAP_VERSION -Dpackaging=jar -DpomFile=${JSHOMEDIR}/tools/maven/poms/mongo-datasource/pom.xml -Dfile=${JSHOMEDIR}/lib/optional/datasource/mongo/mongo-datasource.jar

# Build and install OpenSpaces Maven Plugin
mvn -f maven-openspaces-plugin/pom.xml install -DcreateChecksum=true
