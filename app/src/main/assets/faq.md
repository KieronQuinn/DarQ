## Why does DarQ only work on Android Q?
Force dark was only added as a (hidden) option on Android Q. If you want to change the theme of apps prior to Q, you may wish to use a Substratum theme.

## Why does DarQ need root?
Since Android Q beta 4, the force dark property (`persist.hwui.force_dark`) is only writable with root. Previously, it was possible to change it with ADB without root, but that is no longer possible. DarQ needs root to change this property, and also uses it to enable and disable the system dark theme without opening the Settings app.

## Why does DarQ use an Accessibility Service?
Accessibility services are the quickest and most compatible way to monitor when an app opens and closes, without using hacks like reading the `logcat` (which obviously doesn't work if the device has logs disabled either). If you are worried about DarQ doing anything nefarious using the service, feel free to check the code out on GitHub - and compile the app yourself if you wish - to see what it's doing for yourself.

## Why does DarQ/force dark need the system dark theme to be enabled?
For some reason, in Android Q beta 4, a requirement was added to force dark to make it only work when the system dark theme is enabled. A workaround has not been found (and may not even exist) for this, so it is required for DarQ to work too.

## Force Dark makes an app look weird! Can you fix it?
Short answer: No.

Long answer: Force dark is able to invert light colours to dark, and dark text and icons to light, but is not perfect. Gradients are often broken (such as in Facebook and Facebook Messenger), and sometimes regular images get caught in the crossfire and are inverted too, making them look incorrect. As force dark is literally just a property for DarQ to change, there's no way to configure or tweak its sensitivity and so apps can't be fixed. In a way, that's why DarQ even exists - so you can enable force dark on the apps that _do_ work, and leave it disabled for those that don't, or that already have a dark theme.

**Please do not pester app developers to fix apps with force dark enabled. Force dark is a not normally meant as a user-facing tool, it is meant for developers. If enough developers complain to Google about users pestering for fixes with force dark enabled, it may be removed from a future release of Android, preventing DarQ from working at all.**

## Force Dark doesn't work at all on an app! Can you fix it?
Sometimes an app launches too quickly for force dark to be applied. You may have luck closing and reopening the app, or opening and closing recent apps. Some apps, such as Google Opinion Rewards, have very basic layouts, and so load the initial page before force dark can be enabled, but any submenus have the dark theme applied. It may be possible to improve the detection and applying time in the future to provide a better experience for these apps, so please be patient and check back for future updates.

## Which apps look good when force dark is enabled?
During testing; Snapchat, the Google Play Store and Instagram were found to be usable with force dark enabled. Plenty more will work too, it's up to you to experiment and see what works for you.

## Why does the status bar invert (have black icons) when force dark is enabled? Can it be fixed?
This appears to be a bug in Android, and as force dark is a developer option, may not be fixed. It may be possible to fix it with DarQ, and this is being investigated

## Can you change the force dark colour in an app (eg. to full black)?
No. There is no customisation with force dark, so you cannot change the colours.

## How can I keep DarQ up to date? Is DarQ available on the Play Store?
DarQ is not on the Play Store, as it uses an accessibility service for non-accessibility purposes, which Google does not like. DarQ is instead available on XDA Labs, so please use the XDA Labs app to keep the app up to date. You can quickly open the XDA Labs listing by tapping the "About" setting on the main screen of DarQ.