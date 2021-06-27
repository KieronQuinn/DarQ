// ILocationManager.aidl
package android.location;

import android.location.Location;
import android.location.LocationRequest;

interface ILocationManager {

    //Android 11+
    Location getLastLocation(in LocationRequest request, String packageName, String featureId);

    //Android 10
    //Location getLastLocation(in LocationRequest request, String packageName) = 2;

}