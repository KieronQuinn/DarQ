// IUiModeManager.aidl
package android.app;

interface IUiModeManager {

    /**
     * Sets the night mode.
     * The mode can be one of:
     *   1 - notnight mode
     *   2 - night mode
     *   3 - automatic mode switching
     */
    void setNightMode(int mode);

}