package com.hanmiit.app.rfid.blasterdemo.view.base;

import java.util.Locale;

import com.hanmiit.lib.diagnostics.ATLog;
import com.hanmiit.lib.rfid.exception.ATRfidReaderException;
import com.hanmiit.lib.rfid.params.MinMaxValue;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.adapter.PowerRangeAdapter;
import com.hanmiit.app.rfid.blasterdemo.util.ResUtil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public abstract class RfidPowerGainActivity extends RfidClearActivity {

	private static final String TAG = RfidPowerGainActivity.class.getSimpleName();

	private static final int DEFAULT_POWER_GAIN = 300;

	private TextView txtPower;
	private TextView txtOperationTime;

	private int mPowerGain;
	private int mOperationTime;
	private MinMaxValue mPowerRange;

	protected boolean mIsEnabled;

	@Override
	protected void createReader() {
		super.createReader();
		mPowerGain = DEFAULT_POWER_GAIN;
		mPowerRange = getReader().getPowerGainRange();
		mIsEnabled = false;

		ATLog.i(TAG, "INFO. createReader()");
	}

	// Initialize Reader
	@Override
	protected void initReader() {
		int power = 0;
		int time = 0;

		// Get Power Gain
		try {
			power = getReader().getPowerGain();
		} catch (ATRfidReaderException e) {
			power = mPowerRange.getMax();
		}
		setPowerGain(power);
		// Get Operation Time
		try {
			time = getReader().getOperationTime();
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. initReader() - Failed to get operation time");
			time = 0;
		}
		setOperationTime(time);

		ATLog.i(TAG, "INFO. initReader()");
	}

	@Override
	public void onClick(View v) {
		ATLog.i(TAG, "INFO. onClick(%s)", ResUtil.getId(v.getId()));

		switch (v.getId()) {
		case R.id.power_gain:
			showPowerGainDialog();
			break;
		case R.id.operation_time:
			showOperationTimeDialog();
			break;
		}
		super.onClick(v);
	}

	protected void initWidgets() {
		super.initWidgets();

		// Power Gain Spinner
		txtPower = (TextView) findViewById(R.id.power_gain);
		txtPower.setOnClickListener(this);
		// Operation Time EditText
		txtOperationTime = (TextView) findViewById(R.id.operation_time);
		txtOperationTime.setOnClickListener(this);
		setOperationTime(mOperationTime);
		
		ATLog.i(TAG, "INFO. initWidgets()");
	}

	protected void enableActionWidgets(boolean enabled) {
		super.enableActionWidgets(enabled);

		mIsEnabled = isEnabledWidget(enabled);

		ATLog.i(TAG, "INFO. enableActionWidgets(%s)", enabled);
	}

	protected void setPowerGain(int power) {
		mPowerGain = power;
		txtPower.setText(String.format(Locale.US, "%.1f dBm", (double) power / 10.0));

		try {
			getReader().setPowerGain(mPowerGain);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, "ERROR. setPowerGain(%d) - Failed to set power gain", mPowerGain);
			return;
		}
		ATLog.i(TAG, "INFO. setPowerGain(%d)", power);
	}

	protected int getPowerGain() {
		ATLog.i(TAG, "INFO. getPowerGain() - [%d]", mPowerGain);
		return mPowerGain;
	}

	protected void setOperationTime(int time) {
		mOperationTime = time;
		txtOperationTime.setText(String.format(Locale.US, "%d ms", mOperationTime));
		ATLog.i(TAG, "INFO. setOperationTime(%d)", time);
	}

	protected int getOperationTime() {
		ATLog.i(TAG, "INFO. getOperationTime() - [%d]", mOperationTime);
		return mOperationTime;
	}

	private void showPowerGainDialog() {

		if (!mIsEnabled)
			return;

		LinearLayout root = (LinearLayout) LinearLayout.inflate(this, R.layout.dialog_list_view, null);
		final ListView power = (ListView) root.findViewById(R.id.list);
		final PowerRangeAdapter adapter = new PowerRangeAdapter(this, mPowerRange,
				android.R.layout.simple_list_item_single_choice);
		power.setAdapter(adapter);
		power.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.power_gain);
		builder.setView(root);
		builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				int position = power.getCheckedItemPosition();
				int power = adapter.getItemValue(position);
				setPowerGain(power);
				ATLog.i(TAG, "INFO. showPowerGinaDialog().$PositiveButton.onClick()");
			}
		});
		builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ATLog.i(TAG, "INFO. showPowerGinaDialog().$NegativeButton.onClick()");
			}
		});
		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				ATLog.i(TAG, "INFO. showPowerGinaDialog().onCancel()");
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				int position = adapter.getItemPosition(mPowerGain);
				power.setItemChecked(position, true);
				power.setSelectionFromTop(position, 0);
				ATLog.i(TAG, "INFO. showPowerGinaDialog().onShow()");
			}
		});
		dialog.show();

		ATLog.i(TAG, "INFO. showPowerGinaDialog()");
	}

	private void showOperationTimeDialog() {

		if (!mIsEnabled)
			return;

		LinearLayout root = (LinearLayout) LinearLayout.inflate(this, R.layout.dialog_input_unit, null);
		final EditText operationTime = (EditText) root.findViewById(R.id.value);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.operation_time);
		builder.setView(root);
		builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				int time = 0;
				try {
					time = Integer.parseInt(operationTime.getText().toString());
				} catch (Exception e) {
					time = 0;
				}
				try {
					getReader().setOperationTime(time);
				} catch (ATRfidReaderException e) {
					ATLog.e(TAG, e, "ERROR. showOperationTimeDialog() - Failed to set operation time(%d)", time);
				}
				setOperationTime(time);
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(operationTime.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showOperationTimeDialog().$PositiveButton.onClick()");
			}
		});
		builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(operationTime.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showOperationTimeDialog().$NegativeButton.onClick()");
			}
		});
		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(operationTime.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showOperationTimeDialog().onCancel()");
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				operationTime.setText(String.format(Locale.US, "%d", mOperationTime));
				operationTime.selectAll();
				operationTime.requestFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(operationTime, InputMethodManager.SHOW_FORCED);
				ATLog.i(TAG, "INFO. showOperationTimeDialog().onShow()");
			}
		});
		dialog.show();

		ATLog.i(TAG, "INFO. showOperationTimeDialog()");
	}
}
