@echo off

@rem The call to setenv.bat can be commented out if necessary.
@call "%~dp0\setenv.bat"

@echo Starting a Processing Unit Instance
set COMMAND_LINE=%JAVACMD% %JAVA_OPTIONS% %XAP_OPTIONS% %GS_LOGGING_CONFIG_FILE_PROP% %RMI_OPTIONS% "-Dcom.gs.home=%XAP_HOME%" -Djava.security.policy=%POLICY% -classpath %PRE_CLASSPATH%;%GS_JARS%;%SPRING_JARS%;%EXT_JARS%;%JDBC_JARS%;%POST_CLASSPATH% org.openspaces.pu.container.standalone.StandaloneProcessingUnitContainer %*

set TITLE="Processing Unit Instance ["%*"] started on [%computername%]"
@title %TITLE%

echo.
echo.
echo Starting puInstance with line:
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