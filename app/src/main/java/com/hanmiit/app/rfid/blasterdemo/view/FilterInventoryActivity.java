package com.hanmiit.app.rfid.blasterdemo.view;

import com.hanmiit.lib.rfid.exception.ATRfidReaderException;
import com.hanmiit.lib.rfid.type.ActionState;
import com.hanmiit.lib.rfid.type.CommandType;
import com.hanmiit.lib.rfid.type.ResultCode;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.dialog.WaitDialog;
import com.hanmiit.app.rfid.blasterdemo.util.ResUtil;
import com.hanmiit.app.rfid.blasterdemo.view.base.BaseInventoryActivity;

import java.util.Locale;

import com.hanmiit.lib.ATRfidReader;
import com.hanmiit.lib.diagnostics.ATLog;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

public class FilterInventoryActivity extends BaseInventoryActivity implements OnCheckedChangeListener {

	private static final String TAG = FilterInventoryActivity.class.getSimpleName();
	
	private static final int MAX_TAG_COUNT = 10000;
	
	private Switch swtStoredMode;
	private Switch swtReportMode;
	private TextView txtStoredTagCount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filter_inventory);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		ATLog.i(TAG, "INFO. onCreate()");
		
		// Create Reader
		createReader();

		// Initialize Mask
		initMask();

		// Initialize Widget
		initWidgets();

		// Initialize Reader
		initReader();

		// Enable action widget
		enableActionWidgets(true);
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
		case R.id.stored_mode:
			try {
				getReader().setStoredMode(isChecked);
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. onCheckedChanged(%s, %s) - Failed to set stored mode",
						ResUtil.getId(buttonView.getId()), isChecked);
			}
			break;
		case R.id.report_mode:
			try {
				getReader().setReportMode(isChecked);
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. onCheckedChanged(%s, %s) - Failed to set report mode",
						ResUtil.getId(buttonView.getId()), isChecked);
			}
			break;
		}
	}

	
	// Initialize Reader
	@Override
	protected void initReader() {
		super.initReader();

		// Set Reader Continuous Mode
		try {
			getReader().setContinuousMode(true);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. initReader() - Failed to set continuous mode enable");
		}
		// Set Stored Mode Disable
		try {
			getReader().setStoredMode(true);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. initReader() - Failed to set stored mode");
		}
		swtStoredMode.setChecked(true);
		// Set Rpoert Mode Disable
		try {
			getReader().setReportMode(true);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. initReader() - Failed to set report mode");
		}
		swtReportMode.setChecked(true);
		
		ATLog.i(TAG, "INFO. initReader()");
	}

	@Override
	public void onActionChanged(ATRfidReader reader, ActionState action) {
		super.onActionChanged(reader, action);
		int count = 0;
		if (action == ActionState.Stop) {
			try {
				count = getReader().getStoredTagCount();
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. onActionChanged() - Failed to get stored tag count");
				return;
			}
			txtStoredTagCount.setText(String.format(Locale.US, "%d", count));
			if (count >= MAX_TAG_COUNT) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.inventory_complete);
				builder.setPositiveButton(R.string.action_ok, null);
				builder.setCancelable(true);
				builder.show();
			}
		}
	}

	@Override
	public void onCommandComplete(ATRfidReader reader, CommandType command) {
		super.onCommandComplete(reader, command);
		displayStoredTagCount();
	}

	// Initialize Widgets
	@Override
	protected void initWidgets() {
		super.initWidgets();

		// Stored Mode CheckBox
		swtStoredMode = (Switch) findViewById(R.id.stored_mode);
		swtStoredMode.setOnCheckedChangeListener(this);
		// Report Mode CheckBox
		swtReportMode = (Switch) findViewById(R.id.report_mode);
		swtReportMode.setOnCheckedChangeListener(this);
		
		txtStoredTagCount = (TextView) findViewById(R.id.stored_tag_count);
		displayStoredTagCount();
		
		ATLog.i(TAG, "INFO. initWidgets()");
	}

	// Enable/Disable Action Widgets
	@Override
	protected void enableActionWidgets(boolean enabled) {
		super.enableActionWidgets(enabled);

		swtStoredMode.setEnabled(isEnabledWidget(enabled));
		swtReportMode.setEnabled(isEnabledWidget(enabled));

		ATLog.i(TAG, "INFO. enableActionWidgets(%s)", enabled);
	}

	@Override
	protected void clearWidgets() {
		super.clearWidgets();

		if (swtStoredMode.isChecked()) {
			ResultCode res;
			enableActionWidgets(false);
			WaitDialog.show(this, R.string.remove_all_stored_tag_message);
			if ((res = getReader().removeAllStoredTags()) != ResultCode.NoError) {
				ATLog.e(TAG, "ERROR. clearWidgets() - Failed to remove all stored tag [%s]", res);
				WaitDialog.hide();
				enableActionWidgets(true);
				displayStoredTagCount();
			}
		}

		ATLog.i(TAG, "INFO. clearWidgets()");
	}

	// Display Stored Tag Count
	private void displayStoredTagCount() {
		int count = 0;
		try {
			count = getReader().getStoredTagCount();
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. displayStoredTagCount() - Failed to get stored tag count");
			count = 0;
		}
		txtStoredTagCount.setText(String.format(Locale.US, "%d", count));
		ATLog.i(TAG, "INFO. displayStoredTagCount()");
	}
}
