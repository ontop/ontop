@echo off

set JAVA=java
if exist "%JAVA_HOME%\bin\java.exe" set JAVA="%JAVA_HOME%\bin\java"

pushd %~dp0
set ONTOP_HOME=%CD%
popd

set CLASSPATH=%ONTOP_HOME%\lib\*;%ONTOP_HOME%\jdbc\*

set ENCODING=%ONTOP_FILE_ENCODING%
if "%ENCODING%" == "" (set ENCODING="UTF-8")

%JAVA% %ONTOP_JAVA_ARGS% -cp "%CLASSPATH%" -Dfile.encoding="%ENCODING%" -Dlogback.configurationFile="%ONTOP_HOME%\log\logback.xml" -Dlogging.config="%ONTOP_HOME%\log\logback.xml" it.unibz.inf.ontop.cli.Ontop %*
