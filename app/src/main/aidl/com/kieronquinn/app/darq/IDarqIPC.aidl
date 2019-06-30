// IDarqIPC.aidl
package com.kieronquinn.app.darq;

interface IDarqIPC {
    void pokeServices();

    void setForceDarkEnabled(boolean enabled);

    void setDarkEnabled(boolean enabled);

    boolean isForceDarkEnabled();

    void forceStopApp(String app);

    void hookService();
}
