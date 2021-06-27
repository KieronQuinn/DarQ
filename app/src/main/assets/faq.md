## Why does DarQ only work on Android 10 and above?
Force dark was only added as a (hidden) option on Android 10 and above. If you want to change the theme of apps prior to Android 10, you may wish to use a Substratum theme.

## Why does DarQ need root or Shizuku/ADB access?
Since the final beta of Android 10, the force dark property (`debug.hwui.force_dark`) is only writable with root or with the `shell` UID (which `adb shell` and therefore Shizuku runs with).

## How does DarQ monitor running apps?
DarQ uses a hidden API only accessible to privileged code (`IActivityManager.registerProcessObserver`), which allows it to know when an app is opened or closed without an Accessibility Service.

## Why does DarQ/force dark need the system dark theme to be enabled?
For some reason, in the final beta of Android 10, a requirement was added to force dark to make it only work when the system dark theme is enabled. A workaround has not been found (and may not even exist) for this, so it is required for DarQ to work too.

## Force Dark makes an app look weird! Can you fix it?
Short answer: No.

Long answer: Force dark is able to invert light colours to dark, and dark text and icons to light, but is not perfect. Gradients are often broken (such as in Facebook and Facebook Messenger), and sometimes regular images get caught in the crossfire and are inverted too, making them look incorrect. As force dark is literally just a property for DarQ to change, there's no way to configure or tweak its sensitivity and so apps can't be fixed. In a way, that's why DarQ even exists - so you can enable force dark on the apps that _do_ work, and leave it disabled for those that don't, or that already have a dark theme.

**Please do not pester app developers to fix apps with force dark enabled. Force dark is a not normally meant as a user-facing tool, it is meant for developers. If enough developers complain to Google about users pestering for fixes with force dark enabled, it may be removed from a future release of Android, preventing DarQ from working at all.**

## Force Dark doesn't work at all on an app! Can you fix it?
Sometimes an app launches too quickly for force dark to be applied. You may have luck closing and reopening the app, or opening and closing recent apps.

## Which apps look good when force dark is enabled?
During testing; LinkedIn, Facebook and Google Opinion Rewards were found to be usable with force dark enabled. Plenty more will work too, it's up to you to experiment and see what works.

Please note that some of the above require the Xposed module to be enabled for them to work.

## Can Force Dark be made to work on all apps without Xposed?
No. Apps are able to disable force dark in code, so Xposed is the only way to prevent that.

## Why does the status bar invert (have black icons) when force dark is enabled? Can it be fixed?
This appears to be a bug in Android, and as force dark is a developer option, may not be fixed. It may be possible to fix it with DarQ, and this is being investigated

## Can you change the force dark colour in an app (eg. to full black)?
No. There is no customisation with force dark, so you cannot change the colours.

## How can I keep DarQ up to date? Is DarQ available on the Play Store?
DarQ is not on the Play Store, as it uses hidden APIs, which Google does not like. DarQ is instead available on GitHub, and will automatically check for updates when launched. If you would like to check manually, use the GitHub link on the main page of DarQ, and follow the "Releases" link.
