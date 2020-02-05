package com.kieronquinn.app.darq.root;

import android.annotation.SuppressLint;
import android.app.UiModeManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import com.kieronquinn.app.darq.BuildConfig;
import com.kieronquinn.app.darq.IDarqIPC;
import com.kieronquinn.app.darq.utils.ExtensionsKt;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import eu.chainfire.librootjava.RootIPC;
import eu.chainfire.librootjava.RootJava;
import eu.chainfire.libsuperuser.Shell;

import static com.kieronquinn.app.darq.utils.ExtensionsKt.KEY_FORCE_DARK;

public class DarqIPCReceiver {

    public static void main(String[] args) {

        IBinder ipc = new IDarqIPC.Stub() {
            @Override
            public void pokeServices() throws RemoteException {
                Log.d("SystemPropPoker", "pokeServices START");
                try {
                    Class serviceManagerClass = Class.forName("android.os.ServiceManager");
                    String[] services = (String[]) serviceManagerClass.getMethod("listServices").invoke(null);
                    if (services == null) {
                        Log.e("SystemPropPoker", "There are no services, how odd");
                        return;
                    }

                    int v4;
                    for (v4 = 0; v4 < services.length; ++v4) {
                        String v5 = services[v4];
                        IBinder v6 = (IBinder) serviceManagerClass.getMethod("checkService", String.class).invoke(null, v5);
                        if (v6 != null) {
                            Parcel v7 = Parcel.obtain();
                            try {
                                v6.transact(0x5F535052, v7, null, 0);
                                v7.recycle();
                                Log.i("SystemPropPoker", "Successfully poked service " + v5);
                            } catch (RemoteException unused_ex) {
                                v7.recycle();
                            } catch (Exception v6_1) {
                                Log.i("SystemPropPoker", "Someone wrote a bad service \'" + v5 + "\' that doesn\'t like to be poked", v6_1);
                            }
                        }
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void setForceDarkEnabled(boolean enabled) throws RemoteException {
                Log.d("SetProp", "setprop " + KEY_FORCE_DARK + " " + enabled);
                Shell.SH.run("setprop " + KEY_FORCE_DARK + " " + enabled);
                Log.d("DarQ", "isRoot " + Shell.SH.run("whoami") + "isOnePlus " + ExtensionsKt.isOnePlus() + " enabled " + enabled + " is dark theme " + isDarkTheme());
                if(ExtensionsKt.isOnePlus() && (!enabled || isDarkTheme())){
                    Shell.SH.run("settings put secure op_force_dark_entire_world " + (enabled ? "1" : "0"));
                    Shell.SH.run("settings put secure aosp_force_dark_mode " + (enabled ? "1" : "0"));
                }
            }

            @Override
            public void setDarkEnabled(boolean enabled) throws RemoteException {
                int state = enabled ? 2 : 1;
                Shell.SH.run("settings put secure ui_night_mode " + state);
                Context context = RootJava.getSystemContext();
                //Force a state change by enabling and immediately disabling car mode
                UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
                uiModeManager.enableCarMode(0);
                uiModeManager.disableCarMode(0);
            }

            @Override
            public boolean isForceDarkEnabled() throws RemoteException {
                return ExtensionsKt.getIsForceDarkTheme();
            }

            @Override
            public void forceStopApp(String app) throws RemoteException {
                Log.d("DarqR", "Force stop " + app);
                Shell.SH.run("am force-stop " + app);
            }

            @Override
            public void hookService() throws RemoteException {
                /* Below is some broken code for attempting to change the status bar to be in LIGHT mode
                  (it doesn't work).


                try {

                    Log.d("DarqR", "Status bar start");

                    Class IStatusBarStub = Class.forName("com.android.internal.statusbar.IStatusBarService$Stub");
                    Class IStatusBarProxy = Class.forName("com.android.internal.statusbar.IStatusBarService$Stub$Proxy");
                    Field sDefaultImpl = IStatusBarProxy.getDeclaredField("sDefaultImpl");
                    Object IStatusBarStatic = sDefaultImpl.get(null);
                    Log.d("DarqR", "IStatusBarStatic " + IStatusBarStatic);
                    Class IStatusBar = Class.forName("com.android.internal.statusbar.IStatusBarService");
                    Method asInterface = IStatusBarStub.getDeclaredMethod("asInterface", IBinder.class);
                    Class ServiceManager = Class.forName("android.os.ServiceManager");
                    Object statusBarService = ServiceManager.getMethod("getService", String.class).invoke(null, "statusbar");
                    Object StatusBar = asInterface.invoke(IStatusBarStub, statusBarService);
                    Method setSystemUiVisibility = IStatusBar.getDeclaredMethod("setSystemUiVisibility", int.class, int.class, int.class, String.class);
                    //setSystemUiVisibility.invoke(StatusBar, 0, View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR, View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR, "");
                    /*for(Method method : IStatusBar.getDeclaredMethods()){
                        Log.d("DarqR", method.getName() + " " + method.getParameterTypes());
                        int x = 0;
                        for(Class cls : method.getParameterTypes()){
                            Log.d("DarqR", "--> " + cls.getName() + " " + x);
                            x++;
                        }
                    }
                    //setSystemUiVisibility(0, View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR, View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR, View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR, View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR, new Rect(), new Rect(), true);
                    //Context context = RootJava.getSystemContext();
                    Log.d("DarqR", "Status bar found");
                } catch (ClassNotFoundException e) {
                    Log.d("DarqR", "Status bar class not found");
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    Log.d("DarqR", "Status bar method not found " + Log.getStackTraceString(e));
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    Log.d("DarqR", "invoke illegal");
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    Log.d("DarqR", "no such field " + Log.getStackTraceString(e));
                    e.printStackTrace();
                }
                //Object statusbarService = android.server.status.IStatusBar.Stub.asInterface(sm.getService("statusbar"));*/

            }

            @Override
            public boolean isRoot(){
                return ExtensionsKt.getUID() == 0;
            }

            @Override
            public String getUid(){
                String uidString = Shell.SH.run("whoami").toString();
                Log.d("DarQ", "Running as " + uidString);
                return uidString;
            }
        };

        Log.d("SystemPropPoker", "Darq start");

        try {
            new RootIPC(BuildConfig.APPLICATION_ID, ipc, 0, 30 * 1000, true);
        } catch (RootIPC.TimeoutException e) {
            Log.d("SystemPropPoker", "Unable to connect to IPC: timeout");
        }

    }

    public static boolean isDarkTheme(){
        return Shell.SH.run("settings get secure ui_night_mode").get(0).equals("2");
    }

    public static void setSystemUiVisibility(int displayId, int vis, int fullscreenStackVis, int dockedStackVis, int mask, Rect fullscreenBounds, Rect dockedBounds, boolean navbarColorManagedByIme) throws RemoteException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Parcel _data = Parcel.obtain();
        try {
            _data.writeInterfaceToken("com.android.internal.statusbar.IStatusBar");
        }
        catch(Throwable v0) {
            _data.recycle();
            throw v0;
        }

        try {
            _data.writeInt(displayId);
        }
        catch(Throwable v0) {
            _data.recycle();
            throw v0;
        }

        try {
            _data.writeInt(vis);
        }
        catch(Throwable v0) {
            _data.recycle();
            throw v0;
        }

        try {
            _data.writeInt(fullscreenStackVis);
            _data.writeInt(dockedStackVis);
            _data.writeInt(mask);
            int v1 = 0;
            if(fullscreenBounds == null) {
                _data.writeInt(0);
            }
            else {
                _data.writeInt(1);
                fullscreenBounds.writeToParcel(_data, 0);
            }

            if(dockedBounds == null) {
                _data.writeInt(0);
            }
            else {
                _data.writeInt(1);
                dockedBounds.writeToParcel(_data, 0);
            }

            if(navbarColorManagedByIme) {
                v1 = 1;
            }

            _data.writeInt(v1);
            //if(!this.mRemote.transact(9, _data, null, 1) && Stub.getDefaultImpl() != null) {
                Class IStatusBarStub = Class.forName("com.android.internal.statusbar.IStatusBar$Stub");
                Class IStatusBar = Class.forName("com.android.internal.statusbar.IStatusBar");
                @SuppressLint("BlockedPrivateApi")
                Object Stub = IStatusBarStub.getDeclaredMethod("getDefaultImpl").invoke(null);
                IStatusBar.getDeclaredMethod("setSystemUiVisibility", int.class, int.class, int.class, int.class, int.class, Rect.class, Rect.class, boolean.class).invoke(Stub, displayId, vis, fullscreenStackVis, dockedStackVis, mask, fullscreenBounds, dockedBounds, navbarColorManagedByIme);
                _data.recycle();
                return;
            //}
        }
        catch(Throwable v0){
            _data.recycle();
            throw v0;
        }
    }

    public static String getLaunchScript(Context context) {
        try {
            String path = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).sourceDir;
            return "su -c 'CLASSPATH="+path+" app_process /system/bin com.kieronquinn.app.darq.root.DarqIPCReceiver'";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    @NotNull
    public static List<String> getLaunchShellScript(@NotNull Context context) {
        return RootJava.getLaunchScript(
                context,
                DarqIPCReceiver.class,
                null,
                null,
                null,
                BuildConfig.APPLICATION_ID + ":root"
        );
    }
}
