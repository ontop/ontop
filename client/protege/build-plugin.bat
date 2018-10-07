setlocal

REM Builds the Protege plugin for debug purposes
REM Source: http://github.com/ontop/ontop/wiki/ObdalibPluginDebug

SET CURRENTDIR="%CD%"

REM Build ontop bundles
cd "%CURRENTDIR%\..\.."
REM mvn install -DskipTests -Dmaven.javadoc.skip=true

REM Compile ontop-protege plugin jar file
cd "%CURRENTDIR%"
mvn bundle:bundle

REM Copy the jar to Protege plugins.
REM You may need to unzip first: cd "%CURRENTDIR%/build/dependencies"; unzip protege-5.0.0-beta-21-platform-independent.zip -d protege
xcopy /Y "%CURRENTDIR%\target\it.unibz.inf.ontop.protege-3.0.0-beta-2-SNAPSHOT.jar" "%CURRENTDIR%\..\..\build\distribution\Protege-5.2.0\plugins\"

endlocal