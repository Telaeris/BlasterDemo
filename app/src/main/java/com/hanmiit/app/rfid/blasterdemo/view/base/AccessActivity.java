package com.hanmiit.app.rfid.blasterdemo.view.base;

import java.util.Locale;

import com.hanmiit.lib.diagnostics.ATLog;
import com.hanmiit.lib.rfid.exception.ATRfidReaderException;
import com.hanmiit.lib.rfid.type.ActionState;
import com.hanmiit.lib.rfid.type.ResultCode;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.util.ResUtil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public abstract class AccessActivity extends RfidMaskActivity implements OnCheckedChangeListener {

	private static final String TAG = AccessActivity.class.getSimpleName();

	public static final int VIEW_READ_MEMORY = 1003;
	public static final int VIEW_WRITE_MEMORY = 1004;
	public static final int VIEW_TAG_ACCESS = 1005;

	private TextView txtSelection;
	private ProgressBar prgWait;
	private TextView txtMessage;
	private LinearLayout layoutMessage;
	private TextView txtPassword;

	private LinearLayout layoutRssi;
	private View splitRssi;
	private TextView txtRssi;
	private TextView txtPhase;

	private CheckBox chkRssi;

	private boolean mLastContinuousMode;
	private String mMask;
	private String mPassword;

	private boolean mIsActionResult;

	public AccessActivity() {
		mMask = "";
		mIsActionResult = false;
	}

	@Override
	protected void initReader() {
		super.initReader();

		// Continuous Mode Backup
		try {
			mLastContinuousMode = getReader().getContinuousMode();
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. initReader() - Failed to get continuous mode");
		}
		try {
			getReader().setContinuousMode(false);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. initReader() - Failed to set continuous mode");
		}

		// Get RSSI Mode
		try {
			boolean isChecked = getReader().getReportRSSI();
			splitRssi.setVisibility(View.VISIBLE);
			chkRssi.setVisibility(View.VISIBLE);
			isChecked = false;
			getReader().setReportRSSI(isChecked);
			chkRssi.setChecked(isChecked);
			layoutRssi.setVisibility(isChecked ? View.VISIBLE : View.GONE);
		} catch (Exception e) {
			ATLog.e(TAG, e, "ERROR. initReader() - Faield to get rssi report mode");
			chkRssi.setChecked(false);
			splitRssi.setVisibility(View.GONE);
			chkRssi.setVisibility(View.GONE);
			layoutRssi.setVisibility(View.GONE);
		}

		ATLog.i(TAG, "INFO. initReader()");
	}

	@Override
	protected void destroyReader() {

		ATLog.i(TAG, "INFO. destroyReader()");

		try {
			getReader().setContinuousMode(mLastContinuousMode);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. destroyReader() - Failed to set continuous mode backup");
		}

		super.destroyReader();
	}

	@Override
	public void onClick(View v) {
		ATLog.i(TAG, "INFO. onClick(%s)", ResUtil.getId(v.getId()));

		switch (v.getId()) {
		case R.id.password:
			showAccessPasswordDialog();
			break;
		}
		super.onClick(v);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

		ATLog.i(TAG, "INFO. onCheckedChanged(%s, %s)", ResUtil.getId(buttonView.getId()), isChecked);

		switch (buttonView.getId()) {
		case R.id.report_rssi:
			try {
				getReader().setReportRSSI(isChecked);
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. onCheckedChanged(%s, %s) - Failed to set report rssi mode",
						ResUtil.getId(buttonView.getId()), isChecked);
			}
			layoutRssi.setVisibility(isChecked ? View.VISIBLE : View.GONE);
			break;
		}
	}

	// Stop Action
	protected void stopAction() {
		ResultCode res = ResultCode.NoError;
		enableActionWidgets(false);
		outputMessage(R.string.stop_action_message);

		if ((res = getReader().stop()) != ResultCode.NoError) {
			ATLog.e(TAG, "ERROR. stopAction() - Failed to stop operation [%s]", res);
			enableActionWidgets(true);
			outputFailMessage(R.string.stop_action_fail_message);
			return;
		}

		ATLog.i(TAG, "INFO. stopAction()");
	}

	@Override
	protected void initWidgets() {
		super.initWidgets();

		// Selection TextView
		txtSelection = (TextView) findViewById(R.id.selection);
		// Wait ProgressBar
		prgWait = (ProgressBar) findViewById(R.id.progress_bar);
		// Message TextView
		txtMessage = (TextView) findViewById(R.id.message);
		// Message Background
		layoutMessage = (LinearLayout) findViewById(R.id.background);

		// RSSI Layout
		layoutRssi = (LinearLayout) findViewById(R.id.rssi_background);
		txtRssi = (TextView) findViewById(R.id.rssi);
		txtRssi.setText("0.0 dB");
		txtPhase = (TextView) findViewById(R.id.phase);
		txtPhase.setText("0.0˚");

		// Password EditText
		txtPassword = (TextView) findViewById(R.id.password);
		txtPassword.setOnClickListener(this);
		setAccessPassword("");

		// Display RSSI
		splitRssi = findViewById(R.id.rssi_split);
		chkRssi = (CheckBox) findViewById(R.id.report_rssi);
		chkRssi.setOnCheckedChangeListener(this);

		ATLog.i(TAG, "INFO. initWidgets()");
	}

	@Override
	protected void enableActionWidgets(boolean enabled) {
		super.enableActionWidgets(enabled);
		prgWait.setVisibility(getReader().getAction() == ActionState.Stop ? View.GONE : View.VISIBLE);
		chkRssi.setEnabled(isEnabledWidget(enabled));

		ATLog.i(TAG, "INFO. enableActionWidgets(%s)", enabled);
	}

	@Override
	protected void clearWidgets() {
		outputSelection("");
		outputMessage("");
		txtRssi.setText("0.0 dB");
		txtPhase.setText("0.0˚");

		ATLog.i(TAG, "INFO. clearWidgets()");
	}

	protected void setMask(String mask) {
		mMask = mask;
		txtSelection.setText(mask);

		ATLog.i(TAG, "INFO. setMask([%s])", mask);
	}

	protected void outputSelection(String epc) {
		if (!mMask.equals(""))
			return;

		txtSelection.setText(epc);

		ATLog.i(TAG, "INFO. outputSelection([%s])", epc);
	}

	protected void outputMessage(int id) {
		outputMessage(getString(id));
	}

	protected void outputMessage(String msg) {
		txtMessage.setText(msg);
		txtMessage.setTextColor(getResources().getColor(R.color.black));
		layoutMessage.setBackgroundColor(getResources().getColor(R.color.message_background));

		ATLog.i(TAG, "INFO. outputMessage([%s])", msg);
	}

	protected void outputSuccessMessage(int id) {
		outputSuccessMessage(getString(id));
	}

	protected void outputSuccessMessage(String msg) {
		prgWait.setVisibility(View.GONE);
		txtMessage.setText(msg);
		txtMessage.setTextColor(getResources().getColor(R.color.white));
		layoutMessage.setBackgroundColor(getResources().getColor(R.color.blue));

		ATLog.i(TAG, "INFO. outputSuccessMessage([%s])", msg);
	}

	protected void outputFailMessage(int id) {
		outputFailMessage(getString(id));
	}

	protected void outputFailMessage(String msg) {
		prgWait.setVisibility(View.GONE);
		txtMessage.setText(msg);
		txtMessage.setTextColor(getResources().getColor(R.color.white));
		layoutMessage.setBackgroundColor(getResources().getColor(R.color.red));

		ATLog.i(TAG, "INFO. outputFailMessage([%s])", msg);
	}

	protected synchronized boolean isActionResult() {
		return mIsActionResult;
	}

	protected synchronized void setActionResult() {
		mIsActionResult = true;
	}

	protected synchronized void resetActionResult() {
		mIsActionResult = false;
	}

	protected void setAccessPassword(String password) {
		mPassword = password;
		txtPassword.setText(mPassword);
		ATLog.i(TAG, "INFO. setAccessPassword([%s])", password);
	}

	protected String getAccessPassword() {
		ATLog.i(TAG, "INFO. getAccessPassword() - [%s]", mPassword);
		return mPassword;
	}

	protected void setRssi(float rssi, float phase) {
		txtRssi.setText(String.format(Locale.US, "%.1f dB", rssi));
		txtPhase.setText(String.format(Locale.US, "%.1f˚", phase));

		ATLog.i(TAG, "INFO. setRssi(%.2f, %.2f)", rssi, phase);
	}

	protected String getRequestCode(int requestCode) {
		switch (requestCode) {
		case VIEW_READ_MEMORY:
			return "VIEW_READ_MEMORY";
		case VIEW_WRITE_MEMORY:
			return "VIEW_WRITE_MEMORY";
		case VIEW_TAG_ACCESS:
			return "VIEW_TAG_ACCESS";
		}
		return super.getRequestCode(requestCode);
	}

	private void showAccessPasswordDialog() {
		if (!mIsEnabled)
			return;
		
		LinearLayout root = (LinearLayout)LinearLayout.inflate(this, R.layout.dialog_password, null);
		final EditText password = (EditText)root.findViewById(R.id.password);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.password);
		builder.setView(root);
		builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				setAccessPassword(password.getText().toString());
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showAccessPasswordDialog().$PositiveButton.onClick()");
			}
		});
		builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showAccessPasswordDialog().$NegativeButton.onClick()");
			}
		});
		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showAccessPasswordDialog().onCancel()");
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				password.setText(mPassword);
				password.selectAll();
				password.requestFocus();
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(password, InputMethodManager.SHOW_FORCED);
				ATLog.i(TAG, "INFO. showAccessPasswordDialog().onShow()");
			}
		});
		dialog.show();
		
		ATLog.i(TAG, "INFO. showAccessPasswordDialog()");
	}
}
