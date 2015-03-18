@echo off

if "%1" == ""  (
@rem The call to setenv.bat can be commented out if necessary.
@call "%~dp0\..\..\bin\setenv.bat"
)

if "%1" == "edition"  (
%JAVACMD% -cp %GS_JARS% org.openspaces.maven.support.OutputVersion edition
goto END
)

if "%1" == "XAP"  (
%JAVACMD% -cp %GS_JARS% org.openspaces.maven.support.OutputVersion XAP
goto END
)

FOR /F "usebackq tokens=*" %%i IN (`installmavenrep.bat edition`) DO @set EDITION=%%i

FOR /F "usebackq tokens=*" %%i IN (`installmavenrep.bat XAP`) DO @set XAP_VERSION=%%i

REM Dependencies that will be installed into the local maven repository
set DEPENDENCY_LIST=gs-openspaces,gs-openspaces-jetty,mongo-datasource,mule-os

%JAVACMD% -cp %GS_JARS% org.openspaces.maven.support.POMGenerator "%TEMP%" "%DEPENDENCY_LIST%"

echo ""
echo ""
echo "Installing XAP %XAP_VERSION% jars"
echo ""
echo ""

REM GigaSpaces Jars
call mvn -f %TEMP%/gs-dependencies-pom.xml install

REM Build and install OpenSpaces Maven Plugin 
call mvn -f maven-openspaces-plugin/pom.xml install  -DcreateChecksum=true

REM remove temp files
del %TEMP%\gs-dependencies-pom.xml

:END
