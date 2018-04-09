@echo off

rem Paths to tools
set TOOLS=C:\Programme\JavaTools
set ANT_HOME=%TOOLS%\apache-ant-1.10.3


rem Options
set ANT_OPTS=-Xmx512m 

rem Paths
set BUILD=build
set CHECKOUT=.

%ANT_HOME%\bin\ant -Dbuild=%BUILD% -Dcheckout=%CHECKOUT%
pause