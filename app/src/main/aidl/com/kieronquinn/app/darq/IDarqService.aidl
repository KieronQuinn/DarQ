// IDarqService.aidl
package com.kieronquinn.app.darq;

import com.kieronquinn.app.darq.model.settings.IPCSetting;
import com.kieronquinn.app.darq.model.location.LatLng;

interface IDarqService {
    void destroy() = 16777114;
    void ping() = 0;
    void onBind() = 1;
    void killOtherInstances() = 2;
    void setupSettings(in IPCSetting ipcSetting) = 3;
    void setupWhitelist(boolean isStart, boolean isEnd, String packageName) = 4;
    void notifySettingsChange(in IPCSetting ipcSetting) = 5;
    void setNightMode(boolean nightMode) = 6;
    LatLng getLocation() = 7;
    String getServiceType() = 8;
}