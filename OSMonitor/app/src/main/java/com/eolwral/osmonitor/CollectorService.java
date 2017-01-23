package com.eolwral.osmonitor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.util.Log;

import com.eolwral.osmonitor.ipc.IpcService;
import com.eolwral.osmonitor.ipc.IpcService.ipcClientListener;
import com.eolwral.osmonitor.ipc.ipcCategory;

public class CollectorService extends Service implements ipcClientListener{
    private IpcService ipcService = IpcService.getInstance();
    private int updateInterval = 2;
    public CollectorService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        byte newCommand[] = { ipcCategory.OS, ipcCategory.PROCESS };
        ipcService.addRequest(newCommand, 0, this);
    }


    @Override
    public void onDestroy() {
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
       // throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onRecvData(byte[] result) {
        byte newCommand[] = { ipcCategory.OS, ipcCategory.PROCESS };
        ipcService.addRequest(newCommand, updateInterval, this);
    }
}
