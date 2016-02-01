#!/bin/bash

# The call to setenv.sh can be commented out if necessary.
export XAP_HOME=`dirname $0`/../../
. `dirname $0`/../../bin/setenv.sh

JAVACMD="${JAVA_HOME}/bin/java"

CPS=":"
export CPS

VELOCITY_JARS="${XAP_HOME}"/lib/platform/velocity/*
export VELOCITY_JARS

COMMONS_JARS="${XAP_HOME}"/lib/platform/commons/*
export COMMONS_JARS

COMMAND_LINE="${JAVACMD} ${JAVA_OPTIONS} -Dlb.vmDir="${XAP_HOME}/tools/apache" ${RMI_OPTIONS} ${XAP_OPTIONS} -Djava.security.policy=${POLICY} -Dcom.gs.home=${XAP_HOME} -classpath "${PRE_CLASSPATH}:${GS_JARS}:${SPRING_JARS}:${JDBC_JARS}:${VELOCITY_JARS}:${COMMONS_JARS}:${POST_CLASSPATH}" org.openspaces.pu.container.jee.lb.apache.ApacheLoadBalancerAgent $*"

echo
echo
echo Starting apache-lb-agent with line:
echo ${COMMAND_LINE}

${COMMAND_LINE}
echo
echo
