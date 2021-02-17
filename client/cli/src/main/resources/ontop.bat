@echo off

set JAVA=java
if exist "%JAVA_HOME%\bin\java.exe" set JAVA="%JAVA_HOME%\bin\java"

pushd %~dp0
set ONTOP_HOME=%CD%
popd

set CLASSPATH=%ONTOP_HOME%\lib\*;%ONTOP_HOME%\jdbc\*

%JAVA% %ONTOP_JAVA_ARGS% -cp "%CLASSPATH%" -Dlogback.configurationFile="%ONTOP_HOME%\log\logback.xml" -Dlogging.config="%ONTOP_HOME%\log\logback.xml" it.unibz.inf.ontop.cli.Ontop %*
