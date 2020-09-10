package com.hanmiit.app.rfid.blasterdemo.view.base;

import java.util.Locale;

import com.hanmiit.lib.ATRfidReader;
import com.hanmiit.lib.diagnostics.ATLog;
import com.hanmiit.lib.rfid.exception.ATRfidReaderException;
import com.hanmiit.lib.rfid.type.ActionState;
import com.hanmiit.lib.rfid.type.CommandType;
import com.hanmiit.lib.rfid.type.ResultCode;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.adapter.TagListAdapter;
import com.hanmiit.app.rfid.blasterdemo.dialog.WaitDialog;
import com.hanmiit.app.rfid.blasterdemo.type.MaskType;
import com.hanmiit.app.rfid.blasterdemo.util.ResUtil;
import com.hanmiit.app.rfid.blasterdemo.view.ReadMemoryActivity;
import com.hanmiit.app.rfid.blasterdemo.view.TagAccessActivity;
import com.hanmiit.app.rfid.blasterdemo.view.WriteMemoryActivity;

import android.content.Intent;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

public class BaseInventoryActivity extends RfidMaskActivity implements OnCheckedChangeListener {

	private static final String TAG = BaseInventoryActivity.class.getSimpleName();

	protected ListView lstTags;
	protected Switch swtDisplayPc;
	protected TextView txtCount;
	protected Button btnInventory;

	protected TagListAdapter adpTags;

	private MenuItem mnuReadMemory;
	private MenuItem mnuWriteMemory;
	private MenuItem mnuTagAccess;

