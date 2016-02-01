@echo off

@rem The call to setenv.bat can be commented out if necessary.
@call "%~dp0\..\..\bin\setenv.bat"

set VELOCITY_JARS="%XAP_HOME%\lib\platform\velocity\*;"
set COMMONS_JARS="%XAP_HOME%\lib\platform\commons\*;"

set COMMAND_LINE=%JAVACMD% %JAVA_OPTIONS% -Dlb.vmDir="%XAP_HOME%/tools/apache" %XAP_OPTIONS% %RMI_OPTIONS% "-Dcom.gs.home=%XAP_HOME%" -Djava.security.policy=%POLICY% -classpath %PRE_CLASSPATH%;%GS_JARS%;%SPRING_JARS%;%JDBC_JARS%;%VELOCITY_JARS%;%COMMONS_JARS%;%POST_CLASSPATH% org.openspaces.pu.container.jee.lb.apache.ApacheLoadBalancerAgent %*
echo %COMMAND_LINE%
set TITLE="Apache Load Balancer Agent ["%*"] started on [%computername%]"
@title %TITLE%

echo.
echo.
echo Starting apache-lb-agent with line:
echo %COMMAND_LINE%
echo.

%COMMAND_LINE%
goto end

:end
endlocal
title Command Prompt
set TITLE=
if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal
exit /B 0