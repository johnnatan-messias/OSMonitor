package com.eolwral.osmonitor;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Debug;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.eolwral.osmonitor.core.cpuInfo;
import com.eolwral.osmonitor.core.cpuInfoList;
import com.eolwral.osmonitor.core.networkInfo;
import com.eolwral.osmonitor.core.networkInfoList;
import com.eolwral.osmonitor.core.osInfo;
import com.eolwral.osmonitor.core.processInfo;
import com.eolwral.osmonitor.core.processInfoList;
import com.eolwral.osmonitor.core.processStatus;
import com.eolwral.osmonitor.ipc.IpcService;
import com.eolwral.osmonitor.ipc.IpcService.ipcClientListener;
import com.eolwral.osmonitor.ipc.ipcCategory;
import com.eolwral.osmonitor.ipc.ipcData;
import com.eolwral.osmonitor.ipc.ipcMessage;
import com.eolwral.osmonitor.settings.Settings;
import com.eolwral.osmonitor.util.ProcessUtil;
import com.eolwral.osmonitor.util.UserInterfaceUtil;
import com.google.flatbuffers.FlatBufferBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class OSMonitorCollectorService extends Service implements ipcClientListener {
    private static String TAG = OSMonitorCollectorService.class.getSimpleName();
    private String pathFile = Environment.getExternalStorageDirectory().getPath() + File.separator + "OSMonitor" + File.separator;
    private PrintWriter outFile;
    private IpcService ipcService = null;
    private int UpdateInterval = 1;

    private int packageIndex;
    private String packageName = "com.monitorlog";

    private boolean isRegistered = false;
    private FlatBufferBuilder sysProcess = null;
    private ArrayList<processInfo> data = new ArrayList<processInfo>();

    // process
    //private boolean isSetTop = false;
    private float totalCpuUsage = 0;
    private float cpuUsage = 0;
    private float ioWaitUsage = 0;
    private float[] topUsage = new float[3];
    private String[] topProcess = new String[3];

    // memory
    private long memoryTotal = 0;
    private long memoryFreeTotal = 0;
    private long memoryCachedTotal = 0;
    private long memoryBufferedTotal = 0;

    // battery
    private boolean useCelsius = false;
    private int battLevel = 0; // percentage value or -1 for unknown
    private int temperature = 0;

    // network
    private long trafficOut = 0;
    private long trafficIn = 0;

    // private
    private ProcessUtil infoHelper = null;
    private Settings settings = null;
    private boolean isRegisterBattery = false;
    private BroadcastReceiver battReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int rawlevel = intent.getIntExtra("level", -1);
            int scale = intent.getIntExtra("scale", -1);

            temperature = intent.getIntExtra("temperature", -1);

            if (rawlevel >= 0 && scale > 0) {
                battLevel = (rawlevel * 100) / scale;
            }
        }
    };
    private BroadcastReceiver mReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        super.onCreate();

        settings = Settings.getInstance(this);

        infoHelper = ProcessUtil.getInstance(this, true);

        IpcService.Initialize(this);
        ipcService = IpcService.getInstance();

        refreshSettings();

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "Received");
                if (intent.getAction().equals("startOSMonitor")) {
                    Log.i(TAG, "Service is Running...");
                    openFile();
                    initService();
                }else if(intent.getAction().equals("stopOSMonitor")){
                    endService();
                }
                else if (intent.getAction().equals("runrunrun")){
                    outFile.write("run\n");
                }
            }
        };
        registerReceiver(mReceiver, new IntentFilter("startOSMonitor"));
        registerReceiver(mReceiver, new IntentFilter("stopOSMonitor"));
        registerReceiver(mReceiver, new IntentFilter("runrunrun"));
        isRegistered = true;

        Log.i(TAG, "Service created");
    }

    private void refreshSettings() {
        useCelsius = settings.isUseCelsius();
        // recreate connection type when refreshing settings
        ipcService.createConnection();
    }

    @Override
    public void onDestroy() {
        endService();
       // if (!settings.getSessionValue().equals("Non-Exit"))
        //android.os.Process.killProcess(android.os.Process.myPid());
        if(outFile != null)
            outFile.close();
        super.onDestroy();
        Log.i(TAG, "Service Destroyed");
    }

    private void openFile() {
        File file = new File(pathFile);
        if (!file.exists())
            file.mkdirs();
        try {
            outFile = new PrintWriter(new File(file.getPath() + File.separator + "OSMonitorLog_" +  getTimestamp() + ".txt"));
            outFile.write("memoryTotal\tmemoryFreeTotal\tmemoryCachedTotal\tmemoryBufferedTotal\t" +
                    "battTemperature\tbatteryLevel\ttotalCpuUsage\tcpuUsage\tstartTime\tcpuTime\t" +
                    "usedSystemTime\tppid\tusedUserTime\tstatus\trss\ttotalPss\ttotalPrivateDirty\n");
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public String getTimestamp() {
        Date date = new Date();
        String dateTimestamp = String.valueOf(date.getTime() / 1000);
        Log.i(TAG, "getTimestamp() = " + dateTimestamp);
        return dateTimestamp;
    }

    private void initService() {
        if (!isRegistered) {
            //registerScreenEvent();
            isRegistered = true;
        }
        wakeUp();
    }

    private void endService() {
        if (isRegistered) {
            unregisterReceiver(mReceiver);
            isRegistered = false;
        }

        goSleep();
    }

    private byte[] getReceiveDataType() {
        byte newCommand[] = {ipcCategory.PROCESS, ipcCategory.CPU, ipcCategory.OS};
        newCommand = new byte[]{ipcCategory.PROCESS, ipcCategory.CPU, ipcCategory.OS, ipcCategory.NETWORK};
        return newCommand;
    }

    private void wakeUp() {
        UpdateInterval = settings.getInterval();
        byte newCommand[] = getReceiveDataType();
        ipcService.removeRequest(this);
        ipcService.addRequest(newCommand, 0, this);
        startBatteryMonitor();
    }

    private void goSleep() {
        ipcService.removeRequest(this);
        stopBatteryMonitor();
    }

    private void startBatteryMonitor() {
        if (!isRegisterBattery) {
            IntentFilter battFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            registerReceiver(battReceiver, battFilter);
            isRegisterBattery = true;
        }
    }

    private void stopBatteryMonitor() {
        if (isRegisterBattery) {
            unregisterReceiver(battReceiver);
            isRegisterBattery = false;
        }
    }

    @Override
    public void onRecvData(byte[] result) {
        if (result == null) {
            byte newCommand[] = getReceiveDataType();
            ipcService.addRequest(newCommand, UpdateInterval, this);
            return;
        }
        // clean up
        while (!data.isEmpty())
            data.remove(0);
        data.clear();

        // gather data
        cpuUsage = 0;

        // empty
        for (int index = 0; index < 3; index++) {
            topUsage[index] = 0;
            topProcess[index] = "";
        }

        try {
            ipcMessage ipcMessageResult = ipcMessage.getRootAsipcMessage(ByteBuffer.wrap(result));
            for (int index = 0; index < ipcMessageResult.dataLength(); index++) {
                ipcData rawData = ipcMessageResult.data(index);

                if (rawData.category() == ipcCategory.OS)
                    extractOSInfo(rawData);
                else if (rawData.category() == ipcCategory.CPU)
                    extractCPUInfo(rawData);
                else if (rawData.category() == ipcCategory.NETWORK)
                    extractNetworkInfo(rawData);
                else if (rawData.category() == ipcCategory.PROCESS)
                    extractProcessInfo(rawData);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.getStackTrace();
        }
        totalCpuUsage = 0;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).name().compareTo(packageName) == 0) {
                packageIndex = i;
            }
            totalCpuUsage += data.get(i).cpuUsage();
        }
        // refreshNotification();
        saveFile();

        // send command again
        byte newCommand[] = getReceiveDataType();
        ipcService.addRequest(newCommand, UpdateInterval, this);
    }

    private void extractProcessInfo(ipcData rawData) {
        // summary value
        float cpuUsage = 0;
        long rss = 0;
        long vsz = 0;
        long startTime = 0;
        int threadCount = 0;
        long usedSystemTime = 0;
        long usedUserTime = 0;
        long cpuTime = 0;

        // process processInfo
        processInfoList psInfoList = processInfoList.getRootAsprocessInfoList(rawData.payloadAsByteBuffer().asReadOnlyBuffer());
        for (int count = 0; count < psInfoList.listLength(); count++) {
            processInfo psInfo = psInfoList.list(count);

            boolean doMerge = false;

            if (psInfo.uid() == 0 || psInfo.name().contains("/system/") || psInfo.name().contains("/sbin/"))
                doMerge = true;

            if (psInfo.name().toLowerCase(Locale.getDefault()).contains("osmcore"))
                doMerge = false;

            if (settings.isUseExpertMode())
                doMerge = false;

            // Don't merge data
            if (doMerge == false) {
                data.add(psInfo);
                continue;
            }

            // Merge process information into a process
            cpuUsage += psInfo.cpuUsage();
            rss += psInfo.rss();
            vsz += psInfo.vsz();
            threadCount += psInfo.threadCount();
            usedSystemTime += psInfo.usedSystemTime();
            usedUserTime += psInfo.usedUserTime();
            cpuTime += psInfo.cpuTime();

            if (startTime < psInfo.startTime() || startTime == 0)
                startTime = psInfo.startTime();

        }

        if (!settings.isUseExpertMode()) {

            // summary all system processes
            sysProcess = new FlatBufferBuilder(0);

            int[] sysProcessArray = new int[1];
            int sysProcessName = sysProcess.createString("System");
            int sysProcessOwner = sysProcess.createString("root");

            processInfo.startprocessInfo(sysProcess);
            processInfo.addPid(sysProcess, 0);
            processInfo.addUid(sysProcess, 0);
            processInfo.addPpid(sysProcess, 0);
            processInfo.addName(sysProcess, sysProcessName);
            processInfo.addOwner(sysProcess, sysProcessOwner);
            processInfo.addPriorityLevel(sysProcess, 0);
            processInfo.addStatus(sysProcess, processStatus.Running);
            processInfo.addCpuUsage(sysProcess, cpuUsage);
            processInfo.addRss(sysProcess, rss);
            processInfo.addVsz(sysProcess, vsz);
            processInfo.addStartTime(sysProcess, startTime);
            processInfo.addThreadCount(sysProcess, threadCount);
            processInfo.addUsedSystemTime(sysProcess, usedSystemTime);
            processInfo.addUsedUserTime(sysProcess, usedUserTime);
            processInfo.addCpuTime(sysProcess, cpuTime);

            sysProcessArray[0] = processInfo.endprocessInfo(sysProcess);

            int sysProcessVector = processInfoList.createListVector(sysProcess, sysProcessArray);
            int sysProcessList = processInfoList.createprocessInfoList(sysProcess, sysProcessVector);
            processInfoList.finishprocessInfoListBuffer(sysProcess, sysProcessList);

            processInfoList sysProcessInfoList = processInfoList.getRootAsprocessInfoList(sysProcess.dataBuffer());

            data.add(sysProcessInfoList.list(0));
        }
    }

    private void extractNetworkInfo(ipcData rawData) {
        // process processInfo
        trafficOut = 0;
        trafficIn = 0;
        networkInfoList nwInfoList = networkInfoList.getRootAsnetworkInfoList(rawData.payloadAsByteBuffer().asReadOnlyBuffer());
        for (int count = 0; count < nwInfoList.listLength(); count++) {
            networkInfo nwInfo = nwInfoList.list(count);
            trafficOut += nwInfo.transUsage();
            trafficIn += nwInfo.recvUsage();
        }
    }

    private void extractCPUInfo(ipcData rawData) {
        cpuInfoList infoList = cpuInfoList.getRootAscpuInfoList(rawData.payloadAsByteBuffer().asReadOnlyBuffer());
        for (int count = 0; count < infoList.listLength(); count++) {
            cpuInfo info = infoList.list(count);
            ioWaitUsage = info.ioUtilization();
            break;
        }
    }

    private void extractOSInfo(ipcData rawData) {
        osInfo info = osInfo.getRootAsosInfo(rawData.payloadAsByteBuffer().asReadOnlyBuffer());
        memoryFreeTotal = info.freeMemory(); //+ info.bufferedMemory() + info.cachedMemory();
        memoryTotal = info.totalMemory();
        memoryCachedTotal = info.cachedMemory();
        memoryBufferedTotal = info.bufferedMemory();
    }

    private String getBatteryInfo() {
        String info = "";
        if (useCelsius)
            info = battLevel + "% (" + temperature / 10 + "\u2103)";
        else
            info = battLevel + "% (" + ((int) temperature / 10 * 9 / 5 + 32) + "\u2109)";
        return info;
    }

    private void saveFile() {
        outFile.write(dataStatus());
        outFile.flush();
    }

    private String dataStatus() {
        String status;
        Debug.MemoryInfo memInfo = infoHelper.getMemoryInfo(data.get(packageIndex).pid());
        status = UserInterfaceUtil.convertToSize(memoryTotal, false) + "\t" +
                UserInterfaceUtil.convertToSize(memoryFreeTotal, false) + "\t" +
                UserInterfaceUtil.convertToSize(memoryCachedTotal, false) + "\t" +
                UserInterfaceUtil.convertToSize(memoryBufferedTotal, false) + "\t" +
                temperature / 10.0 + "\t" + battLevel + "\t" +
                UserInterfaceUtil.convertToUsage(totalCpuUsage)
                + "\t" +  UserInterfaceUtil.convertToUsage(data.get(packageIndex).cpuUsage())
                + "\t" + data.get(packageIndex).startTime()
                + "\t" + data.get(packageIndex).cpuTime()
                + "\t" + data.get(packageIndex).usedSystemTime()
                + "\t" + data.get(packageIndex).ppid()
                + "\t" + data.get(packageIndex).usedUserTime()
                + "\t" + data.get(packageIndex).status()
                + "\t" + UserInterfaceUtil.convertToSize((data.get(packageIndex).rss() * 1024), false)
                + "\t" + UserInterfaceUtil.convertToSize(memInfo.getTotalPss() * 1024, false)
                + "\t" + UserInterfaceUtil.convertToSize(memInfo.getTotalPrivateDirty() * 1024, false)
                + "\n";

        Log.i(TAG, status);
        return status;
    }
}
