package com.eolwral.osmonitor.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.List;
import java.lang.reflect.Method;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceActivity;

import com.eolwral.osmonitor.OSMonitorService;
import com.eolwral.osmonitor.ipc.IpcService;
import com.eolwral.osmonitor.ipc.ipcCategory;
import com.eolwral.osmonitor.settings.Settings;

public class CoreUtil {

  /**
   * predefine location for osmcore
   */
  private final static String binaryName = "osmcore";

  /**
   * predefine socket name
   */
  private final static String socketName = "osmipcV";

  /**
   * bring up help activity
   * 
   * @param context
   * @param url
   */
  public static void showHelp(Context context, String url) {
    // To use try-catch and avoid Activity is not ready
    try {
      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setData(Uri.parse(url));
      context.startActivity(intent);
    } catch (ActivityNotFoundException e) {
    }
  }

  /**
   * kill a process
   * 
   * @param pid
   * @param context
   */
  public static void killProcess(int pid, Context context) {
    final Settings settings = Settings.getInstance(context);
    if (!settings.isRoot())
      android.os.Process.killProcess(pid);
    else
      IpcService.getInstance().sendCommand(ipcCategory.KILLPROCESS, pid);
  }

  /**
   * is ARMv7 base ?
   * 
   * @return true == yes , false == no
   */
  public static boolean isARMv7() {
    return (android.os.Build.CPU_ABI.toLowerCase().contains("armeabi-v7"));
  }

  /**
   * is ARM base ?
   * 
   * @return true == yes , false == no
   */
  public static boolean isARM() {
    return (android.os.Build.CPU_ABI.toLowerCase().contains("armeabi"));
  }

  /**
   * is MIPS base ?
   * 
   * @return true == yes, false == no
   */
  public static boolean isMIPS() {
    return (android.os.Build.CPU_ABI.toLowerCase().contains("mips"));
  }

  /**
   * is X86 base ?
   * 
   * @return true == yes, false == no
   */
  public static boolean isX86() {
    return (android.os.Build.CPU_ABI.toLowerCase().contains("x86"));
  }

