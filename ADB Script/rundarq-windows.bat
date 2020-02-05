@ECHO OFF
cls
set NLM=^


set NL=^^^%NLM%%NLM%^%NLM%%NLM%

SET scriptpath=/sdcard/Android/data/com.kieronquinn.app.darq/files/script.sh
SET divider=****************
SET adbpath=tools\adb.exe

ECHO   _____              ____  
ECHO  ^|  __ \            / __ \ 
ECHO  ^| ^|  ^| ^| __ _ _ __^| ^|  ^| ^|
ECHO  ^| ^|  ^| ^|/ _` ^| '__^| ^|  ^| ^|
ECHO  ^| ^|__^| ^| (_^| ^| ^|  ^| ^|__^| ^|
ECHO  ^|_____/ \__,_^|_^|   \___\_\

echo %NL%DarQ Windows runner script v1.0

if not exist %adbpath% (
	echo %NL%%divider%
	echo Unable to find adb. Please make sure you extracted the full zip file including the tools folder containing adb.exe%NL%before running this script.
	echo %divider%
	pause
	EXIT /B 0
)

echo %NL%Checking connected devices...

%adbpath% devices > devices.txt
if "%1"=="-v" (
	type devices.txt
)
findstr /c:"device" devices.txt | find /c /v "" > temp.txt
set /p devicecount=<temp.txt
del temp.txt
del devices.txt

IF "%devicecount%"=="1" (
	echo %divider%
    ECHO No devices found. Please make sure your device is connected USB debugging is enabled, and that they are authorised%NL%^(check screen for prompt^), then run the script again
	echo %divider%
	pause
	EXIT /B 0
)
IF "%devicecount%" NEQ "2" (
	echo %divider%
    ECHO Multiple devices found, please disconnect all but the device you wish to use DarQ on, then run the script again
	echo %divider%
	pause
	EXIT /B 0
)

echo Device found
echo Checking for script...

if "%1"=="-v" (
	%adbpath% shell ls %scriptpath%
) ELSE (
	%adbpath% shell ls %scriptpath%>nul 2>nul
)

IF %ERRORLEVEL% NEQ 0 (
	echo %divider%
	ECHO Script not found. Please run DarQ on your device to generate it first, then re-run this script
	echo %divider%
	pause
	EXIT /B 0
)

echo Script found
echo Running script...
%adbpath% shell "nohup sh %scriptpath% 2>/dev/null 1>/dev/null &"
if "%1"=="-v" (
	%adbpath% shell "am start com.kieronquinn.app.darq/.activities.MainActivity"
) ELSE (
	%adbpath% shell "am start com.kieronquinn.app.darq/.activities.MainActivity">nul 2>nul
)
echo %divider%
echo DarQ should now be running. You may disconnect and continue on your device now.
echo REMINDER: DarQ needs to be run from this script *every time your device is restarted*. It will not work otherwise.
echo %divider%

pause
exit /B 0

