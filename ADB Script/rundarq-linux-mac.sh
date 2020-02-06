scriptpath="/sdcard/Android/data/com.kieronquinn.app.darq/files/script.sh"

function header(){
	printf " ____              ___  \n"
	printf "|  _ \  __ _ _ __ / _ \ \n"
	printf "| | | |/ _\` | '__| | | |\n"
	printf "| |_| | (_| | |  | |_| |\n"
	printf "|____/ \__,_|_|   \__\_\\ \n\n"
	printf "DarQ Linux/Mac running script v1.0\n\n"
}

function divider(){
	printf "*****************\n"
}

function pause(){
	read -p "Press enter to $1"
}

function runScript(){
	printf "Running script...\n"
	adb shell "sh $scriptpath 2>/dev/null 1>/dev/null &"
	printf "Starting DarQ...\n"
	if isVerbose; then
		adb shell "am start com.kieronquinn.app.darq/.activities.MainActivity"
	else
		adb shell "am start com.kieronquinn.app.darq/.activities.MainActivity" > /dev/null 2> /dev/null
	fi
	divider
	printf "DarQ should now be running. You may disconnect and continue on your device now.\n"
	printf "REMINDER: DarQ needs to be run from this script *every time your device is\nrestarted*. It will not work otherwise.\n"
	divider
	pause "quit"
}

function checkScriptExists(){
	printf "Checking for script...\n"
	if runCommandExitCode "adb shell ls $scriptpath"; then
		printf "Script found\n"
		runScript
	else
		divider
		printf "Script not found. Please run DarQ on your device to generate it first,\nthen re-run this script\n"
		divider
		pause "quit"
	fi
}

function checkDevices(){
	printf "Checking connected devices...\n"
	if runCommandAndCheckMultiple "adb devices" "device" "-w"; then
		printf "Device found\n"
		checkScriptExists
	else
		if runCommandAndCheck "adb devices" "device" "-w"; then
			printf "Multiple devices found, please disconnect all but the device you wish to use\nDarQ on, then run the script again\n"
			pause "quit"
		else
			printf "No devices found. Please make sure your device is connected, USB debugging is\nenabled and that they are authorised (check screen for prompt), then run the script again\n"
			pause "quit"
		fi
	fi
}

function checkAdb(){
	printf "Checking ADB...\n"
	if runCommandAndCheck "which adb" "adb"; then
	   printf "ADB found\n"
	   checkDevices
	else
		divider
		printf "You do not have ADB installed. Please follow the steps here to install it:\nhttps://www.xda-developers.com/install-adb-windows-macos-linux/\n"
		divider
	fi
}

function runCommandAndCheckMultiple(){
	output=$($1 | grep $2 -c $3)
	[ $output == 1 ]
}

function runCommandAndCheck(){
	if isVerbose; then
		$1 | grep $2 $3 &
	else
		$1 | grep $2 $3 &> /dev/null
	fi
	[ $? == 0 ]
}

function runCommandExitCode(){
	if isVerbose; then
		$1 &> /dev/null
	else 
		$1 &
	fi
	[ $? == 0 ]
}

function isVerbose(){
	[[ $_V -eq 1 ]]
}

header

checkAdb