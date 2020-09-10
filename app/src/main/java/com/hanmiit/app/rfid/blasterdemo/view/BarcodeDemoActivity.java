package com.hanmiit.app.rfid.blasterdemo.view;

import java.util.Locale;

import com.hanmiit.lib.barcode.type.BarcodeType;
import com.hanmiit.lib.device.type.ConnectionState;
import com.hanmiit.lib.diagnostics.ATLog;
import com.hanmiit.lib.rfid.event.ATRfidEventListener;
import com.hanmiit.lib.rfid.exception.ATRfidReaderException;
import com.hanmiit.lib.rfid.type.ActionState;
import com.hanmiit.lib.rfid.type.CommandType;
import com.hanmiit.lib.rfid.type.OperationMode;
import com.hanmiit.lib.rfid.type.RemoteKeyState;
import com.hanmiit.lib.rfid.type.ResultCode;
import com.hanmiit.lib.util.Version;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.adapter.BarcodeListAdapter;
import com.hanmiit.app.rfid.blasterdemo.adapter.SpinnerAdapter;
import com.hanmiit.app.rfid.blasterdemo.dialog.WaitDialog;
import com.hanmiit.app.rfid.blasterdemo.util.ResUtil;
import com.hanmiit.app.rfid.blasterdemo.view.base.RfidClearActivity;
import com.hanmiit.lib.ATRfidReader;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class BarcodeDemoActivity extends RfidClearActivity implements ATRfidEventListener, OnClickListener {

	private static final String TAG = BarcodeDemoActivity.class.getSimpleName();

	private static final int VIEW_BARCODE_OPTION = 1000;

	private ListView lstBarcodes;
	//private Spinner spnRescanTime;
	private TextView txtCount;
	private Button btnAction;

	private BarcodeListAdapter adpBarcode;
	private SpinnerAdapter adpRescanTime;

	private boolean isInitialized;
	private boolean isScanning;
	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_barcode_demo);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		ATLog.i(TAG, "INFO. onCreate()");

		isScanning = false;
		isInitialized = false;
		mHandler = new Handler();

		// Create Reader
		createReader();

		// Initialize Widget
		initWidgets();

		// Initialize Reader
		initReader();

		// Enable action widget
		enableActionWidgets(false);

		WaitDialog.show(this, R.string.barcode_mode_change);
		new Thread(mChangeBarcodeMode).start();
	}

	@Override
	protected void onDestroy() {

		ATLog.i(TAG, "INFO. onDestroy()");

		// Disable Barcode Mode
//		getReader().setBarcodeMode(false);

		// Destroy Reader
		destroyReader();

		super.onDestroy();
	}

