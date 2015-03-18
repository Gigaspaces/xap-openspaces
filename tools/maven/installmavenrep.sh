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

# Dependencies that will be installed into the local maven repository
DEPENDENCY_LIST="gs-openspaces,gs-openspaces-jetty,mongo-datasource,mule-os"
${JAVACMD} -cp ${GS_JARS} org.openspaces.maven.support.POMGenerator ${TMPDIR} ${DEPENDENCY_LIST}

echo ""
echo ""
echo "Installing XAP $XAP_VERSION jars"
echo ""
echo ""

# GigaSpaces Jars
mvn -f ${TMPDIR}/gs-dependencies-pom.xml install

# Build and install OpenSpaces Maven Plugin
mvn -f maven-openspaces-plugin/pom.xml install -DcreateChecksum=true

# Remove temp files
rm ${TMPDIR}/gs-dependencies-pom.xml