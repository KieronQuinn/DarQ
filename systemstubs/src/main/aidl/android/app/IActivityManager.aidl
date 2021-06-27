// IActivityManager.aidl
package android.app;

import android.app.IProcessObserver;
import android.app.ActivityManager;

interface IActivityManager {

    void registerProcessObserver(in IProcessObserver observer);
    void unregisterProcessObserver(in IProcessObserver observer);
    List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses();

}