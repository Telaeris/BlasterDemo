package com.hanmiit.app.rfid.blasterdemo.view;

import java.util.Locale;

import com.hanmiit.lib.rfid.exception.ATRfidReaderException;
import com.hanmiit.lib.rfid.type.ActionState;
import com.hanmiit.lib.rfid.type.MemoryBank;
import com.hanmiit.lib.rfid.type.ResultCode;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.adapter.MemoryListAdapter;
import com.hanmiit.app.rfid.blasterdemo.filter.InputFilterMinMax;
import com.hanmiit.app.rfid.blasterdemo.type.MaskType;
import com.hanmiit.app.rfid.blasterdemo.util.ResUtil;
import com.hanmiit.app.rfid.blasterdemo.view.base.MemoryActivity;
import com.hanmiit.lib.ATRfidReader;
import com.hanmiit.lib.diagnostics.ATLog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ReadMemoryActivity extends MemoryActivity {

	private static final String TAG = ReadMemoryActivity.class.getSimpleName();

	private static final int DEFAULT_MEM_LENGTH = 2;

	private ListView lstValue;
	private TextView txtLength;
	private Button btnRead;
	private Button btnErase;

	private MemoryListAdapter adpValue;

	private int mLength;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read_memory);
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
		mMaskType = MaskType.SelectionMask;

		resetActionResult();

		setMask(getMask());

		// Enable action widget
		enableActionWidgets(true);

		outputMessage(R.string.read_memory_intro_message);
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

		ATLog.i(TAG, "INFO. onStart()");
		super.onStop();
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		// Set Key Action
		try {
			getReader().setUseKeyAction(true);
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
		case R.id.length:
			showMemLengthDialog();
			break;
		case R.id.action_read:
			if (getReader().getAction() == ActionState.Stop) {
				readMemory();
			} else {
				stopAction();
			}
			break;
		case R.id.action_erase:
			if (getReader().getAction() == ActionState.Stop) {
				blockErase();
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
				outputMessage(R.string.read_memory_intro_message);
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
		int offset = getMemOffset();
		adpValue.setValue(offset, data);
		setRssi(rssi, phase);
	}

	// Read Memroy
	private void readMemory() {
		clearWidgets();
		enableActionWidgets(false);
		outputSelection("");
		outputMessage(R.string.read_memory_read_message);

		resetActionResult();

		MemoryBank bank = getMemBank();
		int offset = getMemOffset();
		int length = getMemLength();
		int time = getOperationTime();

		String password = getAccessPassword();
		try {
			getReader().setAccessPassword(password);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. readMemory() - Failed to set password [%s]", password);
		}

		try {
			getReader().setOperationTime(time);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. readMemory() - Failed to set operation time(%d)", time);
		}

		if (getReader().readMemory(bank, offset, length) != ResultCode.NoError) {
			ATLog.e(TAG, "ERROR. readMemory() - Failed to start read memory(%s, %d, %d)", bank, offset, length);
			enableActionWidgets(true);
			outputFailMessage(R.string.read_memory_fail_read_message);
			return;
		}
		ATLog.i(TAG, "INFO. readMemory()");
	}

	// Block Erase
	private void blockErase() {
		clearWidgets();
		enableActionWidgets(false);
		outputSelection("");
		outputMessage(R.string.read_memory_block_erase_message);

		resetActionResult();

		MemoryBank bank = getMemBank();
		int offset = getMemOffset();
		int length = getMemLength();
		int time = getOperationTime();

		String password = getAccessPassword();
		try {
			getReader().setAccessPassword(password);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. blockErase() - Failed to set password [%s]", password);
		}

		try {
			getReader().setOperationTime(time);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. blockErase() - Failed to set operation time(%d)", time);
		}

		if (getReader().blockErase(bank, offset, length) != ResultCode.NoError) {
			ATLog.e(TAG, "ERROR. blockErase() - Failed to start block erase(%s, %d, %d)", bank, offset, length);
			enableActionWidgets(true);
			outputFailMessage(R.string.read_memory_fail_block_erase_message);
			return;
		}
		ATLog.i(TAG, "INFO. blockErase()");
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

		// Value ListView
		lstValue = (ListView) findViewById(R.id.read_memory);
		adpValue = new MemoryListAdapter(this);
		lstValue.setAdapter(adpValue);
		// Length Spinner
		txtLength = (TextView) findViewById(R.id.length);
		txtLength.setOnClickListener(this);

		// Read Button
		btnRead = (Button) findViewById(R.id.action_read);
		btnRead.setOnClickListener(this);
		// Erase Button
		btnErase = (Button) findViewById(R.id.action_erase);
		btnErase.setOnClickListener(this);

		setMemLength(DEFAULT_MEM_LENGTH);

		ATLog.i(TAG, "INFO. initWidgets()");
	}

	// Enable/Disable Action Widgets
	@Override
	protected void enableActionWidgets(boolean enabled) {
		super.enableActionWidgets(enabled);

		lstValue.setEnabled(isEnabledWidget(enabled));
		// spnLength.setEnabled(isEnabledWidget(enabled));
		btnRead.setEnabled(isEnabledWidget(enabled, ActionState.ReadMemory));
		btnErase.setEnabled(isEnabledWidget(enabled, ActionState.BlockErase) && false);
		if (enabled) {
			switch (getReader().getAction()) {
			case ReadMemory:
				btnRead.setText(R.string.action_stop);
				btnErase.setText(R.string.action_block_erase);
				break;
			case BlockErase:
				btnRead.setText(R.string.action_read_memory);
				btnErase.setText(R.string.action_stop);
				break;
			case Stop:
				btnRead.setText(R.string.action_read_memory);
				btnErase.setText(R.string.action_block_erase);
				break;
			default:
				break;
			}
		}

		ATLog.i(TAG, "INFO. enableActionWidgets(%s)", enabled);
	}

	@Override
	protected void clearWidgets() {
		super.clearWidgets();

		resetActionResult();
		adpValue.clear();

		ATLog.i(TAG, "INFO. clearWidgets()");
	}

	protected void setMemLength(int length) {
		mLength = length;
		txtLength.setText(String.format(Locale.US, "%d WORD", mLength));
		ATLog.i(TAG, "INFO. setMemLength(%d)", length);
	}

	protected int getMemLength() {
		ATLog.i(TAG, "INFO. getMemLength() - [%d]", mLength);
		return mLength;
	}

	private void showMemLengthDialog() {
		if (!mIsEnabled)
			return;

		LinearLayout root = (LinearLayout) LinearLayout.inflate(this, R.layout.dialog_input_word, null);
		final EditText length = (EditText) root.findViewById(R.id.value);
		length.setFilters(new InputFilter[] { new InputFilterMinMax(1, Short.MAX_VALUE) });
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.length);
		builder.setView(root);
		builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int value = 0;
				try {
					value = Integer.parseInt(length.getText().toString());
				} catch (Exception e) {
					value = DEFAULT_MEM_LENGTH;
				}
				setMemLength(value);
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(length.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showMemLengthDialog().$PositiveButton.onClick()");
			}
		});
		builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(length.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showMemLengthDialog().$NegativeButton.onClick()");
			}
		});
		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(length.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showMemLengthDialog().onCancel()");
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				length.setText(String.format(Locale.US, "%d", mLength));
				length.selectAll();
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(length, InputMethodManager.SHOW_FORCED);
				ATLog.i(TAG, "INFO. showMemLengthDialog().onShow()");
			}
		});
		dialog.show();
		
		ATLog.i(TAG, "INFO. showMemLengthDialog()");
	}
}
