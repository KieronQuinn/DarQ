English | [简体中文](./non-root-faq.zh_Hans.md)

DarQ is able to run without root, as long as you run a script from your computer using ADB every time it restarts. Once the script is run, DarQ should function until the next reboot. 

You can find the latest script to run from a Windows, Linux or Mac computer on the [XDA thread](https://forum.xda-developers.com/android/apps-games/app-darq-app-selectable-force-dark-t3944356)

# Steps to run: 

1.) Download the script from the XDA post. It will be a ZIP archive, containing two files and a folder. 

2.) Extract the ZIP *fully* to somewhere you'll easily find it. Remember, you'll be running this as often as your device restarts.

3.) Run the correct script for your operating system. 

	For Windows, simply double click "rundarq-windows.bat". 
	
	For Linux and MacOS, you may need to open a terminal in the directory containing the scripts, and run "sh startdarq-linux-mac.sh"
	
	Note: On Linux and Mac, "adb" is not included. If you do not have it installed, the script will link you to a guide to do so.
		
4.) Make sure the script runs correctly. If it shows an error, it may advise you on what to do. If you find an error that isn't handled correctly, please screenshot while using the -v (verbose) switch and post it on the XDA thread

## Note:
As well as if your device is rebooted, the script will need to be re-run if DarQ is force stopped for whatever reason, including if the system kills the background code. This includes toggling the accessibility service (which force stops the app in the process).

# FAQ:

## Why does DarQ require running a script from a computer when not using a rooted phone?
The property that force dark uses requires either the `shell` or `root` UID to be changed, or it will throw an error. Without access to `root`, `shell` is needed, which is what ADB runs as.
Therefore, DarQ must have code running on the device in the background, started from ADB, that will allow the property to be changed.

## Why does DarQ running as non-root require to be run after reboot?
The background script required by DarQ will get killed when the device is rebooted. Therefore, it must be run again every time.

## Does my device need to stay connected to my computer for DarQ to work?
No. DarQ will continue working as long as the script is run successfully and the device is not rebooted.

## For more questions and answers, please refer to the main FAQ
