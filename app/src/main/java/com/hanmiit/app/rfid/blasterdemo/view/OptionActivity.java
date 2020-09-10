package com.hanmiit.app.rfid.blasterdemo.view;

import java.util.Locale;

import com.hanmiit.lib.rfid.exception.ATRfidReaderException;
import com.hanmiit.lib.rfid.type.BuzzerState;
import com.hanmiit.lib.rfid.type.CommandType;
import com.hanmiit.lib.rfid.type.LbtItem;
import com.hanmiit.lib.rfid.type.ResultCode;
import com.hanmiit.lib.util.Version;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.adapter.LbtChannelAdapter;
import com.hanmiit.app.rfid.blasterdemo.adapter.SpinnerAdapter;
import com.hanmiit.app.rfid.blasterdemo.dialog.WaitDialog;
import com.hanmiit.app.rfid.blasterdemo.filter.InputFilterMinMax;
import com.hanmiit.app.rfid.blasterdemo.type.MaskType;
import com.hanmiit.app.rfid.blasterdemo.util.ResUtil;
import com.hanmiit.app.rfid.blasterdemo.view.base.RfidActivity;
import com.hanmiit.lib.ATRfidReader;
import com.hanmiit.lib.diagnostics.ATLog;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class OptionActivity extends RfidActivity implements OnClickListener {

	private static final String TAG = OptionActivity.class.getSimpleName();

	private static final int EVENT_LOAD_OPTION = 1000;
	private static final String KEY_BATTERY_INTERVAL = "battery_interval";
	private static final String KEY_MASK_TYPE = "mask_type";

	private static final int DEFAULT_BATTERY_INTERVAL = 10;
	private static final int DEFAULT_MASK_TYPE = 1;
	private static final int MIN_AUTO_OFF_TIME = 30;

	private TextView txtBuzzer;
	private TextView txtInventoryTime;
	private TextView txtIdleTime;
	private TextView txtAutoOffTime;
	private TextView txtKeyAction;
	private TextView txtBatteryInterval;
	private TextView txtMaskType;
	private Button btnLbtChannel;
	private Button btnSave;
	private Button btnDefault;
	private boolean mIsEnabled;

	private Handler mEvent;

	private BuzzerState mBuzzer;
	private int mInventoryTime;
	private int mIdleTime;
	private int mAutoOffTime;
	private int mBatteryInterval;
	private MaskType mMaskType;
	private MaskType mOldMaskType;

	private boolean mIsSaveTable;

	private FreqSlotItem[] mFreqSlots;
	private LbtItem[] mFreqTable;
	
	private boolean mIsSaving;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_option);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		ATLog.i(TAG, "INFO. onCreate()");

		mIsSaveTable = false;
		mIsSaving = false;
		
		// Create Reader
		createReader();

		Intent intent = getIntent();
		mBatteryInterval = intent.getIntExtra(KEY_BATTERY_INTERVAL, DEFAULT_BATTERY_INTERVAL);
		int code = intent.getIntExtra(KEY_MASK_TYPE, DEFAULT_MASK_TYPE);
		mMaskType = MaskType.valueOf(code);
		mOldMaskType = mMaskType;

		// Initialize Widget
		initWidgets();

		// Initialize Reader
		initReader();

		// Enable action widget
		enableActionWidgets(false);

		mEvent = new Handler();
	}

	@Override
	protected void onDestroy() {

		ATLog.i(TAG, "INFO. onDestroy()");

		// Destroy Reader
		destroyReader();

		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();

		// Load Option
		WaitDialog.show(this, R.string.load_option);
		new Thread(mLoadOption).start();
		
		ATLog.i(TAG, "INFO. onStart()");
	}

	@Override
	protected void onStop() {

		ATLog.i(TAG, "INFO. onStart()");
		super.onStop();
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		// Set Key Action
		try {
			getReader().setUseKeyAction(false);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. onStart() - Failed to set use key action");
		}
		
		ATLog.i(TAG, "INFO. onResume()");
	}

	@Override
	protected void onPause() {
		
		if (!mIsSaving) {
			// Set Key Action
			try {
				getReader().setUseKeyAction(false);
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. onStart() - Failed to set use key action");
			}
		}

		ATLog.i(TAG, "INFO. onPause()");
		super.onPause();
	}

	@Override
	public void onClick(View v) {

		ATLog.i(TAG, "INFO. onClick(%s)", ResUtil.getId(v.getId()));

		switch (v.getId()) {
		case R.id.buzzer:
			showBuzzerDialog();
			break;
		case R.id.inventory_time:
			showInventoryTimeDialog();
			break;
		case R.id.idle_time:
			showIdleTimeDialog();
			break;
		case R.id.auto_off_time:
			showAutoOffTimeDialog();
			break;
		case R.id.key_action:
			showKeyActionDialog();
			break;
		case R.id.battery_interval:
			showBatteryIntervalDialog();
			break;
		case R.id.mask_type:
			showMaskTypeDialog();
			break;
		case R.id.lbt_channel:
			showLbtChannelDialog();
			break;
		case R.id.save:
			mIsSaving = true;
			// Enable action widget
			enableActionWidgets(false);
			// Save Option
			WaitDialog.show(this, R.string.save_option);
			new Thread(mSaveOption).start();
			break;
		case R.id.option_default:
			ResultCode res;

			enableActionWidgets(false);
			// Save Option
			WaitDialog.show(this, R.string.load_default_option);
			if ((res = getReader().defaultParameter()) != ResultCode.NoError) {
				ATLog.e(TAG, "ERROR. onClick(%s) - Failed to default parameter [%s]", ResUtil.getId(v.getId()), res);
				enableActionWidgets(false);
				WaitDialog.hide();
			}
			break;
		}
	}

	@Override
	public void onCommandComplete(ATRfidReader reader, CommandType command) {
		ATLog.i(TAG, "EVENT. onCommandComplete (%s)", command);

		if (command == CommandType.DefaultParameter) {
			new Thread(mLoadOption).start();
		}
	}

	// Asynchronous Load Option
	private Runnable mLoadOption = new Runnable() {

		@Override
		public void run() {
			ATLog.i(TAG, "+++ INFO. $mLoadOption.run()");

			try {
				mBuzzer = getReader().getBuzzer();
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. $mLoadOption.run() - Failed to get buzzer");
				mBuzzer = BuzzerState.Off;
			}
			try {
				mInventoryTime = getReader().getInventoryTime();
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. $mLoadOption.run() - Failed to get inventory time");
				mInventoryTime = 500;
			}
			try {
				mIdleTime = getReader().getIdleTime();
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. $mLoadOption.run() - Failed to get idle time");
				mIdleTime = 0;
			}
			try {
				mAutoOffTime = getReader().getAutoOffTime();
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. $mLoadOption.run() - Failed to get auto off time");
				mAutoOffTime = 30;
			}
			if (Version.isLaster(Version.VX_2_2_5)) {
				try {
					int[] slots = getReader().getLbtMask();
					mFreqSlots = new FreqSlotItem[slots.length];
					for (int i = 0; i < slots.length; i++) {
						String name = getReader().getLbtFreq(slots[i]);
						mFreqSlots[i] = new FreqSlotItem(slots[i], name);
					}
					mFreqTable = getReader().getLbt();
				} catch (ATRfidReaderException e) {
					ATLog.e(TAG, e, "ERROR. $mLoadOption.run() - Failed to get LBT table");
				}
			}
			mIsSaveTable = false;
			mEvent.post(new Runnable() {

				@Override
				public void run() {

					displayBuzzer();
					displayInventoryTime();
					displayIdleTime();
					displayAutoOffTime();
					displayKeyAction();
					displayBatteryInterval();
					displayMaskType();

					WaitDialog.hide();
					enableActionWidgets(true);
				}

			});
			mEvent.sendEmptyMessage(EVENT_LOAD_OPTION);
			ATLog.i(TAG, "--- INFO. $mLoadOption.run()");
		}

	};

	// Asynchronous Save Option
	private Runnable mSaveOption = new Runnable() {

		@Override
		public void run() {
			ATLog.i(TAG, "+++ INFO. $mSaveOption.run()");

			// Set Buzzer
			try {
				getReader().setBuzzer(mBuzzer);
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. $mSaveOption.run() - Failed to set buzzer (%s)", mBuzzer);
			}
			// Set Inventory Time
			try {
				getReader().setInventoryTime(mInventoryTime);
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. $mSaveOption.run() - Failed to set inventory time (%d)", mInventoryTime);
			}
			// Set Idle Time
			try {
				getReader().setIdleTime(mIdleTime);
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. $mSaveOption.run() - Failed to set idle time (%d)", mIdleTime);
			}
			// Set Auto Off Time
			try {
				getReader().setAutoOffTime(mAutoOffTime);
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. $mSaveOption.run() - Failed to set auto off time (%d)", mAutoOffTime);
			}
			if (mOldMaskType != mMaskType) {
				// ------------------------------------------------------------
				// Clear Selection Mask
				try {
					getReader().clearSelectionMask();
				} catch (ATRfidReaderException e) {
					ATLog.e(TAG, e, "--- ERROR. $mSaveOption.run() - Failed to clear selection mask");
				}
				ATLog.d(TAG, "DEBUG. $mInitReader.run() - clear selection mask");

				// ------------------------------------------------------------
				// Clear EPC Mask
				try {
					getReader().clearEpcMask();
				} catch (ATRfidReaderException e) {
					ATLog.e(TAG, e, "--- ERROR. $mSaveOption.run() - Failed to clear epc mask");
				}
			}
			// Set LBT Table
			if (Version.isLaster(Version.VX_2_2_5)) {
				if (mIsSaveTable) {
					try {
						getReader().setLbt(mFreqTable);
					} catch (ATRfidReaderException e) {
						ATLog.e(TAG, e, "ERROR. $mSaveOption.run() - Failed to set LBT table");
					}
				}
			}
			// Set Key Action
			try {
				getReader().setUseKeyAction(false);
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. onStart() - Failed to set use key action");
			}
			mEvent.post(new Runnable() {

				@Override
				public void run() {
					WaitDialog.hide();
					Intent intent = new Intent();
					intent.putExtra(KEY_BATTERY_INTERVAL, mBatteryInterval);
					intent.putExtra(KEY_MASK_TYPE, mMaskType.getCode());
					intent.putExtra(KEY_ACTION, getKeyAction());
					setResult(Activity.RESULT_OK, intent);
					finish();
				}

			});
			ATLog.i(TAG, "+++ INFO. $mSaveOption.run()");
		}

	};

	// Initialize Reader
	@Override
	protected void initReader() {
		ATLog.i(TAG, "INFO. initReader()");
	}

	// Initialize Widgets
	protected void initWidgets() {
		txtBuzzer = (TextView) findViewById(R.id.buzzer);
		txtBuzzer.setOnClickListener(this);
		txtInventoryTime = (TextView) findViewById(R.id.inventory_time);
		txtInventoryTime.setOnClickListener(this);
		txtIdleTime = (TextView) findViewById(R.id.idle_time);
		txtIdleTime.setOnClickListener(this);
		txtAutoOffTime = (TextView) findViewById(R.id.auto_off_time);
		txtAutoOffTime.setOnClickListener(this);
		txtKeyAction = (TextView) findViewById(R.id.key_action);
		txtKeyAction.setOnClickListener(this);
		txtBatteryInterval = (TextView) findViewById(R.id.battery_interval);
		txtBatteryInterval.setOnClickListener(this);
		txtMaskType = (TextView) findViewById(R.id.mask_type);
		txtMaskType.setOnClickListener(this);
		btnLbtChannel = (Button) findViewById(R.id.lbt_channel);
		btnLbtChannel.setOnClickListener(this);
		if (Version.isBefore(Version.VX_2_2_5)) {
			btnLbtChannel.setVisibility(View.GONE);
		}

		btnSave = (Button) findViewById(R.id.save);
		btnSave.setOnClickListener(this);

		btnDefault = (Button) findViewById(R.id.option_default);
		btnDefault.setOnClickListener(this);

		ATLog.i(TAG, "INFO. initWidgets()");
	}

	// Enable/Disable Action Widgets
	protected void enableActionWidgets(boolean enabled) {
		mIsEnabled = enabled;
		btnSave.setEnabled(enabled);
		btnDefault.setEnabled(enabled);

		ATLog.i(TAG, "INFO. enableActionWidgets(%s)", enabled);
	}

	private void displayBuzzer() {
		txtBuzzer.setText(mBuzzer.toString());
		ATLog.i(TAG, "INFO. displayBuzzer() - [%s]", mBuzzer);
	}

	private void displayInventoryTime() {
		txtInventoryTime.setText(String.format(Locale.US, "%d ms", mInventoryTime));
		ATLog.i(TAG, "INFO. displayInventoryTime() - [%d]", mInventoryTime);
	}

	private void displayIdleTime() {
		txtIdleTime.setText(String.format(Locale.US, "%d ms", mIdleTime));
		ATLog.i(TAG, "INFO. displayIdleTime() - [%d]", mIdleTime);
	}

	private void displayAutoOffTime() {
		txtAutoOffTime.setText(String.format(Locale.US, "%d sec", mAutoOffTime));
		ATLog.i(TAG, "INFO. displayAutoOffTime() - [%d]", mAutoOffTime);
	}

	private void displayKeyAction() {
		txtKeyAction.setText(getKeyAction() ? "Used" : "Not Used");
		ATLog.i(TAG, "INFO. displayKeyAction() - [%s]", getKeyAction());
	}

	private void displayBatteryInterval() {
		txtBatteryInterval.setText(String.format(Locale.US, "%d sec", mBatteryInterval));
		ATLog.i(TAG, "INFO. displayBatteryInterval() - [%d]", mBatteryInterval);
	}

	private void displayMaskType() {
		txtMaskType.setText(mMaskType.toString());
		ATLog.i(TAG, "INFO. displayMaskType() - [%s]", mMaskType);
	}

	private void showBuzzerDialog() {
		if (!mIsEnabled)
			return;

		LinearLayout root = (LinearLayout) LinearLayout.inflate(this, R.layout.dialog_list_view, null);
		final ListView value = (ListView) root.findViewById(R.id.list);
		final SpinnerAdapter adapter = new SpinnerAdapter(this, android.R.layout.simple_list_item_single_choice);
		for (BuzzerState item : BuzzerState.values()) {
			adapter.addItem(item.getCode(), item.toString());
		}
		value.setAdapter(adapter);
		value.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.buzzer);
		builder.setView(root);
		builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				int position = value.getCheckedItemPosition();
				mBuzzer = BuzzerState.valueOf(adapter.getValue(position));
				displayBuzzer();
				ATLog.i(TAG, "INFO. showBuzzerDialog().$PositiveButton.onClick()");
			}
		});
		builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				ATLog.i(TAG, "INFO. showBuzzerDialog().$NegativeButton.onClick()");
			}
		});
		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				ATLog.i(TAG, "INFO. showBuzzerDialog().onCancel()");
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				int position = adapter.getPosition(mBuzzer.getCode());
				value.setItemChecked(position, true);
				value.setSelectionFromTop(position, 0);
				ATLog.i(TAG, "INFO. showBuzzerDialog().onShow()");
			}
		});
		dialog.show();
		ATLog.i(TAG, "INFO. showBuzzerDialog()");
	}

	private void showInventoryTimeDialog() {
		if (!mIsEnabled)
			return;

		LinearLayout root = (LinearLayout) LinearLayout.inflate(this, R.layout.dialog_input_unit, null);
		final EditText value = (EditText) root.findViewById(R.id.value);
		InputFilter[] filters = new InputFilter[] { new InputFilterMinMax(0, 10000) };
		value.setFilters(filters);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.inventory_time);
		builder.setView(root);
		builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					mInventoryTime = Integer.parseInt(value.getText().toString());
				} catch (Exception e) {
				}
				displayInventoryTime();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(value.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showInventoryTimeDialog().$PositiveButton.onClick()");
			}
		});
		builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(value.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showInventoryTimeDialog().$NegativeButton.onClick()");
			}
		});
		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(value.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showInventoryTimeDialog().onCancel()");
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				value.setText(String.format(Locale.US, "%d", mInventoryTime));
				value.selectAll();
				value.requestFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(value, InputMethodManager.SHOW_FORCED);
				ATLog.i(TAG, "INFO. showInventoryTimeDialog().onShow()");
			}
		});
		dialog.show();
		ATLog.i(TAG, "INFO. showInventoryTimeDialog()");
	}

	private void showIdleTimeDialog() {
		if (!mIsEnabled)
			return;

		LinearLayout root = (LinearLayout) LinearLayout.inflate(this, R.layout.dialog_input_unit, null);
		final EditText value = (EditText) root.findViewById(R.id.value);
		InputFilter[] filters = new InputFilter[] { new InputFilterMinMax(0, 10000) };
		value.setFilters(filters);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.idle_time);
		builder.setView(root);
		builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					mIdleTime = Integer.parseInt(value.getText().toString());
				} catch (Exception e) {
				}
				displayIdleTime();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(value.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showIdleTimeDialog().$PositiveButton.onClick()");
			}
		});
		builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(value.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showIdleTimeDialog().$NegativeButton.onClick()");
			}
		});
		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(value.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showIdleTimeDialog().onCancel()");
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				value.setText(String.format(Locale.US, "%d", mIdleTime));
				value.selectAll();
				value.requestFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(value, InputMethodManager.SHOW_FORCED);
				ATLog.i(TAG, "INFO. showIdleTimeDialog().onShow()");
			}
		});
		dialog.show();
		ATLog.i(TAG, "INFO. showIdleTimeDialog()");
	}

	private void showAutoOffTimeDialog() {
		if (!mIsEnabled)
			return;

		LinearLayout root = (LinearLayout) LinearLayout.inflate(this, R.layout.dialog_input_unit, null);
		final EditText value = (EditText) root.findViewById(R.id.value);
		final TextView unit = (TextView) root.findViewById(R.id.unit);
		unit.setText(R.string.unit_sec);
		InputFilter[] filters = new InputFilter[] { new InputFilterMinMax(0, 2000) };
		value.setFilters(filters);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.auto_off_time);
		builder.setView(root);
		builder.setPositiveButton(R.string.action_ok, null);