	@Override
	protected void onStart() {
		super.onStart();

//		// Set Key Action
//		try {
//			getReader().setUseKeyAction(getKeyAction());
//		} catch (ATRfidReaderException e) {
//			ATLog.e(TAG, e, "ERROR. onStart() - Failed to set use key action");
//		}
		
//		ATLog.i(TAG, "INFO. onStart()");
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
	protected void onStop() {
		
		// Set Key Action
		try {
			getReader().stop();  //2016-06-17 mkj 
			getReader().setUseKeyAction(false);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. onStart() - Failed to set use key action");
		}

		ATLog.i(TAG, "INFO. onStart()");
		super.onStop();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		ATLog.i(TAG, "INFO. onActivityResult(%s, %s)", getRequestCode(requestCode), getResultCode(resultCode));

		if (resultCode == RESULT_DISCONNECTED) {
			setResult(RESULT_DISCONNECTED);
			finish();
			return;
		}

		switch (requestCode) {
		case AccessActivity.VIEW_READ_MEMORY:
		case AccessActivity.VIEW_WRITE_MEMORY:
		case AccessActivity.VIEW_TAG_ACCESS:
			enableActionWidgets(true);
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

		ATLog.i(TAG, "INFO. onCreateContextMenu()");

		if (v.getId() != R.id.tag_list || getReader().getAction() != ActionState.Stop)
			return;

		getMenuInflater().inflate(R.menu.inventory_menu, menu);

		mnuReadMemory = menu.findItem(R.id.read_memory);
		mnuWriteMemory = menu.findItem(R.id.write_memory);
		mnuTagAccess = menu.findItem(R.id.tag_access);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		ATLog.i(TAG, "INFO. onContextItemSelected(%s)", ResUtil.getId(item.getItemId()));

		Intent intent = null;
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		int position = menuInfo.position;

		if (position < 0)
			return false;

		mnuReadMemory.setEnabled(false);
		mnuWriteMemory.setEnabled(false);
		mnuTagAccess.setEnabled(false);

		String tag = adpTags.getItem(position, false);

		switch (item.getItemId()) {
		case R.id.read_memory:
			intent = new Intent(this, ReadMemoryActivity.class);
			intent.putExtra(KEY_MASK_TYPE, getMaskType().getCode());
			intent.putExtra(KEY_MASK, tag);
			startActivityForResult(intent, AccessActivity.VIEW_READ_MEMORY);
			break;
		case R.id.write_memory:
			intent = new Intent(this, WriteMemoryActivity.class);
			intent.putExtra(KEY_MASK_TYPE, getMaskType().getCode());
			intent.putExtra(KEY_MASK, tag);
			startActivityForResult(intent, AccessActivity.VIEW_WRITE_MEMORY);
			break;
		case R.id.tag_access:
			intent = new Intent(this, TagAccessActivity.class);
			intent.putExtra(KEY_MASK_TYPE, getMaskType().getCode());
			intent.putExtra(KEY_MASK, tag);
			startActivityForResult(intent, AccessActivity.VIEW_TAG_ACCESS);
			break;
		}
		return true;
	}

	@Override
	public void onClick(View v) {

		ATLog.i(TAG, "INFO. onClick(%s)", ResUtil.getId(v.getId()));

		switch (v.getId()) {
		case R.id.inventory:
			if (getReader().getAction() == ActionState.Stop) {
				startInventory();
			} else {
				stopInventory();
				
			}
			break;
		}
		super.onClick(v);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

		ATLog.i(TAG, "INFO. onCheckedChanged(%s, %s)", ResUtil.getId(buttonView.getId()), isChecked);

		switch (buttonView.getId()) {
		case R.id.display_pc:
			adpTags.setDisplayPc(isChecked);
			break;
		}
	}

	@Override
	public void onActionChanged(ATRfidReader reader, ActionState action) {
		ATLog.d(TAG, "EVENT. onActionChanged(%s)", action);

		adpTags.notifyDataSetChanged();

		enableActionWidgets(true);
	}

	@Override
	public void onCommandComplete(ATRfidReader reader, CommandType command) {
		ATLog.d(TAG, "EVENT. onCommandComplete(%s)", command);
		
		WaitDialog.hide();
		enableActionWidgets(true);
	}

	@Override
	public void onReadedTag(ATRfidReader reader, ActionState action, String tag, float rssi, float phase) {
		
		ATLog.i(TAG, "EVENT. onReadedTag(%s, [%s], %.2f, %.2f)", action, tag, rssi, phase);
		
		adpTags.addTag(tag, rssi, phase);
		txtCount.setText(String.format(Locale.US, "%d", adpTags.getCount()));
	}
	
	@Override
	public void onAccessResult(ATRfidReader reader, ResultCode code, ActionState action, String epc, String data,
			float rssi, float phase) {
		
		ATLog.i(TAG, "EVENT. onAccessResult(%s, [%s], [%s]%.2f, %.2f)", action, epc, data, rssi, phase);
		
		adpTags.addTag(epc, data, rssi, phase);
		txtCount.setText(String.format(Locale.US, "%d", adpTags.getCount()));
	}

	// Initialize Widgets
	@Override
	protected void initWidgets() {
		super.initWidgets();

		// Tag ListView
		lstTags = (ListView) findViewById(R.id.tag_list);
		adpTags = new TagListAdapter(this);
		lstTags.setAdapter(adpTags);
		adpTags.start();
		if (getMaskType() == MaskType.SelectionMask)
			registerForContextMenu(lstTags);

		// DisplayPC CheckBox
		swtDisplayPc = (Switch) findViewById(R.id.display_pc);
		swtDisplayPc.setOnCheckedChangeListener(this);
		swtDisplayPc.setChecked(adpTags.getDisplayPc());
		// Count TextView
		txtCount = (TextView) findViewById(R.id.tag_count);
		txtCount.setText(String.format(Locale.US, "%d", adpTags.getCount()));
		// Inventory Button
		btnInventory = (Button) findViewById(R.id.inventory);
		btnInventory.setOnClickListener(this);
		
		ATLog.i(TAG, "INFO. initWidgets()");
	}

	// Enable/Disable Action Widgets
	@Override
	protected void enableActionWidgets(boolean enabled) {
		super.enableActionWidgets(enabled);

		lstTags.setEnabled(isEnabledWidget(enabled));
		swtDisplayPc.setEnabled(isEnabledWidget(enabled));
		enableActionButton(btnInventory, enabled, R.string.action_inventory, R.string.action_stop);

		ATLog.i(TAG, "INFO. enableActionWidgets(%s)", enabled);
	}
	
	@Override
	protected void clearWidgets() {

		adpTags.clear();
		txtCount.setText(String.format(Locale.US, "%d", adpTags.getCount()));

		ATLog.i(TAG, "INFO. clearWidgets()");
	}

	//2016-06-01 mkj90 private --> protected
	protected void startInventory() {
		enableActionWidgets(false);
		int time = getOperationTime();
		try {
			getReader().setOperationTime(time);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. startInventory() - Failed to set operation time(%d)", time);
		}
		if (getReader().inventory() != ResultCode.NoError) {
			ATLog.e(TAG, "ERROR. startInventory() - Failed to start inventory()");
			enableActionWidgets(true);
			return;
		}
		ATLog.i(TAG, "INFO. startInventory()");
	}

	//2016-06-01 mkj90 private --> protected
	protected void stopInventory() {
		enableActionWidgets(false);
		if (getReader().stop() != ResultCode.NoError) {
			ATLog.e(TAG, "ERROR. stopInventory() - Failed to stop inventory()");
			enableActionWidgets(true);
			return;
		}
		ATLog.i(TAG, "INFO. stopInventory()");
	}
	
}
