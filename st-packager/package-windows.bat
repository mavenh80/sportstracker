SET ST_PROJECT_DIR=..\sportstracker
SET BUILD_DIR=.\build
SET PACKAGE_DIR=.\package

SET ST_VERSION=8.0.0
REM Packager fails, when package version contains any suffixes, e.g. -SNAPSHOT
SET ST_PACKAGE_VERSION=8.0.0

# cleanup
RMDIR /S/Q %BUILD_DIR%
RMDIR /S/Q %PACKAGE_DIR%

REM copy SportsTracker files and documentation
MKDIR %BUILD_DIR%
XCOPY %ST_PROJECT_DIR%\target\sportstracker-%ST_VERSION%.jar %BUILD_DIR%
XCOPY /SY %ST_PROJECT_DIR%\target\lib %BUILD_DIR%\lib\
XCOPY /SY %ST_PROJECT_DIR%\docs %BUILD_DIR%\docs\

REM use type app-image, exe or msi depending on the needed package type
REM (WiX 3.0 Installer is needed for types exe and msi, needs to be in PATH)
%JAVA_HOME%\bin\jpackage ^
    --verbose ^
    --type exe ^
    --input %BUILD_DIR% ^
    --main-jar ./sportstracker-%ST_VERSION%.jar ^
    --dest %PACKAGE_DIR% ^
    --name SportsTracker ^
    --app-version %ST_PACKAGE_VERSION% ^
    --vendor "Saring.de" ^
    --copyright "(C) 2023 Stefan Saring" ^
    --description "Application for tracking your sporting activities." ^
    --icon ./icons/windows/SportsTracker.ico ^
    --license-file %BUILD_DIR%/docs/LICENSE.txt ^
    --win-menu ^
    --module-path %JAVA_HOME%/jmods ^
    --add-modules java.base,java.desktop,java.logging,java.net.http,java.scripting,java.sql,java.xml,jdk.crypto.ec,jdk.localedata,jdk.jsobject,jdk.unsupported

REM delete temporary build directory
RMDIR /S/Q %BUILD_DIR%
