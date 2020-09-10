package com.hanmiit.app.rfid.blasterdemo.view.base;

import com.hanmiit.lib.barcode.type.BarcodeType;
import com.hanmiit.lib.ATRfidManager;
import com.hanmiit.lib.ATRfidReader;
import com.hanmiit.lib.device.type.ConnectionState;
import com.hanmiit.lib.diagnostics.ATLog;
import com.hanmiit.lib.rfid.event.ATRfidEventListener;
import com.hanmiit.lib.rfid.type.ActionState;
import com.hanmiit.lib.rfid.type.CommandType;
import com.hanmiit.lib.rfid.type.RemoteKeyState;
import com.hanmiit.lib.rfid.type.ResultCode;
import com.hanmiit.app.rfid.blasterdemo.util.ResUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public abstract class RfidActivity extends SubActivity implements ATRfidEventListener {

	private static final String TAG = RfidActivity.class.getSimpleName();
	
	protected static final int VIEW_MASK = 1000;
	protected static final int RESULT_DISCONNECTED = Activity.RESULT_FIRST_USER + 1;

	public static final String KEY_ACTION = "key_action";
	
	public static final boolean DEFAULT_KEY_ACTION = true;

	private ATRfidReader mReader = null;
	private boolean mKeyAction;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		mKeyAction = getIntent().getBooleanExtra(KEY_ACTION, DEFAULT_KEY_ACTION);
	}

	// Retreive RFID Reader
	protected ATRfidReader getReader() {
		return mReader;
	}

	// Create Reader
	protected void createReader() {
		mReader = ATRfidManager.getInstance();
		mReader.setEventListener(this);

		ATLog.i(TAG, "INFO. createReader()");
	}

	// Destroy Reader
	protected void destroyReader() {
		mReader.removeEventListener(this);
		// Destroy Rfid Reader
		ATRfidManager.onDestroy();

		ATLog.i(TAG, "INFO. destroyReader()");
	}

	// Initialize Reader
	protected abstract void initReader();

	protected boolean isReaderAction() {
		return mReader.getAction() != ActionState.Stop;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		ATLog.d(TAG, "DEBUG. onActivityResult(%s, %s)", getRequestCode(requestCode), getResultCode(resultCode));

		if (requestCode == VIEW_MASK && resultCode == RESULT_DISCONNECTED) {
			setResult(RESULT_DISCONNECTED);
			finish();
			return;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onStateChanged(ATRfidReader reader, ConnectionState state) {
		ATLog.d(TAG, "EVENT. onStateChanged(%s)", state);

		if (state == ConnectionState.Disconnected) {
			setResult(RESULT_DISCONNECTED);
			finish();
		}
	}

	@Override
	public void onActionChanged(ATRfidReader reader, ActionState action) {
		ATLog.d(TAG, "EVENT. onActionChanged(%s)", action);
	}

	@Override
	public void onCommandComplete(ATRfidReader reader, CommandType command) {
		ATLog.d(TAG, "EVENT. onCommandComplete(%s)", command);
	}

	@Override
	public void onLoadTag(ATRfidReader reader, String tag) {
		ATLog.d(TAG, "EVENT. onLoadTag([%s])", tag);
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
	public void onDebugMessage(ATRfidReader reader, String msg) {
		ATLog.i(TAG, "EVENT. onDebugMessage (%s)", msg);
	}

	@Override
	public void onDetactBarcode(ATRfidReader reader, BarcodeType type, String codeId, String barcode) {
		ATLog.i(TAG, "EVENT. onDetactBarcode (%s, %s, [%s])", type, codeId, barcode);
	}

	@Override
	public void onRemoteKeyStateChanged(ATRfidReader reader, RemoteKeyState state) {
		ATLog.i(TAG, "EVENT. onRemoteKeyStateChanged(%s)", state);

	}

	protected void enableActionButton(Button action, boolean enabled, int actionId, int stopId) {
		action.setEnabled(enabled);
		if (enabled) {
			action.setText(getReader().getAction() == ActionState.Stop ? actionId : stopId);
		}
		ATLog.i(TAG, "INFO. enableActionButton(%s, %s, %d, %d)", ResUtil.getId(action.getId()), enabled, actionId,
				stopId);
	}

	protected boolean isEnabledWidget(boolean enabled) {
		ActionState action = getReader().getAction();
		return enabled ? (action == ActionState.Stop ? enabled : false) : enabled;
	}

	protected boolean isEnabledWidget(boolean enabled, ActionState state) {
		ActionState action = getReader().getAction();
		return enabled ? (action == ActionState.Stop ? enabled : (action == state ? enabled : false)) : enabled;
	}

	protected String getResultCode(int resultCode) {

		switch (resultCode) {
		case RESULT_DISCONNECTED:
			return "RESULT_DISCONNECTED";
		}
		return super.getResultCode(resultCode);
	}
	
	protected boolean getKeyAction() {
		return mKeyAction;
	}
	
	protected void setKeyAction(boolean enabled) {
		mKeyAction = enabled;
	}
}