//		builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				try {
//					mAutoOffTime = Integer.parseInt(value.getText().toString());
//				} catch (Exception e) {
//					ATLog.e(TAG, e, "ERROR. showAutoOffTimeDialog().$PositiveButton.onClick() - Failed to parse auto off time");
//					return;
//				}
//				
//				displayAutoOffTime();
//				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//				imm.hideSoftInputFromWindow(value.getWindowToken(), 0);
//				ATLog.i(TAG, "INFO. showAutoOffTimeDialog().$PositiveButton.onClick()");
//			}
//		});
		builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(value.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showAutoOffTimeDialog().$NegativeButton.onClick()");
			}
		});
		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(value.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showAutoOffTimeDialog().onCancel()");
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				final AlertDialog dlg = (AlertDialog)dialog;
				value.setText(String.format(Locale.US, "%d", mAutoOffTime));
				value.selectAll();
				value.requestFocus();
				Button positiveButton = dlg.getButton(AlertDialog.BUTTON_POSITIVE);
				positiveButton.setOnClickListener(new OnClickListener() {
					
					@SuppressLint("ShowToast")
					@Override
					public void onClick(View v) {
						int time = 0;
						try {
							time = Integer.parseInt(value.getText().toString());
						} catch (Exception e) {
							ATLog.e(TAG, e,
									"ERROR. showAutoOffTimeDialog().$PositiveButton.onClick() - Failed to parse auto off time");
							dlg.dismiss();
							return;
						}
						if (time < MIN_AUTO_OFF_TIME) {
							AlertDialog.Builder builder = new AlertDialog.Builder(OptionActivity.this);
							builder.setMessage(R.string.min_auto_off_time);
							builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									value.selectAll();
									value.requestFocus();
								}
							});
							builder.setCancelable(true);
							builder.show();
							return;
						}
						mAutoOffTime = time;
						displayAutoOffTime();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(value.getWindowToken(), 0);
						dlg.dismiss();
					}
				});
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(value, InputMethodManager.SHOW_FORCED);
				ATLog.i(TAG, "INFO. showAutoOffTimeDialog().onShow()");
			}
		});
		dialog.show();
		ATLog.i(TAG, "INFO. showAutoOffTimeDialog()");
	}

	private void showKeyActionDialog() {
		if (!mIsEnabled)
			return;

		LinearLayout root = (LinearLayout) LinearLayout.inflate(this, R.layout.dialog_list_view, null);
		final ListView value = (ListView) root.findViewById(R.id.list);
		final SpinnerAdapter adapter = new SpinnerAdapter(this, android.R.layout.simple_list_item_single_choice);
		adapter.addItem(0, "Not Used");
		adapter.addItem(1, "Used");
		value.setAdapter(adapter);
		value.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.key_action);
		builder.setView(root);
		builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				int position = value.getCheckedItemPosition();
				setKeyAction(adapter.getValue(position) > 0);
				displayKeyAction();
				ATLog.i(TAG, "INFO. showKeyActionDialog().$PositiveButton.onClick()");
			}
		});
		builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				ATLog.i(TAG, "INFO. showKeyActionDialog().$NegativeButton.onClick()");
			}
		});
		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				ATLog.i(TAG, "INFO. showKeyActionDialog().onCancel()");
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				int position = adapter.getPosition(getKeyAction() ? 1 : 0);
				value.setItemChecked(position, true);
				value.setSelectionFromTop(position, 0);
				ATLog.i(TAG, "INFO. showKeyActionDialog().onShow()");
			}
		});
		dialog.show();
		ATLog.i(TAG, "INFO. showKeyActionDialog()");
	}

	private void showBatteryIntervalDialog() {
		if (!mIsEnabled)
			return;

		LinearLayout root = (LinearLayout) LinearLayout.inflate(this, R.layout.dialog_input_unit, null);
		final TextView unit = (TextView) root.findViewById(R.id.unit);
		unit.setText(R.string.unit_sec);
		final EditText value = (EditText) root.findViewById(R.id.value);
		InputFilter[] filters = new InputFilter[] { new InputFilterMinMax(1, 60) };
		value.setFilters(filters);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.battery_interval);
		builder.setView(root);
		builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					mBatteryInterval = Integer.parseInt(value.getText().toString());
				} catch (Exception e) {

				}
				displayBatteryInterval();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(value.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showBatteryIntervalDialog().$PositiveButton.onClick()");
			}
		});
		builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(value.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showBatteryIntervalDialog().$NegativeButton.onClick()");
			}
		});
		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(value.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showBatteryIntervalDialog().onCancel()");
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				value.setText(String.format(Locale.US, "%d", mBatteryInterval));
				value.selectAll();
				value.requestFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(value, InputMethodManager.SHOW_FORCED);
				ATLog.i(TAG, "INFO. showBatteryIntervalDialog()");
			}
		});
		dialog.show();
		ATLog.i(TAG, "INFO. showBatteryIntervalDialog().onShow()");
	}

	private void showMaskTypeDialog() {
		if (!mIsEnabled)
			return;

		LinearLayout root = (LinearLayout) LinearLayout.inflate(this, R.layout.dialog_list_view, null);
		final ListView value = (ListView) root.findViewById(R.id.list);
		final SpinnerAdapter adapter = new SpinnerAdapter(this, android.R.layout.simple_list_item_single_choice);
		for (MaskType item : MaskType.values()) {
			adapter.addItem(item.getCode(), item.toString());
		}
		value.setAdapter(adapter);
		value.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.mask_type);
		builder.setView(root);
		builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					int position = value.getCheckedItemPosition();
					mMaskType = MaskType.valueOf(adapter.getValue(position));
				} catch (Exception e) {
				}
				displayMaskType();
				ATLog.i(TAG, "INFO. showMaskTypeDialog().$PositiveButton.onClick()");
			}
		});
		builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				ATLog.i(TAG, "INFO. showMaskTypeDialog().$NegativeButton.onClick()");
			}
		});
		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				ATLog.i(TAG, "INFO. showMaskTypeDialog().onCancel()");
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				int position = adapter.getPosition(mMaskType.getCode());
				value.setItemChecked(position, true);
				value.setSelectionFromTop(position, 0);
				ATLog.i(TAG, "INFO. showMaskTypeDialog().onShow()");
			}
		});
		dialog.show();
		ATLog.i(TAG, "INFO. showMaskTypeDialog()");
	}

	private void showLbtChannelDialog() {

		final LinearLayout root = (LinearLayout) LinearLayout.inflate(this, R.layout.dialog_list_view, null);
		final ListView table = (ListView) root.findViewById(R.id.list);
		final LbtChannelAdapter adapter = new LbtChannelAdapter(this);
		for (FreqSlotItem item : mFreqSlots) {
			adapter.addItem(item.getSlot(), item.getName());
		}
		table.setAdapter(adapter);
		table.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.lbt_channel_title);
		builder.setView(root);
		builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mFreqTable = adapter.getTable();
				mIsSaveTable = true;
				ATLog.i(TAG, "INFO. showLbtChannelDialog().$PositiveButton.onClick()");
			}
		});
		builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mIsSaveTable = false;
				ATLog.i(TAG, "INFO. showLbtChannelDialog().$NegativeButton.onClick()");
			}
		});
		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				mIsSaveTable = false;
				ATLog.i(TAG, "INFO. showLbtChannelDialog().onCancel()");
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				adapter.setTable(mFreqTable);
				adapter.notifyDataSetChanged();
				ATLog.i(TAG, "INFO. showLbtChannelDialog().onShow()");
			}
		});
		dialog.show();

		ATLog.i(TAG, "INFO. showLbtChannelDialog()");
	}

	private class FreqSlotItem {
		private int mSlot;
		private String mFreqName;

		public FreqSlotItem(int slot, String name) {
			mSlot = slot;
			mFreqName = name;
		}

		public int getSlot() {
			return mSlot;
		}

		public String getName() {
			return mFreqName;
		}

		@Override
		public String toString() {
			return String.format(Locale.US, "%d, [%s]", mSlot, mFreqName);
		}
	}
}