  /**
   * is Android Lollipop ?
   * 
   * @return true == yes, false == no
   */
  public static boolean isLollipop() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
  }

  /**
   * is Cyanogenmod ROM ?
   * @return true == yes, false == no
   */
  public static boolean isCyanogenmod() {
    String version = System.getProperty("os.version");
    if (version.contains("cyanogenmod"))
      return true;
    return false;
  }

  /**
   * check file status
   * 
   * @param file
   *          path
   * @return true == exist, false == not exist
   */
  @SuppressWarnings("unused")
  private static boolean fileExist(String localPath) {
    File targetFile = new File(localPath);
    return targetFile.exists();
  }

  /**
   * copy a binary from asset directory to working directory.
   * 
   * @param assetPath
   * @param localPath
   * @param context
   * @return true == copied, false == text busy
   */
  private static boolean copyFile(String assetPath, String localPath,
      Context context) {
    try {

      // detect architecture
      if (isARM())
        assetPath += "_arm";
      else if (isX86())
        assetPath += "_x86";
      else if (isMIPS())
        assetPath += "_mips";
      else
        assetPath += "_arm";

      if (isLollipop())
        assetPath += "_pie";

      InputStream binary = context.getAssets().open(assetPath);
      FileOutputStream execute = new FileOutputStream(localPath);

      int read = 0;
      byte[] buffer = new byte[4096];

      while ((read = binary.read(buffer)) > 0)
        execute.write(buffer, 0, read);

      execute.close();
      binary.close();

      execute = null;
      binary = null;

    } catch (IOException e) {
      return false;
    }
    return true;
  }

  /**
   * write a security token file
   * 
   * @param tokenFilePath
   *          path of the security token
   * @param token
   *          security token
   * @return true or false
   */
  private static boolean writeTokenFile(String tokenFilePath, String token) {
    try {
      FileWriter file = new FileWriter(tokenFilePath, false);
      file.write(token);
      file.close();
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  /**
   * get unix domain socket name
   * @param context Application Context
   * @return String socket name
   */
  public static String getSocketName(Context context) {

    // use abstract or file system 
    String name = null;
    if (CoreUtil.isLollipop())
      name = context.getFilesDir().getAbsolutePath() + "/" + socketName;
    else
      name = socketName;

    try {
      name += context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
    } catch (NameNotFoundException e) {}
    
    return name;
  }

  /**
   * get binary name
   * @param context Application Context
   * @return String binary file name
   */
  public static String getBinaryName(Context context) {
    return context.getFilesDir().getAbsolutePath() + "/" + binaryName;
  }

  /**
   * get uid name
   * @param context Application Context
   * @return String uid
   */
  public static String getUid(Context context) {
    return Integer.toString(context.getApplicationInfo().uid);
  }

  /**
   * execute osmcore as a binary execute
   * 
   * @param context
   * @throws InterruptedException
   */
  public static boolean execCore(Context context) {

    boolean flag = false;
    
    if (context == null)
      return flag;

    String binary = CoreUtil.getBinaryName(context);
    String socket = CoreUtil.getSocketName(context);
    String uid = CoreUtil.getUid(context);
    final Settings settings = Settings.getInstance(context);

    restoreSecurityContext(binary, settings);
 
    // copy file
    if (!copyFile("osmcore", binary, context))
      return flag;

    // write token file
    if (!writeTokenFile(binary + ".token", settings.getToken()))
      return flag;

    // lock file
    File file = new File(binary + ".lock");
    FileChannel channel = null;
    FileLock lock = null;
    try {
      channel = new RandomAccessFile(file, "rw").getChannel();
      lock = channel.tryLock();
    } catch (Exception e) {
      return flag;
    }

    // execute osmcore
    try {
      CoreUtil.runSHELL(new String[] { "chmod", "755", binary });

      if (!settings.isRoot()) {
        CoreUtil.runSHELL(new String [] { binary, binary + ".token", socket, uid, "&"});
      } else {

        if (!CoreUtil.isLollipop()) {
          CoreUtil.runSU(new String [] { binary, binary + ".token", socket, uid, "&"});
        } else {
          boolean supportSecurityContext = CoreUtil.isSupportSecurityContext();
          CoreUtil.runSU(new String [] { "chcon", "u:object_r:system_file:s0", binary });
          if (!supportSecurityContext)
            CoreUtil.runSU(new String [] { "su", "-c", "\"" + binary,
                                           binary + ".token", socket, uid, " &\" &" });
          else
            CoreUtil.runSU(new String [] { "su", "--context", "u:r:init:s0", "-c", "\"" + binary,
                                           binary + ".token", socket, uid, " &\" &" });
          CoreUtil.runSU(new String [] { "chcon", "u:object_r:app_data_file:s0", binary });
        }
      }
      flag = true;  
    } catch (Exception e) { }

    // release the lock
    try {
      lock.release();
      channel.close();
    } catch (Exception e) { }

    return flag;
  }

  /**
   * restore security context
   * @param String binary path
   * @param settings 
   */
  private static void restoreSecurityContext(String binary, final Settings settings) {
    // force set security context to avoid permission issue
    if (settings.isRoot() && isLollipop()) {
      try {
        CoreUtil.runSU(new String [] { "chcon", "u:object_r:app_data_file:s0", binary });
      } catch (Exception e) { }
    }
  }

  /**
   * run shell command with root permission
   * @param String [] array of arguments
   * @return int Exit code
   * @throws Exception 
   */
  public static int runSU(String [] args) throws Exception {
    int exitCode = 0;

    StringBuilder builder = new StringBuilder();
    for(String arg : args) {
      builder.append(arg);
      builder.append(" ");
    }
    builder.append("\n");

    try {
      Process process = Runtime.getRuntime().exec(new String[] { "su" });
      DataOutputStream os = new DataOutputStream(process.getOutputStream());

      os.writeBytes(builder.toString());
      os.flush();

      os.writeBytes("exit $?\n\n");
      os.flush();

      process.waitFor();
      exitCode = process.exitValue();
    } catch (IOException e) {
      throw new Exception(e);
    } catch (InterruptedException e) {
      throw new Exception(e);
    } 

    return exitCode;
  }

  /**
   * run shell command with user permission
   * @param String [] array of arguments
   * @return int Exit code
   * @throws Exception 
   */
  public static int runSHELL(String [] args) throws Exception {
    int exitCode = 1;

    StringBuilder builder = new StringBuilder();
    for(String arg : args) {
      builder.append(arg);
      builder.append(" ");
    }
    builder.append("\n");

    try {
      
      Process process = Runtime.getRuntime().exec(new String[] { "sh" });
      DataOutputStream os = new DataOutputStream(process.getOutputStream());

      os.writeBytes(builder.toString());
      os.flush();

      os.writeBytes("exit $?\n\n");
      os.flush();
      
      process.waitFor();
      exitCode = process.exitValue();
    } catch (IOException e) {
      throw new Exception(e);
    } catch (InterruptedException e) {
      throw new Exception(e);
    } 

    return exitCode;
  }

  /**
   * detect that Device is rooted or not
   * 
   * @return true == rooted, false == non-rooted
   */
  public static boolean preCheckRoot() {
    boolean flag = false;

    try {
      if (CoreUtil.runSU(new String [] {}) == 0) {
        flag = true;
      }
    } catch (Exception e) { }

    return flag;
  }
  
  /**
   * Detect security context feature
   * @return true == yes, false == no
   */
  public static boolean isSupportSecurityContext() {

    
    boolean support = false;
    try {
      Process process = Runtime.getRuntime().exec(new String[] { "sh" });
      DataOutputStream os = new DataOutputStream(process.getOutputStream());
      BufferedReader ibr = new BufferedReader(new InputStreamReader(process.getInputStream()));

      os.writeBytes("su -h\n");
      os.flush();

      os.writeBytes("exit\n\n");
      os.flush();

      process.waitFor();

      // SuperSU supports security context feature
      if (ibr.readLine().contains("SuperSU"))
        support = true;

    } catch (IOException e) {} 
      catch (InterruptedException e) {} 

    return support;
  }

  /**
   * detect background service is running or not
   * 
   * @param context
   * @return running == ture, none == false
   */
  public static boolean isServiceRunning(Context context) {
    final ActivityManager actMgr = (ActivityManager) context
        .getSystemService(Context.ACTIVITY_SERVICE);
    final List<RunningServiceInfo> services = actMgr
        .getRunningServices(Integer.MAX_VALUE);

    for (RunningServiceInfo serviceInfo : services) {
      if (serviceInfo.service.getClassName().equals(
          OSMonitorService.class.getName()))
        return true;
    }
    return false;
  }

  /**
   * Determined installation location
   * @param PreferenceActivity activity
   * @return true == external storage, false == internal stroage
   */
  public static boolean isExtraStroage(PreferenceActivity activity)
  {
    boolean flag = false;
    if(Integer.parseInt(Build.VERSION.SDK) >= 8)
    {
      // use Reflection to avoid errors (for cupcake 1.5)
      Method MethodList[] = activity.getClass().getMethods();
      for(int checkMethod = 0; checkMethod < MethodList.length; checkMethod++)
      {
        if(MethodList[checkMethod].getName().indexOf("ApplicationInfo") != -1)
        {
          try{
            if((((ApplicationInfo) MethodList[checkMethod].invoke(activity , new Object[]{})).flags & 0x40000 /* ApplicationInfo.FLAG_EXTERNAL_STORAGE*/ ) != 0 )
              flag = true;
          }
          catch(Exception e) {}
        }
      }
    }
    return flag;
  }
}
