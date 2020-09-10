package com.hanmiit.app.rfid.blasterdemo.view;

import com.hanmiit.lib.rfid.exception.ATRfidReaderException;
import com.hanmiit.lib.rfid.type.ActionState;
import com.hanmiit.lib.rfid.type.MemoryBank;
import com.hanmiit.lib.rfid.type.ResultCode;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.type.MaskType;
import com.hanmiit.app.rfid.blasterdemo.util.ResUtil;
import com.hanmiit.app.rfid.blasterdemo.view.base.MemoryActivity;
import com.hanmiit.lib.ATRfidReader;
import com.hanmiit.lib.diagnostics.ATLog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WriteMemoryActivity extends MemoryActivity {

	private static final String TAG = WriteMemoryActivity.class.getSimpleName();

	private TextView txtValue;
	
	private Button btnWrite;
	private Button btnBlockWrite;

	private String mValue;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_write_memory);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		ATLog.i(TAG, "INFO. onCreate()");

		// Create Reader
		createReader();

		// Initialize Widget
		initWidgets();

		// Initialize Reader
		initReader();

		// Initialize Mask
		initMask();

		resetActionResult();

		setMask(getMask());
		mMaskType = MaskType.SelectionMask;

		// Enable action widget
		enableActionWidgets(true);

		outputMessage(R.string.write_memory_intro_message);
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
		
		ATLog.i(TAG, "INFO. onStart()");
	}

	@Override
	protected void onStop() {

		// Rollback Mask
		exitMask();

		ATLog.i(TAG, "INFO. onStop()");
		super.onStop();
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		// Set Key Action
		try {
			getReader().setUseKeyAction(getKeyAction());
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. onStart() - Failed to set use key action");
		}
		
		ATLog.i(TAG, "INFO. onResume()");
	}

	@Override
	protected void onPause() {
		// Set Key Action
		try {
			getReader().setUseKeyAction(false);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. onStart() - Failed to set use key action");
		}

		ATLog.i(TAG, "INFO. onPause()");
		super.onPause();
	}
	
	@Override
	public void onClick(View v) {

		ATLog.i(TAG, "INFO. onClick(%s)", ResUtil.getId(v.getId()));
		
		switch (v.getId()) {
		case R.id.write_value:
			showWriteValueDialog();
			break;
		case R.id.action_write:
			if (getReader().getAction() == ActionState.Stop) {
				writeMemory();
			} else {
				stopAction();
			}
			break;
		case R.id.action_block_write:
			if (getReader().getAction() == ActionState.Stop) {
				blockWrite();
			} else {
				stopAction();
			}
			break;
		}
		super.onClick(v);
	}

	@Override
	public void onActionChanged(ATRfidReader reader, ActionState action) {
		ATLog.d(TAG, "EVENT. onActionChanged(%s)", action);

		enableActionWidgets(true);

		if (action == ActionState.Stop) {
			if (!isActionResult()) {
				outputMessage(R.string.write_memory_intro_message);
			}
		}
	}

	@Override
	public void onAccessResult(ATRfidReader reader, ResultCode code, ActionState action, String epc, String data,
			float rssi, float phase) {
		ATLog.i(TAG, "EVENT. onAccessResult(%s, %s, [%s], [%s], %.2f, %.2f)", code, action, epc == null ? "" : epc,
				data == null ? "" : data, rssi, phase);

		setActionResult();

		if (code != ResultCode.NoError) {
			outputFailMessage(code.toString());
			return;
		}

		outputSelection(epc.length() > 4 ? epc.substring(4) : epc);
		outputSuccessMessage(R.string.access_success);
		setRssi(rssi, phase);
	}

	// Write Memory
	private void writeMemory() {
		clearWidgets();
		enableActionWidgets(false);
		outputSelection("");
		outputMessage(R.string.read_memory_read_message);

		resetActionResult();

		MemoryBank bank = getMemBank();
		int offset = getMemOffset();
		String data = getWriteValue();
		int time = getOperationTime();

		String password = getAccessPassword();
		try {
			getReader().setAccessPassword(password);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, "ERROR. writeMemory() - Failed to set password [%s]", password);
		}

		try {
			getReader().setOperationTime(time);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, "ERROR. writeMemory() - Failed to set operation time(%d)", time);
		}

		if (getReader().writeMemory(bank, offset, data) != ResultCode.NoError) {
			ATLog.e(TAG, "ERROR. writeMemory() - Failed to start write memory(%s, %d, [%s])", bank, offset, data);
			enableActionWidgets(true);
			outputFailMessage(R.string.write_memory_fail_write_message);
			return;
		}
		ATLog.i(TAG, "INFO. writeMemory()");
	}

	// Block Write
	private void blockWrite() {
		clearWidgets();
		enableActionWidgets(false);
		outputSelection("");
		outputMessage(R.string.read_memory_block_erase_message);

		resetActionResult();

		MemoryBank bank = getMemBank();
		int offset = getMemOffset();
		String data = getWriteValue();
		int time = getOperationTime();

		String password = getAccessPassword();
		try {
			getReader().setAccessPassword(password);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. blockWrite() - Failed to set password [%s]", password);
		}

		try {
			getReader().setOperationTime(time);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. blockWrite() - Failed to set operation time(%d)", time);
		}

		if (getReader().blockWrite(bank, offset, data) != ResultCode.NoError) {
			ATLog.e(TAG, "ERROR. blockWrite() - Failed to start block write(%s, %d, [%s])", bank, offset, data);
			enableActionWidgets(true);
			outputFailMessage(R.string.write_memory_fail_block_write_message);
			return;
		}
		ATLog.i(TAG, "INFO. blockWrite()");
	}

	// Initialize Reader
	@Override
	protected void initReader() {
		super.initReader();
		ATLog.i(TAG, "INFO. initReader()");
	}

	// Initialize Widgets
	@Override
	protected void initWidgets() {
		super.initWidgets();

		// Value EditText
		txtValue = (TextView) findViewById(R.id.write_value);
		txtValue.setOnClickListener(this);
		setWriteValue("");
		// Write Button
		btnWrite = (Button) findViewById(R.id.action_write);
		btnWrite.setOnClickListener(this);
		// Block Write Button
		btnBlockWrite = (Button) findViewById(R.id.action_block_write);
		btnBlockWrite.setOnClickListener(this);

		ATLog.i(TAG, "INFO. initWidgets()");
	}

	// Enable/Disable Action Widgets
	@Override
	protected void enableActionWidgets(boolean enabled) {
		super.enableActionWidgets(enabled);

		btnWrite.setEnabled(isEnabledWidget(enabled, ActionState.WriteMemory));
		btnBlockWrite.setEnabled(isEnabledWidget(enabled, ActionState.BlockWrite) && false);
		if (enabled) {
			switch (getReader().getAction()) {
			case WriteMemory:
				btnWrite.setText(R.string.action_stop);
				btnBlockWrite.setText(R.string.action_block_write);
				break;
			case BlockWrite:
				btnWrite.setText(R.string.action_write_memory);
				btnBlockWrite.setText(R.string.action_stop);
				break;
			case Stop:
				btnWrite.setText(R.string.action_write_memory);
				btnBlockWrite.setText(R.string.action_block_write);
				break;
			default:
				break;
			}
		}
		
		ATLog.i(TAG, "INFO. enableActionWidgets(%s)", enabled);
	}
	
	protected void setWriteValue(String value) {
		mValue = value;
		txtValue.setText(mValue);
		ATLog.i(TAG, "INFO. setWriteValue([%s])", value);
	}
	
	protected String getWriteValue() {
		ATLog.i(TAG, "INFO. getWriteValue() - [%s]", mValue);
		return mValue;
	}
	
	private void showWriteValueDialog() {
		if (!mIsEnabled)
			return;
		
		LinearLayout root = (LinearLayout)LinearLayout.inflate(this, R.layout.dialog_input_hex, null);
		final EditText writeValue = (EditText)root.findViewById(R.id.value);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.write_data);
		builder.setView(root);
		builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				setWriteValue(writeValue.getText().toString());
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(writeValue.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showWriteValueDialog().$PositiveButton.onClick()");
			}
		});
		builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(writeValue.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showWriteValueDialog().$NegativeButton.onClick()");
			}
		});
		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(writeValue.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showWriteValueDialog().onCancel()");
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				writeValue.setText(mValue);
				writeValue.selectAll();
				writeValue.requestFocus();
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(writeValue, InputMethodManager.SHOW_FORCED);
				ATLog.i(TAG, "INFO. showWriteValueDialog().onShow()");
			}
		});
		dialog.show();
		
		ATLog.i(TAG, "INFO. showWriteValueDialog()");
	}
}
