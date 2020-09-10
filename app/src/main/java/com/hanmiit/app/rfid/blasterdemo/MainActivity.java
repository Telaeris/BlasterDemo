package com.hanmiit.app.rfid.blasterdemo;

import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hanmiit.app.rfid.blasterdemo.dialog.WaitDialog;
import com.hanmiit.app.rfid.blasterdemo.type.MaskType;
import com.hanmiit.app.rfid.blasterdemo.view.BarcodeDemoActivity;
import com.hanmiit.app.rfid.blasterdemo.view.BarcodeOptionActivity;
import com.hanmiit.app.rfid.blasterdemo.view.FilterInventoryActivity;
import com.hanmiit.app.rfid.blasterdemo.view.InventoryActivity;
import com.hanmiit.app.rfid.blasterdemo.view.OptionActivity;
import com.hanmiit.app.rfid.blasterdemo.view.ReadMemoryActivity;
import com.hanmiit.app.rfid.blasterdemo.view.StoredTagActivity;
import com.hanmiit.app.rfid.blasterdemo.view.TagAccessActivity;
import com.hanmiit.app.rfid.blasterdemo.view.WriteMemoryActivity;
import com.hanmiit.app.rfid.blasterdemo.view.base.AccessActivity;
import com.hanmiit.app.rfid.blasterdemo.view.base.RfidActivity;
import com.hanmiit.app.rfid.blasterdemo.view.base.RfidMaskActivity;
import com.hanmiit.app.rfid.blasterdemo.R;
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
import com.hanmiit.lib.ATRfidManager;
import com.hanmiit.lib.ATRfidReader;

public class MainActivity extends Activity implements OnClickListener, ATRfidEventListener {

	private static final String TAG = MainActivity.class.getSimpleName();

	private static final String LOG_PATH = "Log";
	private static final String LOG_PREFIX = "RFBlasterDemo";
	private static final boolean ENABLE_LOG = false;
	private static final boolean ENABLE_LOG_FILE = false;

	private static final String APP_NAME = "rfid.reader.demo";
	private static final String KEY_DEVICE_ADDRESS = "device_address";
	private static final String KEY_BATTERY_INTERVAL = "battery_interval";
	private static final String KEY_MASK_TYPE = "mask_type";
	private static final String KEY_BARCODE_CHARSET = "barcode_charset";

	private static final String DEFAULT_DEVICE_ADDRESS = null;
	private static final int DEFAULT_BATTERY_INTERVAL = 10;
	private static final int DEFAULT_MASK_TYPE = 0;
	private static final int DEFAULT_OPERATION_TIME = 0;
	private static final int MAX_LOGO_TOUCH_COUNT = 5;
	private static final String DEFAULT_BARCODE_CHARSET = "UTF-8";

	private static final int VIEW_INVENTORY = 1000;
	private static final int VIEW_FILTER_INVENTORY = 1001;
	private static final int VIEW_STORED_TAG = 1002;
	private static final int VIEW_OPTION = 1006;
	private static final int VIEW_BARCODE_DEMO = 1007;
	private static final int VIEW_BARCODE_OPTION = 1008;

	private static final int RESULT_DISCONNECTED = Activity.RESULT_FIRST_USER + 1;
	
	private static final boolean VISIBLE_BLE_CONNECT = false;

	private PowerManager.WakeLock wakeLock = null;

	private ATRfidReader mReader = null;

	private TextView txtDemoVersion;
	private TextView txtFirmwareVersion;
	private TextView txtAddress;
	private LinearLayout layoutBleVersion;
	private TextView txtBleVersion;
	private Button btnInventory;
	private Button btnFilterInventory;
	private Button btnStoredTag;
	private Button btnReadMemory;
	private Button btnWriteMemory;
	private Button btnTagAccess;
	private Button btnOption;
	private Button btnBarcodeDemo;
	private Button btnBarcodeOption;
	private ImageView imgBattery;
	private ImageView imgLogo;

