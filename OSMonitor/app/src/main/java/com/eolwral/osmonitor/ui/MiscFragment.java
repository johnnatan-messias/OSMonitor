package com.eolwral.osmonitor.ui;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.eolwral.osmonitor.R;
import com.eolwral.osmonitor.core.networkInfo;
import com.eolwral.osmonitor.core.networkInfoList;
import com.eolwral.osmonitor.core.osInfo;
import com.eolwral.osmonitor.core.processorInfo;
import com.eolwral.osmonitor.core.processorInfoList;
import com.eolwral.osmonitor.ipc.IpcService;
import com.eolwral.osmonitor.ipc.IpcService.ipcClientListener;
import com.eolwral.osmonitor.ipc.ipcCategory;
import com.eolwral.osmonitor.ipc.ipcData;
import com.eolwral.osmonitor.ipc.ipcMessage;
import com.eolwral.osmonitor.preference.OSMPreference;
import com.eolwral.osmonitor.settings.Settings;
import com.eolwral.osmonitor.util.UserInterfaceUtil;

public class MiscFragment extends Fragment implements ipcClientListener {

  // ipc client
  private IpcService ipcService = IpcService.getInstance();;
  private boolean ipcStop = false;

  // data
  private osInfo osdata = null;
  private ArrayList<processorInfo> coredata = new ArrayList<processorInfo>();
  private ArrayList<networkInfo> nwdata = new ArrayList<networkInfo>();
  private Settings settings = null;

  // list
  private MiscListAdapter misckAdapter = null;
  private String[] miscItems = null;

  // expand
  private HashMap<String, Boolean> networkExpanded = new HashMap<String, Boolean>();
  private HashMap<String, Boolean> coreExpanded = new HashMap<String, Boolean>();

  // stop or start
  private boolean stopUpdate = false;
  private MenuItem stopButton = null;

