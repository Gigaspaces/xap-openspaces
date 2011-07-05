#!/bin/sh

# The call to setenv.sh can be commented out if necessary.
export JSHOMEDIR=`dirname $0`/../../
. `dirname $0`/../../bin/setenv.sh

if [ "${JSHOMEDIR}" = "" ] ; then
  JSHOMEDIR=`dirname $0`/..
fi
export JSHOMEDIR
export TMPDIR="/tmp"
export VERSION=`${JAVACMD} -cp ${GS_JARS} org.openspaces.maven.support.OutputVersion`

${JAVACMD} -cp ${GS_JARS} org.openspaces.maven.support.POMGenerator $TMPDIR $VERSION "${JSHOMEDIR}/tools/maven/maven-openspaces-plugin"

echo ""
echo ""
echo "Installing Version $VERSION"
echo ""
echo ""

# GigaSpaces Jars
mvn install:install-file -DgroupId=com.gigaspaces -DcreateChecksum=true -DartifactId=gs-runtime -Dversion=$VERSION -DpomFile=$TMPDIR/gs-runtime-pom.xml -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/required/gs-runtime.jar -Djavadoc="%JSHOMEDIR%/docs/javadoc.zip"
mvn install:install-file -DgroupId=com.gigaspaces -DcreateChecksum=true -DartifactId=gs-openspaces -Dversion=$VERSION -DpomFile=$TMPDIR/gs-openspaces-pom.xml -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/required/gs-openspaces.jar -Djavadoc="%JSHOMEDIR%/docs/javadoc.zip" -Dsources="%JSHOMEDIR%/lib/optional/openspaces/gs-openspaces-src.zip"
mvn install:install-file -DgroupId=com.gigaspaces -DcreateChecksum=true -DartifactId=mule-os -Dversion=$VERSION -Dpackaging=jar -DpomFile=$TMPDIR/mule-os-pom.xml -Dfile=${JSHOMEDIR}/lib/optional/openspaces/mule-os.jar
mvn install:install-file -DgroupId=com.gigaspaces -DcreateChecksum=true -DartifactId=jetty-os -Dversion=$VERSION -Dpackaging=jar -DpomFile=$TMPDIR/jetty-os-pom.xml -Dfile=${JSHOMEDIR}/lib/platform/openspaces/gs-openspaces-jetty.jar

# Build and install OpenSpaces Maven Plugin
mvn -f maven-openspaces-plugin/pom.xml install -DcreateChecksum=true

# Copy licenese file
mvn os:install-license -Dfile=$JSHOMEDIR/gslicense.xml -Dversion=$VERSION

# Remove temp files
rm $TMPDIR/gs-runtime-pom.xml
rm $TMPDIR/gs-openspaces-pom.xml
rm $TMPDIR/mule-os-pom.xml
rm $TMPDIR/jetty-os-pom.xml