	private String mDeviceAddress;
	private int mBatteryInterval;
	private MaskType mMaskType;
	private boolean mKeyAction;
	private int mOperationTime;
	private Charset mCharset;

	private MenuItem mnuConnectDevice;
	private MenuItem mnuConnectNewDevice;
	private MenuItem mnuConnectBleDevice;
	private MenuItem mnuDisconnectDevice;

	private Timer timerCheckBattery;
	
	private int mLogoTouchCount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if(ENABLE_LOG) 
			ATLog.setLogEnabled(true);

		if(ENABLE_LOG_FILE)
			ATLog.startUp(LOG_PATH, LOG_PREFIX);
		
		ATLog.i(TAG, "INFO. onCreate()");

		mOperationTime = DEFAULT_OPERATION_TIME;
		mLogoTouchCount = 0;
		
		// Setup Alway Wakeup
		setupAlwaysWakeup();

		// Load Configuration
		loadConfig();

		// Initialize Widget
		initWidgets();

		// Initialize RFID Reader
		initATRfidReader();

		// Display Demo Version
		displayVersion();
	}

	@Override
	protected void onPause() {

		ATLog.i(TAG, "INFO. onPause()");

		saveConfig();
		stopCheckBattery();
		super.onPause();
	}

	@Override
	protected void onResume() {

		ATLog.i(TAG, "INFO. onResume()");

		if (mReader.getState() == ConnectionState.Connected) {
			startCheckBattery();
		}
		
		super.onResume();
	}

	@Override
	protected void onDestroy() {

		ATLog.i(TAG, "INFO. onDestroy()");

		saveConfig();
		if (timerCheckBattery != null) {
			timerCheckBattery.cancel();
			timerCheckBattery = null;
		}

		// Destroy Rfid Reader
		ATRfidManager.onDestroy();

		defaultWakeup();

		ATLog.shutdown();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		ATLog.d(TAG, "DEBUG. onActivityResult(%s, %s)", getRequestCode(requestCode), getResultCode(resultCode));

		switch (requestCode) {
		case VIEW_INVENTORY:
		case VIEW_FILTER_INVENTORY:
		case VIEW_STORED_TAG:
		case AccessActivity.VIEW_READ_MEMORY:
		case AccessActivity.VIEW_WRITE_MEMORY:
		case AccessActivity.VIEW_TAG_ACCESS:
			switch (resultCode) {
			case RESULT_DISCONNECTED:
				setDisconnectedState();
				break;
			default:
				// Disable use key action
				try {
					mReader.setUseKeyAction(false);
					if (Version.isLaster(Version.V7_2_5_13))
						mReader.setOperationMode(OperationMode.Normal);
				} catch (Exception e) {
					ATLog.e(TAG, e, "ERROR. onActivityResult(%s, %s) - Failed to disable use key action", 
							getRequestCode(requestCode), getResultCode(resultCode));
				}
				break;
			}
			break;
		case VIEW_OPTION:
			switch (resultCode) {
			case RESULT_DISCONNECTED:
				setDisconnectedState();
				break;
			case RESULT_OK:
				if (requestCode == VIEW_OPTION && data != null) {
					mBatteryInterval = data.getIntExtra(KEY_BATTERY_INTERVAL, DEFAULT_BATTERY_INTERVAL);
					int code = data.getIntExtra(KEY_MASK_TYPE, DEFAULT_MASK_TYPE);
					mMaskType = MaskType.valueOf(code);
					mKeyAction = data.getBooleanExtra(RfidActivity.KEY_ACTION, RfidActivity.DEFAULT_KEY_ACTION);
				}
				break;
			}
			break;
		case VIEW_BARCODE_DEMO:
		case VIEW_BARCODE_OPTION:
			switch (resultCode) {
			case RESULT_DISCONNECTED:
				setDisconnectedState();
				break;
			default:
				// Disable Barcode Mode
				try {
					mReader.setUseKeyAction(false);
					if (Version.isLaster(Version.V7_2_5_13))
						mReader.setOperationMode(OperationMode.Normal);
					else
						mReader.setBarcodeMode(false, 0);
				} catch (Exception e) {
					ATLog.e(TAG, e, "ERROR. onActivityResult(%s, %s) - Failed to change barcode mode", 
							getRequestCode(requestCode), getResultCode(resultCode));
				}
				break;
			}
			break;
		case ATRfidManager.REQUEST_DEVICE_LIST:
		case ATRfidManager.REQUEST_BLE_DEVICE_LIST:
		case ATRfidManager.REQUEST_ENABLE_BLUETOOTH:
			ATRfidManager.onActivityResult(requestCode, resultCode, data);
			if ((requestCode == ATRfidManager.REQUEST_DEVICE_LIST
					|| requestCode == ATRfidManager.REQUEST_BLE_DEVICE_LIST) && resultCode == Activity.RESULT_OK) {
				WaitDialog.show(this, R.string.connecting_reader, new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						// Disconnect Device
						stopCheckBattery();
						mReader.disconnectDevice();
					}

				});
			}
			break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		ATLog.i(TAG, "INFO. onCreateOptionsMenu()");

		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		ATLog.i(TAG, "INFO. onPrepareOptionsMenu()");

		mnuConnectDevice = menu.findItem(R.id.menu_connect_bt_device);
		mnuConnectNewDevice = menu.findItem(R.id.menu_connect_new_bt_device);
		mnuConnectBleDevice = menu.findItem(R.id.menu_connect_ble_device);
		mnuConnectBleDevice.setVisible(VISIBLE_BLE_CONNECT);
		mnuDisconnectDevice = menu.findItem(R.id.menu_disconnect);

		visibleMenuItem(mReader.getState());
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		ATLog.i(TAG, "INFO. onOptionsItemSelected(%s)", getMenuId(id));

		switch (id) {
		case R.id.menu_connect_bt_device:
			// Connect Most Recent Device
			if (!ATRfidManager.connectMostRecentDevice()) {
				ATRfidManager.openDeviceListActivity(this);
			} else {
				WaitDialog.show(this, R.string.connecting_reader);
			}
			return true;
		case R.id.menu_connect_new_bt_device:
			// Open Device List
			ATRfidManager.openDeviceListActivity(this);
			return true;
		case R.id.menu_connect_ble_device:
			// Open BLE Device List
			ATRfidManager.openBleDeviceListActivity(this);
			return true;
		case R.id.menu_disconnect:
			// Disconnect Device
			stopCheckBattery();
			mReader.disconnectDevice();
			WaitDialog.show(this, R.string.disconnecting_reader);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		Intent intent;
		int id = v.getId();

		ATLog.i(TAG, "INFO. onClick(%s)", getOnClickId(id));

		switch (id) {
		case R.id.inventory:
			mLogoTouchCount = 0;
			intent = new Intent(this, InventoryActivity.class);
			intent.putExtra(RfidMaskActivity.KEY_MASK_TYPE, mMaskType.getCode());
			intent.putExtra(RfidActivity.KEY_ACTION, mKeyAction);
			startActivityForResult(intent, VIEW_INVENTORY);
			break;
		case R.id.filter_inventory:
			mLogoTouchCount = 0;
			intent = new Intent(this, FilterInventoryActivity.class);
			intent.putExtra(RfidMaskActivity.KEY_MASK_TYPE, mMaskType.getCode());
			intent.putExtra(RfidActivity.KEY_ACTION, mKeyAction);
			startActivityForResult(intent, VIEW_FILTER_INVENTORY);
			break;
		case R.id.stored_tag:
			mLogoTouchCount = 0;
			intent = new Intent(this, StoredTagActivity.class);
			intent.putExtra(RfidActivity.KEY_ACTION, mKeyAction);
			startActivityForResult(intent, VIEW_STORED_TAG);
			break;
		case R.id.read_memory:
			mLogoTouchCount = 0;
			intent = new Intent(this, ReadMemoryActivity.class);
			intent.putExtra(RfidMaskActivity.KEY_MASK_TYPE, mMaskType.getCode());
			intent.putExtra(RfidActivity.KEY_ACTION, mKeyAction);
			startActivityForResult(intent, AccessActivity.VIEW_READ_MEMORY);
			break;
		case R.id.write_memory:
			mLogoTouchCount = 0;
			intent = new Intent(this, WriteMemoryActivity.class);
			intent.putExtra(RfidMaskActivity.KEY_MASK_TYPE, mMaskType.getCode());
			intent.putExtra(RfidActivity.KEY_ACTION, mKeyAction);
			startActivityForResult(intent, AccessActivity.VIEW_WRITE_MEMORY);
			break;
		case R.id.tag_access:
			mLogoTouchCount = 0;
			intent = new Intent(this, TagAccessActivity.class);
			intent.putExtra(RfidMaskActivity.KEY_MASK_TYPE, mMaskType.getCode());
			intent.putExtra(RfidActivity.KEY_ACTION, mKeyAction);
			startActivityForResult(intent, AccessActivity.VIEW_TAG_ACCESS);
			break;
		case R.id.option:
			mLogoTouchCount = 0;
			intent = new Intent(this, OptionActivity.class);
			intent.putExtra(RfidMaskActivity.KEY_MASK_TYPE, mMaskType.getCode());
			intent.putExtra(KEY_BATTERY_INTERVAL, mBatteryInterval);
			intent.putExtra(RfidActivity.KEY_ACTION, mKeyAction);
			startActivityForResult(intent, VIEW_OPTION);
			break;
		case R.id.barcode_demo:
			mLogoTouchCount = 0;
			intent = new Intent(this, BarcodeDemoActivity.class);
			intent.putExtra(RfidActivity.KEY_ACTION, mKeyAction);
			startActivityForResult(intent, VIEW_BARCODE_DEMO);
			break;
		case R.id.barcode_option:
			mLogoTouchCount = 0;
			intent = new Intent(this, BarcodeOptionActivity.class);
			intent.putExtra(RfidActivity.KEY_ACTION, mKeyAction);
			startActivityForResult(intent, VIEW_BARCODE_OPTION);
			break;
		case R.id.app_logo:
			if (mReader == null) return;
			if (mReader.getState() != ConnectionState.Connected)
				return;
			mLogoTouchCount++;
			if (mLogoTouchCount > MAX_LOGO_TOUCH_COUNT) {
				mLogoTouchCount = 0;
				showDebugOption();
			}
			break;
		default:
			return;
		}
	}

	@Override
	public void onStateChanged(ATRfidReader reader, ConnectionState state) {

		ATLog.i(TAG, "EVENT. onConnectionStateChanged(%s)", state);

		switch (state) {
		case Disconnected:
			if (mReader.getResultCode() == ResultCode.NotSupportFirmware) {
				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setTitle(R.string.system_alert);
				alert.setIcon(android.R.drawable.ic_dialog_alert);
				alert.setMessage(R.string.not_support_firmware);
				alert.setPositiveButton(R.string.action_ok, null);
				alert.show();
			}
			setDisconnectedState();
			break;
		case Connecting:
		case Listen:
			enableMenuButtons(false);
			imgLogo.setImageResource(R.drawable.ic_connecting_logo);
			break;
		case Connected:
			// Get Firmware Version
			String versionName = mReader.getFirmwareVersion();
			txtFirmwareVersion.setText(versionName);
			String bleVersion = mReader.getBleVersion();
			mDeviceAddress = mReader.getAddress();
			txtAddress.setText(mDeviceAddress);
			if (bleVersion.length() > 0) {
				layoutBleVersion.setVisibility(View.VISIBLE);
				txtBleVersion.setText(bleVersion);
			} else {
				layoutBleVersion.setVisibility(View.INVISIBLE);
			}
			checkBattery();
			saveConfig();
			startCheckBattery();
			mReader.setCharset(mCharset);
			WaitDialog.hide();
			visibleMenuItem(state);
			imgLogo.setImageResource(R.drawable.ic_connected_logo);
			enableMenuButtons(true);
			break;
		}
	}

	@Override
	public void onActionChanged(ATRfidReader reader, ActionState action) {
		ATLog.i(TAG, "EVENT. onActionChanged(%s)", action);
	}

	@Override
	public void onReadedTag(ATRfidReader reader, ActionState action, String tag, float rssi, float phase) {
		ATLog.i(TAG, "EVENT. onReadedTag(%s, [%s], %.2f, %.2f)", action, tag, rssi, phase);
	}

	@Override
	public void onAccessResult(ATRfidReader reader, ResultCode code, ActionState action, String epc, String data,
			float rssi, float phase) {
		ATLog.i(TAG, "EVENT. onAccessResult(%s, %s, [%s], [%s], %.2f, %.2f)", code, action, epc, data, rssi, phase);
	}

	@Override
	public void onCommandComplete(ATRfidReader reader, CommandType command) {
		ATLog.i(TAG, "EVENT. onCommandComplete(%s)", command);
	}

	@Override
	public void onLoadTag(ATRfidReader reader, String tag) {
		ATLog.i(TAG, "EVENT. onLoadTag([%s])", tag);
	}

	@Override
	public void onDebugMessage(ATRfidReader reader, String msg) {
		ATLog.i(TAG, "EVENT. onDebugMessage (%s)", msg);
	}

	@Override
	public void onDetactBarcode(ATRfidReader reader, BarcodeType type, String codeId, String barcode) {
		ATLog.i(TAG, "EVENT. onDetactBarcode (%s, %s, [%s])", type, codeId, barcode);
	}

	@Override
	public void onRemoteKeyStateChanged(ATRfidReader reader, RemoteKeyState state) {
		ATLog.i(TAG, "EVENT. onRemoteKeyStateChanged (%s)", state);
	}

	// Initialize Widgets
	private void initWidgets() {

		txtDemoVersion = (TextView) findViewById(R.id.demo_version);
		txtFirmwareVersion = (TextView) findViewById(R.id.firmware_version);
		txtAddress = (TextView) findViewById(R.id.address);
		layoutBleVersion = (LinearLayout)findViewById(R.id.layout_ble_firmware_version);
		txtBleVersion = (TextView)findViewById(R.id.ble_firmware_version);
		btnInventory = (Button) findViewById(R.id.inventory);
		btnInventory.setOnClickListener(this);
		btnFilterInventory = (Button) findViewById(R.id.filter_inventory);
		btnFilterInventory.setOnClickListener(this);
		btnStoredTag = (Button) findViewById(R.id.stored_tag);
		btnStoredTag.setOnClickListener(this);
		btnReadMemory = (Button) findViewById(R.id.read_memory);
		btnReadMemory.setOnClickListener(this);
		btnWriteMemory = (Button) findViewById(R.id.write_memory);
		btnWriteMemory.setOnClickListener(this);
		btnTagAccess = (Button) findViewById(R.id.tag_access);
		btnTagAccess.setOnClickListener(this);
		btnOption = (Button) findViewById(R.id.option);
		btnOption.setOnClickListener(this);
		btnBarcodeDemo = (Button) findViewById(R.id.barcode_demo);
		btnBarcodeDemo.setOnClickListener(this);
		btnBarcodeOption = (Button) findViewById(R.id.barcode_option);
		btnBarcodeOption.setOnClickListener(this);
		imgBattery = (ImageView) findViewById(R.id.battery);
		imgLogo = (ImageView) findViewById(R.id.app_logo);
		imgLogo.setOnClickListener(this);

		ATLog.i(TAG, "INFO. initWidgets()");
	}

	// Display Application Verserion
	private void displayVersion() {
		String versionName = "";
		String packageName = getPackageName();

		// Get Application Version
		try {
			versionName = getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA).versionName;
		} catch (NameNotFoundException e) {
			versionName = "";
		}
		txtDemoVersion.setText(versionName);

		ATLog.i(TAG, "INFO. displayVersion() - [%s]", versionName);
	}

	// Setup Always Wakeup
	@SuppressWarnings("deprecation")
	private void setupAlwaysWakeup() {
		if (wakeLock != null)
			return;

		PowerManager powerManager = (PowerManager) this.getSystemService(Activity.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, APP_NAME);
		wakeLock.acquire();

		ATLog.i(TAG, "INFO. setupAlwayWakeup()");
	}

	// Set Default Wakeup
	private void defaultWakeup() {
		if (wakeLock == null)
			return;

		wakeLock.release();
		wakeLock = null;

		ATLog.i(TAG, "INFO. defaultWakeup()");
	}

	// Initialize RFID Reader
	private void initATRfidReader() {
		ATRfidManager.setContext(this);
		if (!ATRfidManager.isBluetoothSupported()) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle(R.string.system_error);
			dialog.setIcon(android.R.drawable.ic_dialog_alert);
			dialog.setMessage(R.string.bluetooth_not_supported_message);
			dialog.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					ATLog.i(TAG, "INFO. initATRfidReader()$onClick()");
					finish();
				}

			});
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					ATLog.i(TAG, "INFO. initATRfidReader()$onCancel()");
					finish();
				}

			});
			dialog.show();
			return;
		}

		ATRfidManager.checkEnableBluetooth(this);

		mReader = ATRfidManager.getInstance();
		mReader.setAddress(mDeviceAddress);
		mReader.setEventListener(this);

		ATLog.i(TAG, "INFO. initATRfidReader()");
	}

	// Enable/Disable All Menu Button
	private void enableMenuButtons(boolean enabled) {
		btnInventory.setEnabled(enabled);
		btnFilterInventory.setEnabled(enabled);
		btnReadMemory.setEnabled(enabled);
		btnWriteMemory.setEnabled(enabled);
		btnTagAccess.setEnabled(enabled);
		btnStoredTag.setEnabled(enabled);
		btnOption.setEnabled(enabled);
		btnBarcodeDemo.setEnabled(enabled && mReader.isBarcodeModule());
		btnBarcodeOption.setEnabled(enabled && mReader.isBarcodeModule());
		ATLog.i(TAG, "INFO. enableMenuButtons(%s)", enabled);
	}

	// Visible Menu Item
	private void visibleMenuItem(ConnectionState state) {
		boolean enabled = ATRfidManager.isBluetoothLeSupported(this);
		mnuConnectDevice.setVisible(state == ConnectionState.Disconnected);
		mnuConnectDevice.setEnabled(enabled);
		mnuConnectNewDevice.setVisible(state == ConnectionState.Disconnected);
		mnuConnectNewDevice.setEnabled(enabled);
		mnuConnectBleDevice.setVisible(VISIBLE_BLE_CONNECT && state == ConnectionState.Disconnected);
		mnuConnectBleDevice.setEnabled(enabled);
		mnuDisconnectDevice.setVisible(state != ConnectionState.Disconnected);
		ATLog.i(TAG, "INFO. VisibleMenuItem(%s)", state);
	}

	// Load Configuration
	private void loadConfig() {
		SharedPreferences prefs = getSharedPreferences(APP_NAME, MODE_PRIVATE);
		mDeviceAddress = prefs.getString(KEY_DEVICE_ADDRESS, DEFAULT_DEVICE_ADDRESS);
		ATLog.d(TAG, "DEBUG. loadConfig() - Device Address : [%s]", mDeviceAddress);
		mBatteryInterval = prefs.getInt(KEY_BATTERY_INTERVAL, DEFAULT_BATTERY_INTERVAL);
		if (mBatteryInterval > 60) mBatteryInterval = DEFAULT_BATTERY_INTERVAL;
		ATLog.d(TAG, "DEBUG. loadConfig() - Batery Check Interval : [%d]", mBatteryInterval);
		mMaskType = MaskType.valueOf(prefs.getInt(KEY_MASK_TYPE, DEFAULT_MASK_TYPE));
		ATLog.d(TAG, "DEBUG. loadConfig() - Mask Type : [%s]", mMaskType);
		mKeyAction = prefs.getBoolean(RfidActivity.KEY_ACTION, RfidActivity.DEFAULT_KEY_ACTION);
		ATLog.d(TAG, "DEBUG. loadConfig(); - Key Action : %s", mKeyAction);
		mCharset = Charset.forName(prefs.getString(KEY_BARCODE_CHARSET, DEFAULT_BARCODE_CHARSET));

		ATLog.i(TAG, "INFO. loadConfig()");
	}

	// Save Configuration
	private void saveConfig() {
		SharedPreferences prefs = getSharedPreferences(APP_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();

		editor.putString(KEY_DEVICE_ADDRESS, mDeviceAddress);
		ATLog.d(TAG, "DEBUG. saveConfig() - Device Address : [%s]", mDeviceAddress);
		editor.putInt(KEY_BATTERY_INTERVAL, mBatteryInterval);
		ATLog.d(TAG, "DEBUG. saveConfig() - Batery Check Interval : [%d]", mBatteryInterval);
		editor.putInt(KEY_MASK_TYPE, mMaskType.getCode());
		ATLog.d(TAG, "DEBUG. saveConfig() - Mask Type : [%s]", mMaskType);
		editor.putBoolean(RfidActivity.KEY_ACTION, mKeyAction);
		ATLog.d(TAG, "DEBUG. saveConfig() - Key Action : %s", mKeyAction);
		editor.putString(KEY_BARCODE_CHARSET, mReader.getCharset().name());
		ATLog.d(TAG, "DEBUG. saveConfig(); - Charset : %s", mReader.getCharset().name());

		editor.commit();

		ATLog.i(TAG, "INFO. saveConfig()");
	}

	private void startCheckBattery() {
		ATLog.i(TAG, "INFO. startCheckBattery()");
		int interval = mBatteryInterval * 1000;
		timerCheckBattery = new Timer();
		timerCheckBattery.schedule(new TimerTask() {

			@Override
			public void run() {
				runOnUiThread(mActionCheckBattery);
			}

		}, interval, interval);
	}

	private void stopCheckBattery() {
		if (timerCheckBattery == null)
			return;
		timerCheckBattery.cancel();
		timerCheckBattery = null;
		ATLog.i(TAG, "INFO. stopCheckBattery()");
	}

	private Runnable mActionCheckBattery = new Runnable() {

		@Override
		public void run() {
			if (timerCheckBattery != null)
				checkBattery();
		}

	};

	private void checkBattery() {

		int battery = 0;

		try {
			battery = mReader.getBatteryStatus();
		} catch (ATRfidReaderException e) {
			battery = 0;
		}

		switch (battery) {
		case 0:
			imgBattery.setImageResource(R.drawable.battery0);
			break;
		case 1:
			imgBattery.setImageResource(R.drawable.battery1);
			break;
		case 2:
			imgBattery.setImageResource(R.drawable.battery2);
			break;
		case 3:
			imgBattery.setImageResource(R.drawable.battery3);
			break;
		case 4:
			imgBattery.setImageResource(R.drawable.battery4);
			break;
		default:
			return;
		}
		ATLog.i(TAG, "INFO. checkBattery() - [%d]", battery);
	}

	private void setDisconnectedState() {

		mReader.disconnectDevice();

		stopCheckBattery();
		txtFirmwareVersion.setText("");
		txtAddress.setText("");
		layoutBleVersion.setVisibility(View.INVISIBLE);
		imgBattery.setImageResource(R.drawable.battery0);
		enableMenuButtons(false);
		imgLogo.setImageResource(R.drawable.ic_disconnected_logo);
		visibleMenuItem(ConnectionState.Disconnected);
		WaitDialog.hide();
		
		ATLog.i(TAG, "INFO. setDisconnectedState()");
	}
	
	private void showDebugOption() {
		final CharSequence[] items = {"Normal Mode", "Debug Mode" };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.debug_mode_title);
		builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				boolean enabled = false;
				switch (which) {
				case 0:	// Normal Mode
					enabled = false;
					break;
				case 1: // Debug Mode
					enabled = true;
					break;
				}
				try {
					mReader.setDebugMode(enabled);
				} catch (ATRfidReaderException e) {
					ATLog.e(TAG, e, "ERROR. showDebugOption() - Failed to set debug mode");
				}
				dialog.dismiss();
			}
		});
		builder.setCancelable(false);
		builder.show();
		ATLog.i(TAG, "INFO. showDebugOption()");
	}

	private String getRequestCode(int requestCode) {
		switch (requestCode) {
		case VIEW_INVENTORY:
			return "VIEW_INVENTORY";
		case VIEW_FILTER_INVENTORY:
			return "VIEW_FILTER_INVENTORY";
		case VIEW_STORED_TAG:
			return "VIEW_STORED_TAG";
		case AccessActivity.VIEW_READ_MEMORY:
			return "VIEW_READ_MEMORY";
		case AccessActivity.VIEW_WRITE_MEMORY:
			return "VIEW_WRITE_MEMORY";
		case AccessActivity.VIEW_TAG_ACCESS:
			return "VIEW_TAG_ACCESS";
		case VIEW_OPTION:
			return "VIEW_OPTION";
		case VIEW_BARCODE_DEMO:
			return "VIEW_BARCODE_DEMO";
		case VIEW_BARCODE_OPTION:
			return "VIEW_BARCODE_OPTION";
		case ATRfidManager.REQUEST_DEVICE_LIST:
			return "REQUEST_DEVICE_LIST";
		case ATRfidManager.REQUEST_BLE_DEVICE_LIST:
			return "REQUEST_BLE_DEVICE_LIST";
		case ATRfidManager.REQUEST_ENABLE_BLUETOOTH:
			return "REQUEST_ENABLE_BLUETOOTH";
		}
		return "";
	}

	private String getResultCode(int resultCode) {
		switch (resultCode) {
		case Activity.RESULT_CANCELED:
			return "RESULT_CANCELED";
		case Activity.RESULT_OK:
			return "RESULT_OK";
		case Activity.RESULT_FIRST_USER:
			return "RESULT_FIRST_USER";
		case RESULT_DISCONNECTED:
			return "RESULT_DISCONNECTED";
		}
		return "";
	}

	private String getMenuId(int id) {
		switch (id) {
		case R.id.menu_connect_bt_device:
			return "menu_connect_bt_device";
		case R.id.menu_connect_new_bt_device:
			return "menu_connect_new_bt_device";
		case R.id.menu_connect_ble_device:
			return "menu_connect_ble_device";
		case R.id.menu_disconnect:
			return "menu_disconnect";
		}
		return "";
	}

	private String getOnClickId(int id) {
		switch (id) {
		case R.id.inventory:
			return "inventory";
		case R.id.filter_inventory:
			return "filter_inventory";
		case R.id.stored_tag:
			return "stored_tag";
		case R.id.read_memory:
			return "read_memory";
		case R.id.write_memory:
			return "write_memory";
		case R.id.tag_access:
			return "tag_access";
		case R.id.option:
			return "option";
		case R.id.barcode_demo:
			return "barcode_demo";
		case R.id.barcode_option:
			return "barcode_option";
		case R.id.app_logo:
			return "app_logo";
		}
		return "";
	}
}
