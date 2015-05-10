@echo off

set JAVA=java
if exist "%JAVA_HOME%\bin\java.exe" set JAVA="%JAVA_HOME%\bin\java"

pushd %~dp0
set ONTOP_HOME=%CD%
popd

set CLASSPATH=%ONTOP_HOME%\lib\*
set EXT_DIRS=%ONTOP_HOME%\lib;%ONTOP_HOME%\jdbc

%JAVA% -cp %CLASSPATH% -Dlogback.configurationFile=%ONTOP_HOME%\log\logback.xml -Djava.ext.dirs=%EXT_DIRS% org.semanticweb.ontop.cli.Ontop %*