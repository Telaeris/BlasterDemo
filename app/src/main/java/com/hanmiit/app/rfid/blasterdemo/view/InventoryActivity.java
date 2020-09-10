package com.hanmiit.app.rfid.blasterdemo.view;

import java.util.Locale;

import com.hanmiit.lib.rfid.exception.ATRfidReaderException;
import com.hanmiit.lib.rfid.type.ActionState;
import com.hanmiit.lib.rfid.type.MemoryBank;
import com.hanmiit.lib.rfid.type.OperationMode;
import com.hanmiit.lib.rfid.type.ResultCode;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.filter.InputFilterMinMax;
import com.hanmiit.app.rfid.blasterdemo.util.ResUtil;
import com.hanmiit.app.rfid.blasterdemo.view.base.BaseInventoryActivity;
import com.hanmiit.lib.ATRfidManager;
import com.hanmiit.lib.ATRfidReader;
import com.hanmiit.lib.diagnostics.ATLog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class InventoryActivity extends BaseInventoryActivity implements OnCheckedChangeListener {

	private static final String TAG = InventoryActivity.class.getSimpleName();

	private static final int DEFAULT_TID_LENGTH = 2;
	
	private Switch swtContinuousMode;
	private Switch swtReportRSSI;
	private Switch swtReportTID;
	private TextView txtTotalCount;
	private TextView txtSpeed;
	private TextView txtRestartTime;
	private TextView txtTidLength;

	private long mTotalTagCount;
	private long mLastTime;
	private double mTagSpeed;

	//2016-06-01 mkj90 ReInventoryTime 
	private long mSpeedTagCount;  //2016-06-01 TotalTagCount�� Speed�� ���� �̹Ƿ� ���� ����.

	private int mRestartTime;
	private int mTidLength;
	private boolean mIsRunRestart;
	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inventory);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		ATLog.i(TAG, "INFO. onCreate()");
		
		mHandler = new Handler();
		
		// Create Reader
		createReader();

		// Initialize Mask
		initMask();

		// Initialize Widget
		initWidgets();

		// Initialize Reader
		initReader();

		mTotalTagCount = 0;
		mLastTime = 0;
		mTagSpeed = 0.0;
		mRestartTime = 0;
		mIsRunRestart = false;
		mTidLength = 2;
		
		// Enable action widget
		enableActionWidgets(true);
		
		//2016-06-01 mkj90 ReInventoryHandler
		mSpeedTagCount =0;
	}

	@Override
	protected void onDestroy() {

		ATLog.i(TAG, "INFO. onDestroy()");

		// Destroy Reader
		destroyReader();

		adpTags.shutDown();

		super.onDestroy();
	}

	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		super.onCheckedChanged(buttonView, isChecked);
		
		ATLog.i(TAG, "INFO. onCheckedChanged(%s, %s)", ResUtil.getId(buttonView.getId()), isChecked);

		switch (buttonView.getId()) {
		case R.id.continuous_mode:
			try {
				getReader().setContinuousMode(isChecked);
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. onCheckedChanged(%s, %s) - Failed to set continuous mode",
						ResUtil.getId(buttonView.getId()), isChecked);
			}
			break;
		case R.id.report_rssi:
			try {
				getReader().setReportRSSI(isChecked);
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. onCheckedChanged(%s, %s) - Failed to set report RSSI",
						ResUtil.getId(buttonView.getId()), isChecked);
			}
			clearWidgets();			
			adpTags.setReportRSSI(isChecked);
			break;
		case R.id.report_tid:
			try {
				getReader().setOperationMode(isChecked ? OperationMode.TID : OperationMode.Normal);
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. onCheckedChanged(%s, %s) - Failed to set report TID",
						ResUtil.getId(buttonView.getId()), isChecked);
			}
			clearWidgets();
			adpTags.setReportTID(isChecked);
			break;
		}
	}

	@Override
	public void onClick(View v) {
		ATLog.i(TAG, "INFO. onClick(%s)", ResUtil.getId(v.getId()));
		switch (v.getId()) {
		case R.id.restart_time:
			showRestartTimeDialog();
			break;
		case R.id.tid_length:
			showTidLengthDialog();
			break;
		}
		super.onClick(v);
	}

	@Override
	public void onReadedTag(ATRfidReader reader, ActionState action, String tag, float rssi, float phase) {
		long time = System.currentTimeMillis();
		double interval = 0.0;

		ATLog.i(TAG, "EVENT. onReadedTag(%s, [%s], %.2f, %.2f)", action, tag, rssi, phase);

		mTotalTagCount++;
		mSpeedTagCount++;  //2016-06-01 mkj90 TagSpeed �߰�
		if (mLastTime == 0) {
			mTagSpeed = 0.0;
			mLastTime = time;
		} else {
			interval = (double) (time - mLastTime) / 1000.0;
			mTagSpeed = (double) mSpeedTagCount / interval;
		}
		ATLog.d(TAG, "@@@ DEBUG. Tag Speed [%.2f, %d, %.3f, %d, %d]", mTagSpeed, mTotalTagCount, interval, time,
				mLastTime);

		super.onReadedTag(reader, action, tag, rssi, phase);

		txtTotalCount.setText("" + mTotalTagCount);
		txtSpeed.setText(String.format(Locale.US, "%.2f tps", mTagSpeed));
	}

	@Override
	public void onAccessResult(ATRfidReader reader, ResultCode code, ActionState action, String epc, String data,
			float rssi, float phase) {

		long time = System.currentTimeMillis();
		double interval = 0.0;

		ATLog.i(TAG, "EVENT. onAccessResult(%s, [%s], [%s], %.2f, %.2f)", action, epc, data, rssi, phase);


		mTotalTagCount++;
		mSpeedTagCount++;
		if (mLastTime == 0) {
			mTagSpeed = 0.0;
			mLastTime = time;
		} else {
			interval = (double) (time - mLastTime) / 1000.0;
			mTagSpeed = (double) mSpeedTagCount / interval;
		}
		ATLog.d(TAG, "@@@ DEBUG. Tag Speed [%.2f, %d, %.3f, %d, %d]", mTagSpeed, mTotalTagCount, interval, time,
				mLastTime);

		super.onAccessResult(reader, code, action, epc, data, rssi, phase);

		txtTotalCount.setText("" + mTotalTagCount);
		txtSpeed.setText(String.format(Locale.US, "%.2f tps", mTagSpeed));
	}

	@Override
	public void onActionChanged(ATRfidReader reader, ActionState action) {
		
		if (mIsRunRestart) {
			adpTags.notifyDataSetChanged();
			if (action != ActionState.Stop) {
				enableActionWidgets(true);
			}
		} else {
			super.onActionChanged(reader, action);
		}
		
		if(action == ActionState.Stop){
			ATLog.e(TAG, "INFO. onActionChanged()   "+action);
			mSpeedTagCount = 0;
			mLastTime =0;
			
			if (mRestartTime > 0 && mIsRunRestart) {
				mHandler.postDelayed(mRestartHandler, mRestartTime);
			}
		}
	}

	// Initialize Reader
	@Override
	protected void initReader() {
		super.initReader();
		// Set Stored Mode Disable
		try {
			getReader().setStoredMode(false);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. initReader() - Failed to set stored mode disable");
		}
		// Set Rpoert Mode Disable
		try {
			getReader().setReportMode(false);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. initReader() - Failed to set report mode disable");
		}
		// Set Reader Continuous Mode
		try {
			getReader().setContinuousMode(true);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. initReader() - Failed to get continuous mode");
		}
		swtContinuousMode.setChecked(true);
		// Get Report RSSI
		boolean isReportRSSI = false;
		swtReportRSSI.setChecked(isReportRSSI);
		adpTags.setReportRSSI(isReportRSSI);
		// Get Report TID
		boolean isReportTID = false;
		swtReportTID.setChecked(isReportTID);
		adpTags.setReportTID(isReportTID);
		// TID Length
		int tidLength = DEFAULT_TID_LENGTH;
		try {
			tidLength = getReader().getActionTIDReadLength();
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. initReader() - Failed to get continuous mode");
		}
		setTidLength(tidLength);
		
		ATLog.e(TAG, "Version : " + ATRfidManager.getVersion());
		ATLog.i(TAG, "INFO. initReader()");
	}
	
	// Initialize Widgets
	@Override
	protected void initWidgets() {
		super.initWidgets();

		// Continuous Mode CheckBox
		swtContinuousMode = (Switch) findViewById(R.id.continuous_mode);
		swtContinuousMode.setOnCheckedChangeListener(this);
		// Report RSSI
		swtReportRSSI = (Switch) findViewById(R.id.report_rssi);
		swtReportRSSI.setOnCheckedChangeListener(this);
		// Report TID
		swtReportTID = (Switch) findViewById(R.id.report_tid);
		swtReportTID.setOnCheckedChangeListener(this);
		
		txtTidLength = (TextView)findViewById(R.id.tid_length);
		txtTidLength.setOnClickListener(this);
		setTidLength(mTidLength);
		
		// Total Count TextView
		txtTotalCount = (TextView) findViewById(R.id.total_tag_count);
		txtTotalCount.setText(String.format(Locale.US, "%d", mTotalTagCount));

		txtSpeed = (TextView) findViewById(R.id.speed);
		txtSpeed.setText("0 tps");
		// txtSpeed.setVisibility(View.GONE);
		
		txtRestartTime = (TextView)findViewById(R.id.restart_time);
		txtRestartTime.setOnClickListener(this);
		setRestartTime(mRestartTime);
		
		ATLog.i(TAG, "INFO. initWidgets()");
	}

	// Enable/Disable Action Widgets
	@Override
	protected void enableActionWidgets(boolean enabled) {
		super.enableActionWidgets(enabled);

		swtContinuousMode.setEnabled(isEnabledWidget(enabled));
		swtReportRSSI.setEnabled(isEnabledWidget(enabled));
		swtReportTID.setEnabled(isEnabledWidget(enabled));
		ATLog.i(TAG, "INFO. enableActionWidgets(%s)", enabled);
	}

	@Override
	protected void clearWidgets() {
		super.clearWidgets();
		
		mSpeedTagCount =0; // 2016-06-01 mkj90 SpeedTagCount �ʱ�ȭ.
		mTotalTagCount = 0;
		mLastTime = 0;
		mTagSpeed = 0.0;
		txtTotalCount.setText("" + mTotalTagCount);
		txtSpeed.setText(String.format(Locale.US, "%.2f tps", mTagSpeed));
		
		ATLog.i(TAG, "INFO. clearWidgets()");
	}
	
	private void setRestartTime(int time) {
		mRestartTime = time;
		txtRestartTime.setText(String.format(Locale.US, "%d ms", mRestartTime));
		ATLog.i(TAG, "INFO. setOperationTime(%d)", time);
	}

	private void showRestartTimeDialog() {

		if (!mIsEnabled)
			return;

		LinearLayout root = (LinearLayout) LinearLayout.inflate(this, R.layout.dialog_input_unit, null);
		final EditText restartTime = (EditText) root.findViewById(R.id.value);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.operation_time);
		builder.setView(root);
		builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				int time = 0;
				try {
					time = Integer.parseInt(restartTime.getText().toString());
				} catch (Exception e) {
					time = 0;
				}
				setRestartTime(time);
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(restartTime.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showOperationTimeDialog().$PositiveButton.onClick()");
			}
		});
		builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(restartTime.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showOperationTimeDialog().$NegativeButton.onClick()");
			}
		});
		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(restartTime.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showOperationTimeDialog().onCancel()");
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				restartTime.setText(String.format(Locale.US, "%d", mRestartTime));
				restartTime.selectAll();
				restartTime.requestFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(restartTime, InputMethodManager.SHOW_FORCED);
				ATLog.i(TAG, "INFO. showOperationTimeDialog().onShow()");
			}
		});
		dialog.show();

		ATLog.i(TAG, "INFO. showRestartTimeDialog()");
	}

	private void setTidLength(int length) {
		mTidLength = length;
		txtTidLength.setText(String.format(Locale.US, "%d word", mTidLength));
		ATLog.i(TAG, "INFO. setTidLength(%d)", length);
	}
	
	private void showTidLengthDialog() {
		if (!mIsEnabled)
			return;
		if (!adpTags.getReportTID())
			return;

		LinearLayout root = (LinearLayout) LinearLayout.inflate(this, R.layout.dialog_input_word, null);
		final EditText length = (EditText) root.findViewById(R.id.value);
		length.setFilters(new InputFilter[] { new InputFilterMinMax(1, Short.MAX_VALUE) });
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.tid_length);
		builder.setView(root);
		builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int value = 0;
				try {
					value = Integer.parseInt(length.getText().toString());
				} catch (Exception e) {
					value = DEFAULT_TID_LENGTH;
				}
				setTidLength(value);
				try {
					getReader().setActionTIDReadLength(mTidLength);
				} catch (ATRfidReaderException e) {
					ATLog.e(TAG, e, "ERROR. showTidLengthDialog().$NegativeButton.onClick() - Failed to set action tid read length [%d]", mTidLength);
				}
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(length.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showTidLengthDialog().$PositiveButton.onClick()");
			}
		});
		builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(length.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showTidLengthDialog().$NegativeButton.onClick()");
			}
		});
		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(length.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showTidLengthDialog().onCancel()");
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				length.setText(String.format(Locale.US, "%d", mTidLength));
				length.selectAll();
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(length, InputMethodManager.SHOW_FORCED);
				ATLog.i(TAG, "INFO. showTidLengthDialog().onShow()");
			}
		});
		dialog.show();
		
		ATLog.i(TAG, "INFO. showTidLengthDialog()");
	}
	
	@Override
	protected void startInventory() {
		mIsRunRestart = mRestartTime > 0;
		
		if (adpTags.getReportTID()) {
			startTidInventory();
		} else {
			super.startInventory();
		}
	}

	@Override
	protected void stopInventory() {
		mIsRunRestart = false;
		super.stopInventory();
	}

	private void startTidInventory() {
		enableActionWidgets(false);
		int time = getOperationTime();
		int count = 2;
		try {
			getReader().setOperationTime(time);
			count = getReader().getActionTIDReadLength();
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. startTidInventory() - Failed to set operation time(%d)", time);
		}
		if (getReader().readMemory(MemoryBank.TID, 0, count) != ResultCode.NoError) {
			ATLog.e(TAG, "ERROR. startTidInventory() - Failed to start TID inventory()");
			enableActionWidgets(true);
			return;
		}
		ATLog.i(TAG, "INFO. startTidInventory()");
	}
	
	private Runnable mRestartHandler = new Runnable() {

		@Override
		public void run() {
			if (getReader().inventory() != ResultCode.NoError) {
				ATLog.e(TAG, "ERROR. $mRestartHandler.run() - Failed to start inventory()");
				enableActionWidgets(true);
				return;
			}
			ATLog.i(TAG, "INFO. $mRestartHandler.run()");
		}
	};
}