//	@Override
//	protected void onStart() {
//		super.onStart();
//
//		if (isInitialized) {
//			// Set Key Action
//			try {
//				getReader().setUseKeyAction(getKeyAction());
//			} catch (ATRfidReaderException e) {
//				ATLog.e(TAG, e, "ERROR. onStart() - Failed to set use key action");
//			}
//		}
//		
//		ATLog.i(TAG, "INFO. onStart()");
//	}
//
//	@Override
//	protected void onStop() {
//		
//		// Set Key Action
//		try {
//			getReader().setUseKeyAction(false);
//		} catch (ATRfidReaderException e) {
//			ATLog.e(TAG, e, "ERROR. onStop() - Failed to set use key action");
//		}
//
//		ATLog.i(TAG, "INFO. onStop()");
//		super.onStop();
//	}
	
	@Override
	protected void onResume() {
		super.onResume();

		// Set Key Action
		try {
			getReader().setUseKeyAction(getKeyAction());
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. onResume() - Failed to set use key action");
		}
		
		ATLog.i(TAG, "INFO. onResume()");
	}

	@Override
	protected void onPause() {
		// Set Key Action
		try {
			getReader().setUseKeyAction(false);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. onPause() - Failed to set use key action");
		}

		ATLog.i(TAG, "INFO. onPause()");
		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		ATLog.i(TAG, "INFO. onActivityResult(%s, %s)", getRequestCode(requestCode), getResultCode(resultCode));

		if (requestCode == VIEW_BARCODE_OPTION) {
			enableActionWidgets(true);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		
		int id = v.getId();
		ATLog.i(TAG, "INFO. onClick(%s)", ResUtil.getId(id));

		switch (id) {
		case R.id.action:
			enableActionWidgets(false);
			if (isScanning) {
				getReader().stopDecode();
				isScanning = false;
			} else {
				getReader().startDecode();
				isScanning = true;
			}
			enableActionWidgets(true);
			break;
		}
	}

	@Override
	public void onStateChanged(ATRfidReader reader, ConnectionState state) {
		ATLog.i(TAG, "EVENT. onStateChanged(%s)", state);
	}

	@Override
	public void onActionChanged(ATRfidReader reader, ActionState action) {
		int rescanTime = 0;
		
		if (action == ActionState.StartDecode) {
//			rescanTime = adpRescanTime.getValue(spnRescanTime.getSelectedItemPosition());
//			getReader().setBarcodeContinueMode(rescanTime > 0);
			isScanning = true;
			btnAction.setText(R.string.action_scan_stop);
		} else if (action == ActionState.Stop){
//			rescanTime = adpRescanTime.getValue(spnRescanTime.getSelectedItemPosition());
//			if (rescanTime > 0) {
//				try {
//					mHandler.removeCallbacks(mRescanCallback);
//				} catch (Exception e) {
//
//				}
//			}
			isScanning = false;
			btnAction.setText(R.string.action_scan_start);
		}
		ATLog.i(TAG, "EVENT. onActionChanged(%s)", action);
	}

	@Override
	public void onDetactBarcode(ATRfidReader reader, BarcodeType type, String codeId, String barcode) {
		ATLog.i(TAG, "EVENT. onDetactBarcode (%s, %s, [%s])", type, codeId, barcode);
		if (type != BarcodeType.NoRead) {
			adpBarcode.addItem(type, codeId, barcode);
			txtCount.setText("" + adpBarcode.getCount());
		}

		if (isScanning) {
//			int rescanTime = adpRescanTime.getValue(spnRescanTime.getSelectedItemPosition());
//			if (rescanTime > 0) {
//				mHandler.postDelayed(mRescanCallback, rescanTime);
//
//			} else {
				isScanning = false;
				btnAction.setText(R.string.action_scan_start);
				enableActionWidgets(true);
			//}
		}
	}

	@Override
	public void onCommandComplete(ATRfidReader reader, CommandType command) {
		ATLog.i(TAG, "EVENT. onCommandComplete(%s)", command);
	}

	@Override
	public void onRemoteKeyStateChanged(ATRfidReader reader, RemoteKeyState state) {
		ATLog.i(TAG, "EVENT. onRemoteKeyStateChanged(%s)", state);

		switch (state) {
		case KeyUp:
//			getReader().stopDecode();
			break;
		case KeyDown:
			enableActionWidgets(false);
			if (isScanning) {
//				int rescanTime = adpRescanTime.getValue(spnRescanTime.getSelectedItemPosition());
//				if (rescanTime > 0) {
//					try {
//						mHandler.removeCallbacks(mRescanCallback);
//					} catch (Exception e) {
//
//					}
//				}
				getReader().stopDecode();
				isScanning = false;
				btnAction.setText(R.string.action_scan_start);
			} else {
				getReader().startDecode();
				isScanning = true;
				btnAction.setText(R.string.action_scan_stop);
			}
			enableActionWidgets(true);
			break;
		}
	}

	private Runnable mRescanCallback = new Runnable() {

		@Override
		public void run() {
			ATLog.i(TAG, "INFO. mRescanCallback.$run()");
			getReader().startDecode();
		}

	};

	// Initialize Reader
	protected void initReader() {
		ATLog.i(TAG, "INFO. initReader()");
	}

	private final String[] mRescanTime = new String[] { "Not Used", "100 ms", "200 ms", "300 ms", "400 ms", "500 ms",
			"600 ms", "700 ms", "800 ms", "900 ms", "1000 ms", "1100 ms", "1200 ms", "1300 ms", "1500 ms", "2000 ms",
			"2500 ms", "3000 ms", "3500 ms", "5000 ms" };

	private final int[] mRescanValue = new int[] { 0, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1100, 1200,
			1300, 1500, 2000, 2500, 3000, 3500, 5000 };

	// Initialize Widgets
	protected void initWidgets() {
		super.initWidgets();
		lstBarcodes = (ListView) findViewById(R.id.barcodes);
		adpBarcode = new BarcodeListAdapter(this);
		lstBarcodes.setAdapter(adpBarcode);
//		spnRescanTime = (Spinner) findViewById(R.id.rescan_time);
//		adpRescanTime = new SpinnerAdapter(this, android.R.layout.simple_list_item_1);
//		for (int i = 0; i < mRescanTime.length; i++) {
//			adpRescanTime.addItem(mRescanValue[i], mRescanTime[i]);
//		}
//		spnRescanTime.setAdapter(adpRescanTime);
//		spnRescanTime.setSelection(0);
		txtCount = (TextView) findViewById(R.id.barcode_count);
		txtCount.setText(String.format(Locale.US, "%d", adpBarcode.getCount()));
		btnAction = (Button) findViewById(R.id.action);
		btnAction.setOnClickListener(this);
		ATLog.i(TAG, "INFO. initWidgets()");
	}

	@Override
	protected void clearWidgets() {
		adpBarcode.clear();
		txtCount.setText(String.format(Locale.US, "%d", adpBarcode.getCount()));
		enableActionWidgets(true);
		ATLog.i(TAG, "INFO. clearWidgets()");
	}

	// Enable/Disable Action Widgets
	protected void enableActionWidgets(boolean enabled) {
		//spnRescanTime.setEnabled(!isScanning && enabled);
		btnAction.setEnabled(enabled);
		btnClear.setEnabled(!isScanning && enabled);
		ATLog.i(TAG, "INFO. enableActionWidgets(%s)", enabled);
	}

	private Runnable mChangeBarcodeMode = new Runnable() {

		@Override
		public void run() {
			isInitialized = false;
			
			ATLog.i(TAG, "+++ INFO. mChangeBarcodeMode.$run()");
			ResultCode res = ResultCode.NoError;

			try {
				boolean result = true;
				if (Version.isLaster(Version.V7_2_5_13)) {
					try {
						getReader().setOperationMode(OperationMode.Barcode);
					} catch (ATRfidReaderException e) {
						ATLog.e(TAG, "ERROR. mChangeBarcodeMode.$run() - Failed to change barcode mode [%s]", e.getResultCode());
						result = false;
					}
				} else {
					if ((res = getReader().setBarcodeMode(true,0)) != ResultCode.NoError) {
						ATLog.e(TAG, "ERROR. mChangeBarcodeMode.$run() - Failed to change barcode mode [%s]", res);
						result = false;
					}
				}
				if (!result) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							WaitDialog.hide();

							AlertDialog.Builder alert = new AlertDialog.Builder(BarcodeDemoActivity.this);
							alert.setTitle(R.string.system_alert);
							alert.setIcon(android.R.drawable.ic_dialog_alert);
							alert.setMessage(R.string.failed_activate_barcode);
							alert.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									finish();
								}
							});
							alert.show();
							isInitialized = false;
						}

					});
					return;
				}
				// Set Key Action
				getReader().setUseKeyAction(getKeyAction());
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. onStart() - Failed to change barcode mode");
			}

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					WaitDialog.hide();
					enableActionWidgets(true);
					isInitialized = true;
				}

			});
			ATLog.i(TAG, "--- INFO. mChangeBarcodeMode.$run()");
		}

	};

	@Override
	protected String getRequestCode(int requestCode) {
		switch (requestCode) {
		case VIEW_BARCODE_OPTION:
			return "VIEW_BARCODE_OPTION";
		}
		return super.getRequestCode(requestCode);
	}
}
