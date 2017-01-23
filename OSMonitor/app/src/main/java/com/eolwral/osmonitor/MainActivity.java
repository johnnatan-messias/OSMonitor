package com.eolwral.osmonitor;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.eolwral.osmonitor.ipc.IpcService;
import com.eolwral.osmonitor.util.UserInterfaceUtil;

import java.io.File;

public class MainActivity extends Activity {
	private String filenameIn = "datasets" + File.separator
			+ "pang_movie_sem_score.txt";
	private static String TAG = MainActivity.class.getSimpleName();

	private Button buttonStart, buttonStop;
	private boolean isServiceRunning = false;
	private TextView statusTextView;
	private BroadcastReceiver mReceiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		buttonStart = (Button) findViewById(R.id.button_start);
		buttonStop = (Button) findViewById(R.id.button_stop);
		statusTextView = (TextView) findViewById(R.id.status);
		//IpcService.Initialize(this);
		//UserInterfaceUtil.Initialize(this);

		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.i(TAG, "Received");
				onClickStop();
			}
		};
		registerReceiver(mReceiver, new IntentFilter("stopOSMonitor"));
		onClickStart();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isServiceRunning) {
			stopService(new Intent(this, OSMonitorCollectorService.class));
			Log.i(TAG, "Service Finished");
		}
		unregisterReceiver(mReceiver);
	}

	public void onClickStartMyService(View v) {
		onClickStart();
	}

	public void onClickStart(){
		buttonStart.setEnabled(false);
		buttonStop.setEnabled(true);
		isServiceRunning = true;
		startService(new Intent(this, OSMonitorCollectorService.class));
		statusTextView.setText("Running");
		// startService(new Intent(this, SystemSens.class));
		Log.i(TAG, "Service started...");
	}

	private void onClickStop(){
		stopService(new Intent(this, OSMonitorCollectorService.class));
		buttonStart.setEnabled(true);
		buttonStop.setEnabled(false);
		isServiceRunning = false;
		statusTextView.setText("Finished");
		Log.i(TAG, "Service finished");
	}
	public void onClickStopMyService(View v) {
		onClickStop();
	}
}