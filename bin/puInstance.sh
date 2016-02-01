#!/bin/bash

# The call to setenv.sh can be commented out if necessary.
. `dirname $0`/setenv.sh

echo Starting a Processing Unit Instance

COMMAND_LINE="${JAVACMD} ${JAVA_OPTIONS} ${RMI_OPTIONS} ${XAP_OPTIONS} -Djava.security.policy=${POLICY} -Dcom.gs.home=${XAP_HOME} -classpath "${PRE_CLASSPATH}${CPS}${GS_JARS}${CPS}${SPRING_JARS}${CPS}${EXT_JARS}$CPS${JDBC_JARS}${CPS}${POST_CLASSPATH}" org.openspaces.pu.container.standalone.StandaloneProcessingUnitContainer $*"

echo
echo
echo Starting puInstance with line:
echo ${COMMAND_LINE}

${COMMAND_LINE}
echo
echo