  // battery view
  private View batteryView = null;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // settings
    settings = Settings.getInstance(getActivity().getApplicationContext());
  }

  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    View v = inflater.inflate(R.layout.ui_misc_fragment, container, false);

    miscItems = getResources().getStringArray(R.array.ui_misc_item);

    ExpandableListView miscList = (ExpandableListView) v
        .findViewById(android.R.id.list);
    misckAdapter = new MiscListAdapter(getActivity().getApplicationContext());
    miscList.setGroupIndicator(null);
    miscList.setAdapter(misckAdapter);

    int count = misckAdapter.getGroupCount();
    for (int position = 1; position <= count; position++)
      miscList.expandGroup(position - 1);

    // enable fragment option menu
    setHasOptionsMenu(true);

    return v;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.ui_misc_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);

    // refresh button
    stopButton = (MenuItem) menu.findItem(R.id.ui_menu_stop);

    if (stopUpdate)
      stopButton.setIcon(R.drawable.ic_action_start);
    else
      stopButton.setIcon(R.drawable.ic_action_stop);

    return;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.ui_menu_setting:
      onSettingClick();
      break;
    case R.id.ui_menu_stop:
      onStopClick(item);
      break;
    case R.id.ui_menu_exit:
      onExitClick();
      break;
    }
    return super.onOptionsItemSelected(item);
  }

  private void onStopClick(MenuItem stopButton) {
    stopUpdate = !stopUpdate;

    if (stopUpdate)
      stopButton.setIcon(R.drawable.ic_action_start);
    else
      stopButton.setIcon(R.drawable.ic_action_stop);
    return;
  }

  private void onExitClick() {
    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
        new Intent("Exit"));
    return;
  }

  private void onSettingClick() {
    Intent settings = new Intent(getActivity(), OSMPreference.class);
    settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(settings);
    return;
  }

  // TODO: use view holder to reduce consuming resource
  private class MiscListAdapter extends BaseExpandableListAdapter {

    private LayoutInflater itemInflater = null;

    public MiscListAdapter(Context mContext) {
      itemInflater = (LayoutInflater) mContext
          .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
      return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
        boolean isLastChild, View convertView, ViewGroup parent) {

      View sv = null;

      switch (groupPosition) {
      case 0:
        sv = prepareSystemView(childPosition, convertView, parent);
        break;
      case 1:
        sv = prepareBatteryView(childPosition, convertView, parent);
        break;
      case 2:
        sv = prepareCPUView(childPosition, convertView, parent);
        break;
      case 3:
        sv = prepareMemoryView(childPosition, convertView, parent);
        break;
      case 4:
        sv = prepareSharedView(childPosition, convertView, parent);
        break;
      case 5:
        sv = prepareNetworkView(childPosition, convertView, parent);
        break;
      }
      return sv;
    }

    private View prepareBatteryView(int position, View convertView,
        ViewGroup parent) {
      View sv = null;
      if (batteryView == null) {
        sv = (View) itemInflater.inflate(R.layout.ui_misc_item_battery, parent,
            false);
        batteryView = sv;
      } else
        sv = batteryView;

      return sv;
    }

    public View prepareNetworkView(int position, View convertView,
        ViewGroup parent) {

      View sv = (View) itemInflater.inflate(R.layout.ui_misc_item_network,
          parent, false);

      if (nwdata.size() < position)
        return sv;

      // get data
      networkInfo item = nwdata.get(position);

      // prepare main information
      ((TextView) sv.findViewById(R.id.id_network_interface)).setText(item.name());
      if (item.ipv4Addr() != null)
        ((TextView) sv.findViewById(R.id.id_network_ip4)).setText(item.ipv4Addr()
             + "/" + item.netMaskv4());
      else
        ((TextView) sv.findViewById(R.id.id_network_ip4)).setText("");

      if (item.ipv6Addr() != null)
        ((TextView) sv.findViewById(R.id.id_network_ip6)).setText(item.ipv6Addr() + "/" + item.netMaskv6());
      else
        ((TextView) sv.findViewById(R.id.id_network_ip6)).setText("");

      // prepare main information
      ((TextView) sv.findViewById(R.id.id_network_mac)).setText(item.mac());
      ((TextView) sv.findViewById(R.id.id_network_rx)).setText(String.format("%,d", item.recvBytes()) +
          " (" + UserInterfaceUtil.convertToSize(item.recvBytes(), true) + ")");
      ((TextView) sv.findViewById(R.id.id_network_tx)).setText(String.format("%,d", item.transBytes()) +
          " (" + UserInterfaceUtil.convertToSize(item.transBytes(), true) + ")");

      StringBuilder status = new StringBuilder();

      final Resources resMgr = getActivity().getResources();

      // IFF_UP = 0x1, /* Interface is up. */
      if ((item.flags() & 0x1) != 0)
        status.append(resMgr.getString(R.string.ui_network_status_up));
      else
        status.append(resMgr.getString(R.string.ui_network_status_down));

      // IFF_BROADCAST = 0x2, /* Broadcast address valid. */
      if ((item.flags() & 0x2) != 0)
        status.append(" "
            + resMgr.getString(R.string.ui_network_status_broadcast));

      // IFF_DEBUG = 0x4, /* Turn on debugging. */
      // IFF_LOOPBACK = 0x8, /* Is a loopback net. */
      if ((item.flags() & 0x8) != 0)
        status.append(" "
            + resMgr.getString(R.string.ui_network_status_loopback));

      // IFF_POINTOPOINT = 0x10, /* Interface is point-to-point link. */
      if ((item.flags() & 0x10) != 0)
        status.append(" " + resMgr.getString(R.string.ui_network_status_p2p));

      // IFF_NOTRAILERS = 0x20, /* Avoid use of trailers. */
      // IFF_RUNNING = 0x40, /* Resources allocated. */
      if ((item.flags() & 0x40) != 0)
        status.append(" "
            + resMgr.getString(R.string.ui_network_status_running));

      // IFF_NOARP = 0x80, /* No address resolution protocol. */
      // IFF_PROMISC = 0x100, /* Receive all packets. */
      if ((item.flags() & 0x100) != 0)
        status.append(" "
            + resMgr.getString(R.string.ui_network_status_promisc));

      // IFF_ALLMULTI = 0x200, /* Receive all multicast packets. */
      // IFF_MASTER = 0x400, /* Master of a load balancer. */
      // IFF_SLAVE = 0x800, /* Slave of a load balancer. */

      // IFF_MULTICAST = 0x1000, /* Supports multicast. */
      if ((item.flags() & 0x1000) != 0)
        status.append(" "
            + resMgr.getString(R.string.ui_network_status_multicast));

      // IFF_PORTSEL = 0x2000, /* Can set media type. */
      // IFF_AUTOMEDIA = 0x4000, /* Auto media select active. */
      // IFF_DYNAMIC = 0x8000 /* Dialup device with changing addresses. */

      ((TextView) sv.findViewById(R.id.id_network_status)).setText(status
          .toString());

      // prepare click
      sv.setTag(item.name());
      sv.setClickable(true);
      sv.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          Boolean flag = networkExpanded.get(v.getTag());
          if (flag == null) {
            networkExpanded.put((String) v.getTag(), true);
            flag = true;
          } else {
            networkExpanded.put((String) v.getTag(), !flag);
            flag = !flag;
          }

          if (flag)
            v.findViewById(R.id.id_misc_item_network_detail).setVisibility(
                View.VISIBLE);
          else
            v.findViewById(R.id.id_misc_item_network_detail).setVisibility(
                View.GONE);
        }
      });

      // set visibility
      Boolean flag = networkExpanded.get(item.name());
      if (flag == null)
        flag = false;
      if (flag)
        sv.findViewById(R.id.id_misc_item_network_detail).setVisibility(
            View.VISIBLE);
      else
        sv.findViewById(R.id.id_misc_item_network_detail).setVisibility(
            View.GONE);

      return sv;
    }

    public View prepareCPUView(int position, View convertView, ViewGroup parent) {

      // prepare view
      View sv = (View) itemInflater.inflate(R.layout.ui_misc_item_processor,
          parent, false);

      if (coredata.size() < position)
        return sv;

      // get data
      processorInfo item = coredata.get(position);

      if (item.minFrequency() != -1)
        ((TextView) sv.findViewById(R.id.id_processor_freq_min)).setText("" + item.minFrequency());
      else
        ((TextView) sv.findViewById(R.id.id_processor_freq_min)).setText("?");

      if (item.maxFrequency() != -1)
        ((TextView) sv.findViewById(R.id.id_processor_freq_max)).setText("" + item.maxFrequency());
      else
        ((TextView) sv.findViewById(R.id.id_processor_freq_max)).setText("?");

      if (item.minScaling() != -1)
        ((TextView) sv.findViewById(R.id.id_processor_cur_max)).setText("" + item.maxScaling());
      else
        ((TextView) sv.findViewById(R.id.id_processor_cur_max)).setText("?");

      if (item.maxScaling() != -1)
        ((TextView) sv.findViewById(R.id.id_processor_cur_min)).setText("" + item.minScaling());
      else
        ((TextView) sv.findViewById(R.id.id_processor_cur_min)).setText("?");

      if (item.currentScaling() != -1)
        ((TextView) sv.findViewById(R.id.id_processor_cur)).setText("" + item.currentScaling());
      else
        ((TextView) sv.findViewById(R.id.id_processor_cur)).setText("?");

      ((TextView) sv.findViewById(R.id.id_processor_gov)).setText(item.governors());
      ((TextView) sv.findViewById(R.id.id_processor_core)).setText("" + item.number());

      if (item.offLine() == 1)
        ((TextView) sv.findViewById(R.id.id_processor_status))
            .setText(getActivity().getResources().getText(
                R.string.ui_processor_status_offline));
      else
        ((TextView) sv.findViewById(R.id.id_processor_status))
            .setText(getActivity().getResources().getText(
                R.string.ui_processor_status_online));

      // prepare click
      sv.setTag("" + item.number());
      sv.setClickable(true);
      sv.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          Boolean flag = coreExpanded.get(v.getTag());
          if (flag == null) {
            coreExpanded.put((String) v.getTag(), true);
            flag = true;
          } else {
            coreExpanded.put((String) v.getTag(), !flag);
            flag = !flag;
          }

          if (flag)
            v.findViewById(R.id.id_misc_item_cpu_detail).setVisibility(
                View.VISIBLE);
          else
            v.findViewById(R.id.id_misc_item_cpu_detail).setVisibility(
                View.GONE);
        }
      });

      // set visibility
      Boolean flag = coreExpanded.get("" + item.number());
      if (flag == null)
        flag = false;
      if (flag)
        sv.findViewById(R.id.id_misc_item_cpu_detail).setVisibility(
            View.VISIBLE);
      else
        sv.findViewById(R.id.id_misc_item_cpu_detail).setVisibility(View.GONE);

      return sv;
    }

    private View prepareSharedView(int position, View convertView,
        ViewGroup parent) {
      View sv = (View) itemInflater.inflate(R.layout.ui_misc_item_swap, parent,
          false);

      if (osdata != null) {

        ((TextView) sv.findViewById(R.id.id_swap_total)).setText(String.format(
            "%,d", osdata.totalSwap())
            + " ("
            + UserInterfaceUtil.convertToSize(osdata.totalSwap(), true) + ")");
        ((TextView) sv.findViewById(R.id.id_swap_free)).setText(String.format(
            "%,d", osdata.freeSwap())
            + " ("
            + UserInterfaceUtil.convertToSize(osdata.freeSwap(), true) + ")");
      }

      return sv;
    }

    private View prepareMemoryView(int position, View convertView,
        ViewGroup parent) {
      View sv = (View) itemInflater.inflate(R.layout.ui_misc_item_memory,
          parent, false);

      if (osdata != null) {

        ((TextView) sv.findViewById(R.id.id_memory_total)).setText(String
            .format("%,d", osdata.totalMemory())
            + " ("
            + UserInterfaceUtil.convertToSize(osdata.totalMemory(), true) + ")");

        ((TextView) sv.findViewById(R.id.id_memory_free)).setText(String
            .format("%,d", osdata.freeMemory())
            + " ("
            + UserInterfaceUtil.convertToSize(osdata.freeMemory(), true) + ")");

        ((TextView) sv.findViewById(R.id.id_memory_cached)).setText(String
            .format("%,d", osdata.cachedMemory())
            + " ("
            + UserInterfaceUtil.convertToSize(osdata.cachedMemory(), true) + ")");

        ((TextView) sv.findViewById(R.id.id_memory_buffered)).setText(String
            .format("%,d", osdata.bufferedMemory(), true)
            + " ("
            + UserInterfaceUtil.convertToSize(osdata.bufferedMemory(), true) + ")");
      }

      return sv;
    }

    private View prepareSystemView(int position, View convertView,
        ViewGroup parent) {
      View sv = (View) itemInflater.inflate(R.layout.ui_misc_item_system,
          parent, false);

      final Calendar calendar = Calendar.getInstance();
      final DateFormat convertTool = DateFormat.getDateTimeInstance();
      final Date currentDate = new Date();
      if (osdata != null) {

        long Uptime = currentDate.getTime() - (osdata.upTime() * 1000);
        int seconds = (int) ((Uptime / 1000) % 60);
        int minutes = (int) ((Uptime / 1000) / 60 % 60);
        int hours = (int) ((Uptime / 1000) / 3600 % 24);
        int days = (int) ((Uptime / 1000) / 86400);

        calendar.setTimeInMillis(osdata.upTime() * 1000);

        ((TextView) sv.findViewById(R.id.id_system_update)).setText(Html
            .fromHtml(convertTool.format(calendar.getTime())
                + String.format("<br/><b>[ %d day(s) %02d:%02d:%02d ]</b>",
                    days, hours, minutes, seconds)));
      }

      return sv;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
      int count = 0;
      switch (groupPosition) {
      case 0:
        count = 1;
        break;
      case 1:
        count = 1;
        break;
      case 2:
        count = coredata.size();
        break;
      case 3:
        count = 1;
        break;
      case 4:
        count = 1;
        break;
      default:
        count = nwdata.size();
        break;
      }
      return count;
    }

    @Override
    public Object getGroup(int groupPosition) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public int getGroupCount() {
      return miscItems.length;
    }

    @Override
    public long getGroupId(int groupPosition) {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
        View convertView, ViewGroup parent) {

      View sv = null;

      // prepare view
      if (convertView == null)
        sv = (View) itemInflater.inflate(R.layout.ui_misc_item, parent, false);
      else
        sv = (View) convertView;

      switch (groupPosition) {
      case 2:
        if (settings != null && settings.isRoot()) {
          Button procBtn = (Button) sv.findViewById(R.id.id_misc_button);
          procBtn.setVisibility(View.VISIBLE);
          procBtn.setOnClickListener(new ProcessorClickListener());
        } else {
          sv.findViewById(R.id.id_misc_button).setVisibility(View.GONE);
        }
        break;
      default:
        sv.findViewById(R.id.id_misc_button).setVisibility(View.GONE);
        break;
      }

      // set title
      ((TextView) sv.findViewById(R.id.id_misc_title))
          .setText(miscItems[groupPosition]);

      return sv;
    }

    @Override
    public boolean hasStableIds() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
      // TODO Auto-generated method stub
      return false;
    }

    private class ProcessorClickListener implements OnClickListener {

      @Override
      public void onClick(View v) {
        // Replace One Fragment with Another
        // http://developer.android.com/training/basics/fragments/fragment-ui.html#Replace

        // pass information
        MiscProcessorFragment procFragment = new MiscProcessorFragment();

        // replace current fragment
        final FragmentManager fragmanger = getActivity()
            .getSupportFragmentManager();
        final FragmentTransaction transaction = fragmanger.beginTransaction();
        transaction.replace(R.id.ui_misc_layout, procFragment, "Processor");
        transaction.addToBackStack(null);
        transaction.commit();
      }

    }
  }

  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);

    ipcService.removeRequest(this);
    ipcStop = !isVisibleToUser;

    if (batteryView != null)
      stopBatteryMonitor();

    if (isVisibleToUser == true) {
      byte newCommand[] = { ipcCategory.OS, ipcCategory.PROCESSOR, ipcCategory.NETWORK };
      ipcService.addRequest(newCommand, 0, this);
      startBatteryMonitor();
    }

  }

  @Override
  public void onRecvData(byte [] result) {

    // check
    if (ipcStop == true)
      return;

    if (stopUpdate == true || result == null) {
      byte newCommand[] = { ipcCategory.OS, ipcCategory.PROCESSOR, ipcCategory.NETWORK };
      ipcService.addRequest(newCommand, settings.getInterval(), this);
      return;
    }

    coredata.clear();
    nwdata.clear();

    // convert data
    ipcMessage resultMessage = ipcMessage.getRootAsipcMessage(ByteBuffer.wrap(result));
    for (int index = 0; index < resultMessage.dataLength(); index++) {

      try {
        ipcData rawData = resultMessage.data(index);

        if (rawData.category() == ipcCategory.OS)
          extractOsInfo(rawData);
        else if (rawData.category() == ipcCategory.PROCESSOR)
          extractProcessorInfo(rawData);
        else if (rawData.category() == ipcCategory.NETWORK)
          extractNetworkInfo(rawData);

      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    // refresh
    misckAdapter.notifyDataSetChanged();

    // send command again
    byte newCommand[] = { ipcCategory.OS, ipcCategory.PROCESSOR, ipcCategory.NETWORK };
    ipcService.addRequest(newCommand, settings.getInterval(), this);
  }

private void extractOsInfo(ipcData rawData) {
	osdata = osInfo.getRootAsosInfo(rawData.payloadAsByteBuffer().asReadOnlyBuffer());
}

  private void extractNetworkInfo(ipcData rawData)
  {
    networkInfoList list = networkInfoList.getRootAsnetworkInfoList(rawData.payloadAsByteBuffer().asReadOnlyBuffer());
    for (int count = 0; count < list.listLength(); count++) {
      networkInfo nwInfo = list.list(count);

      /*
       * if(nwInfo.getIpv4Addr().length() > 0 || nwInfo.getIpv6Addr().length() >
       * 0 || nwInfo.getMac().length() > 0)
       */
      nwdata.add(nwInfo);
    }
  }

  private void extractProcessorInfo(ipcData rawData)
  {
    processorInfoList list = processorInfoList.getRootAsprocessorInfoList(rawData.payloadAsByteBuffer().asReadOnlyBuffer());
    for (int count = 0; count < list.listLength(); count++) {
      processorInfo prInfo = list.list(count);
      coredata.add(prInfo);
    }
  }

  private void startBatteryMonitor() {
    try {
      IntentFilter battFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
      getActivity().registerReceiver(battReceiver, battFilter);
    } catch (Exception e) {
    }
  }

  private void stopBatteryMonitor() {
    try {
      getActivity().unregisterReceiver(battReceiver);
    } catch (Exception e) {
    }
  }

  private BroadcastReceiver battReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {

      Resources ResourceManager = context.getResources();

      if (ResourceManager == null || batteryView == null)
        return;

      int rawlevel = intent.getIntExtra("level", -1);
      int scale = intent.getIntExtra("scale", -1);
      int status = intent.getIntExtra("status", -1);
      int health = intent.getIntExtra("health", -1);
      int plugged = intent.getIntExtra("plugged", -1);
      int temperature = intent.getIntExtra("temperature", -1);
      int voltage = intent.getIntExtra("voltage", -1);
      String technology = intent.getStringExtra("technology");

      int level = -1; // percentage, or -1 for unknown
      if (rawlevel > 0 && scale > 0) {
        level = (rawlevel * 100) / scale;
      } else
        level = 0;

      TextView batteryHealth = (TextView) batteryView
          .findViewById(R.id.id_battery_health);
      switch (health) {
      case BatteryManager.BATTERY_HEALTH_DEAD:
        batteryHealth.setText(ResourceManager
            .getText(R.string.ui_battery_health_dead));
        break;
      case BatteryManager.BATTERY_HEALTH_GOOD:
        batteryHealth.setText(ResourceManager
            .getText(R.string.ui_battery_health_good));
        break;
      case BatteryManager.BATTERY_HEALTH_OVERHEAT:
        batteryHealth.setText(ResourceManager
            .getText(R.string.ui_battery_health_overheat));
        break;
      case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
        batteryHealth.setText(ResourceManager
            .getText(R.string.ui_battery_health_overvoltage));
        break;
      case BatteryManager.BATTERY_HEALTH_UNKNOWN:
        batteryHealth.setText(ResourceManager
            .getText(R.string.ui_battery_health_unknown));
        break;
      case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
        batteryHealth.setText(ResourceManager
            .getText(R.string.ui_battery_health_failure));
        break;

      }

      java.text.DecimalFormat TempFormat = new java.text.DecimalFormat("#.##");
      TextView batteryTechnology = (TextView) batteryView
          .findViewById(R.id.id_battery_technology);
      batteryTechnology.setText(technology);

      TextView batteryCapacity = (TextView) batteryView
          .findViewById(R.id.id_battery_capacity);
      batteryCapacity.setText(level + "%");

      TextView batteryVoltage = (TextView) batteryView
          .findViewById(R.id.id_battery_voltage);
      batteryVoltage.setText(voltage + "mV");

      TextView batteryTemperature = (TextView) batteryView
          .findViewById(R.id.id_battery_temperature);
      batteryTemperature.setText(((double) temperature / 10) + "\u2103 ("
          + TempFormat.format(((double) temperature / 10 * 9 / 5 + 32))
          + "\u2109)");

      TextView batteryStatus = (TextView) batteryView
          .findViewById(R.id.id_battery_status);
      StringBuilder statusBuilder = new StringBuilder();
      switch (status) {
      case BatteryManager.BATTERY_STATUS_UNKNOWN:
        statusBuilder.append("<b>"
            + ResourceManager.getText(R.string.ui_battery_status_unknown)
            + "</b>");
        break;
      case BatteryManager.BATTERY_STATUS_CHARGING:
        statusBuilder.append("<b>"
            + ResourceManager.getText(R.string.ui_battery_status_charging)
            + "</b>");
        break;
      case BatteryManager.BATTERY_STATUS_DISCHARGING:
        statusBuilder.append("<b>"
            + ResourceManager.getText(R.string.ui_battery_status_discharging)
            + "</b>");
        break;
      case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
        statusBuilder.append("<b>"
            + ResourceManager.getText(R.string.ui_battery_status_notcharging)
            + "</b>");
        break;
      case BatteryManager.BATTERY_STATUS_FULL:
        statusBuilder
            .append("<b>"
                + ResourceManager.getText(R.string.ui_battery_status_full)
                + "</b>");
        break;
      }

      if (plugged == BatteryManager.BATTERY_PLUGGED_AC)
        statusBuilder.append(" [<font color=\"green\">").append(
            ResourceManager.getText(R.string.ui_battery_acpower) + "</font>]");

      else if (plugged == BatteryManager.BATTERY_PLUGGED_USB)
        statusBuilder.append(" [<font color=\"green\">").append(
            ResourceManager.getText(R.string.ui_battery_usbpower) + "</font>]");

      batteryStatus.setText(Html.fromHtml(statusBuilder.toString()));
    }
  };

}
