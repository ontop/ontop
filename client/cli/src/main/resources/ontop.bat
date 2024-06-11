@echo off
setlocal

rem
rem ONTOP STARTUP SCRIPT
rem
rem This script runs Ontop CLI (class it.unibz.inf.ontop.cli.Ontop) passing the supplied command line arguments and
rem current environment variables. Run without arguments to see available commands and their syntax.
rem
rem The following functionalities are implemented within this script, rather than in Ontop Java code:
rem - setting JVM option '-Xmx' based on variable ONTOP_JAVA_ARGS (default: 512m)
rem - setting Java property 'file.encoding', based on variable ONTOP_FILE_ENCODING (default: UTF-8)
rem - setting Java properties 'logback.configurationFile' and 'logging.config', based on variable ONTOP_LOG_CONFIG
rem   (default log/logback.xml)
rem - optionally setting Logback root logger level, via variables ONTOP_LOG_LEVEL (values: debug, info, etc...) or, if
rem   undefined, via legacy variable ONTOP_DEBUG (log level set to debug)
rem

rem Select Java executable leveraging JAVA_HOME
set "JAVA=java"
if exist "%JAVA_HOME%\bin\java.exe" set "JAVA=%JAVA_HOME%\bin\java"

rem Determine Ontop root directory
pushd "%~dp0"
set "ONTOP_HOME=%CD%"
popd

rem Inject -Xmx512m into ONTOP_JAVA_ARGS, if not set already
if "%ONTOP_JAVA_ARGS%" == "" (
  set "ONTOP_JAVA_ARGS=-Xmx512m"
)
if "%ONTOP_JAVA_ARGS:*-Xmx=%" == "%ONTOP_JAVA_ARGS%" (
  set "ONTOP_JAVA_ARGS=%ONTOP_JAVA_ARGS% -Xmx512m"
)

rem Warn ignoring of -Dfile.encoding, -Dlogback.configurationFile, -Dlogging.config if present in ONTOP_JAVA_ARGS
if not "%ONTOP_JAVA_ARGS:*-Dfile.encoding=%" == "%ONTOP_JAVA_ARGS%" (
  echo WARNING: Ignoring -Dfile.encoding in ONTOP_JAVA_ARGS. Use ONTOP_FILE_ENCODING instead. 1>&2
)
if not "%ONTOP_JAVA_ARGS:*-Dlogback.configurationFile=%" == "%ONTOP_JAVA_ARGS%" (
  echo WARNING: Ignoring -Dlogback.configurationFile in ONTOP_JAVA_ARGS. Use ONTOP_LOG_CONFIG instead. 1>&2
)
if not "%ONTOP_JAVA_ARGS:*-Dlogging.config=%" == "%ONTOP_JAVA_ARGS%" (
  echo WARNING: Ignoring -Dlogging.config in ONTOP_JAVA_ARGS. Use ONTOP_LOG_CONFIG instead. 1>&2
)

rem Assign ONTOP_LOG_LEVEL based on legacy ONTOP_DEBUG if the former is unset, or report a warning if both are set
if "%ONTOP_LOG_LEVEL%" == "" (
  if "%ONTOP_DEBUG%" == "" (
    set "ONTOP_LOG_LEVEL=info"
  ) else if "%ONTOP_DEBUG%" == "false" (
    set "ONTOP_LOG_LEVEL=info"
  ) else (
    set "ONTOP_LOG_LEVEL=debug"
  )
) else if not "%ONTOP_DEBUG%" == "" (
  echo WARNING: environment variable ONTOP_DEBUG ignored due to ONTOP_LOG_LEVEL being specified 1>&2
)

rem Assign ONTOP_FILE_ENCODING and ONTOP_LOG_CONFIG to their default values, if undefined
if "%ONTOP_FILE_ENCODING%" == "" ( set "ONTOP_FILE_ENCODING=UTF-8" )
if "%ONTOP_LOG_CONFIG%" == "" ( set "ONTOP_LOG_CONFIG=%ONTOP_HOME%\log\logback.xml" )

rem Run Ontop replacing the shell running this script so to proper handle signals
rem (note: ONTOP_JAVA_ARGS specified first and possibly overridden by following -D settings)
"%JAVA%" ^
  %ONTOP_JAVA_ARGS% ^
  "-Dfile.encoding=%ONTOP_FILE_ENCODING%" ^
  "-Dlogback.configurationFile=%ONTOP_LOG_CONFIG%" ^
  "-Dlogging.config=%ONTOP_LOG_CONFIG%" ^
  -cp "%ONTOP_HOME%\lib\*;%ONTOP_HOME%\jdbc\*" ^
  it.unibz.inf.ontop.cli.Ontop %*
