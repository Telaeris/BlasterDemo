package com.hanmiit.app.rfid.blasterdemo.view.sub;

import com.hanmiit.lib.barcode.type.BarcodeType;
import com.hanmiit.lib.device.type.ConnectionState;
import com.hanmiit.lib.diagnostics.ATLog;
import com.hanmiit.lib.rfid.event.ATRfidEventListener;
import com.hanmiit.lib.rfid.exception.ATRfidReaderException;
import com.hanmiit.lib.rfid.params.SelectMaskEpcParam;
import com.hanmiit.lib.rfid.type.ActionState;
import com.hanmiit.lib.rfid.type.CommandType;
import com.hanmiit.lib.rfid.type.RemoteKeyState;
import com.hanmiit.lib.rfid.type.ResultCode;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.adapter.SelectMaskEpcAdapter;
import com.hanmiit.app.rfid.blasterdemo.dialog.IDialogResultListener;
import com.hanmiit.app.rfid.blasterdemo.dialog.SelectMaskEpcDialog;
import com.hanmiit.app.rfid.blasterdemo.dialog.WaitDialog;
import com.hanmiit.app.rfid.blasterdemo.util.ResUtil;
import com.hanmiit.lib.ATRfidManager;
import com.hanmiit.lib.ATRfidReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

public class EpcMaskActivity extends Activity implements ATRfidEventListener, OnClickListener, IDialogResultListener,
		OnItemClickListener, DialogInterface.OnClickListener {

	private static final String TAG = EpcMaskActivity.class.getSimpleName();
	
	private static final int RESULT_DISCONNECTED = Activity.RESULT_FIRST_USER + 1;
	private static final int REMOVE_QUESTION_DIALOG = 2003;

	private ATRfidReader mReader = null;

	private ListView lstMasks;
	private CheckBox chkMatchMode;
	private TextView txtCount;
	private Button btnMaskAdd;
	private Button btnMaskEdit;
	private Button btnMaskRemove;
	private Button btnMaskClear;
	private Button btnSave;
	private Button btnCancel;

	private SelectMaskEpcAdapter adpMasks;

	private SelectMaskEpcDialog mDialog;

	private Handler mEvent;

	// ************************************************************************
	// Activity Event Methods
	// ************************************************************************

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selection_mask_epc);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		ATLog.i(TAG, "INFO. onCreate()");

		// Get RFID Reader Instance
		mReader = ATRfidManager.getInstance();
		mReader.setEventListener(this);

		// Initialize Widget
		initWidgets();
		// Enable action widget
		enableActionWidgets(false);

		mEvent = new Handler();

		// Load Selection Mask
		WaitDialog.show(this, R.string.load_selection_mask);
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					SelectMaskEpcParam item = null;
					int count = mReader.getEpcMaskCount();

					for (int i = 0; i < count; i++) {
						item = mReader.getEpcMask(i);
						adpMasks.addItem(item);
					}
					final boolean isMatchMode = mReader.getEpcMaskMatchMode();
					mEvent.post(new Runnable() {

						@Override
						public void run() {
							chkMatchMode.setChecked(isMatchMode);
							adpMasks.notifyDataSetChanged();
							updateMaskCount();
							enableActionWidgets(true);
							WaitDialog.hide();
						}

					});
				} catch (ATRfidReaderException e) {
					ATLog.e(TAG, e, "ERROR. onCreate().$Runnable.run() - Failed to load select mask EPC");
				}
			}

		}).start();
	}

	@Override
	protected void onDestroy() {
		ATLog.i(TAG, "INFO. onDestroy()");

		mReader.removeEventListener(this);
		// Destroy RFID Reader
		ATRfidManager.onDestroy();

		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			ATLog.i(TAG, "INFO. onOptionsItemSelected(home)");
			setResult(Activity.RESULT_CANCELED);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		ATLog.i(TAG, "INFO. onClick(%s)", ResUtil.getId(v.getId()));

		switch (v.getId()) {
		case R.id.add_mask:
			showDialog(SelectMaskEpcDialog.NEW_ITEM);
			break;
		case R.id.edit_mask:
			showDialog(SelectMaskEpcDialog.MODIFY_ITEM);
			break;
		case R.id.remove_mask:
			showDialog(REMOVE_QUESTION_DIALOG);
			break;
		case R.id.clear_mask:
			int position = lstMasks.getCheckedItemPosition();
			if (position >= 0) {
				lstMasks.setItemChecked(position, false);
			}
			adpMasks.clear();
			enableActionWidgets(true);
			break;
		case R.id.save:
			enableActionWidgets(false);
			WaitDialog.show(this, R.string.save_selection_mask);
			final boolean isMatchMode = chkMatchMode.isChecked();
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						ATLog.i(TAG, "+++ INFO. onClick(save)$Runnable.run()");
						SelectMaskEpcParam item = null;
						int count = adpMasks.getCount();
						mReader.clearEpcMask();
						for (int i = 0; i < count; i++) {
							item = adpMasks.getItem(i);
							mReader.addEpcMask(item);
						}
						mReader.setEpcMaskMatchMode(isMatchMode);

						mEvent.post(new Runnable() {

							@Override
							public void run() {
								ATLog.i(TAG, "INFO. onClick(save)$Runnable.run()$Runable.run()");
								WaitDialog.hide();
								finish();
							}

						});

					} catch (ATRfidReaderException e) {
						ATLog.e(TAG, e, "--- ERROR. onClick(save)$Runnable.run() - Failed to save select mask EPC");
						return;
					}
					ATLog.i(TAG, "--- INFO. onClick(save)$Runnable.run()");
				}

			}).start();
			break;
		case R.id.cancel:
			finish();
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ATLog.i(TAG, "INFO. onItemClick(mask_list, %d, %d)", position, id);
		enableActionWidgets(true);
	}

	// ************************************************************************
	// Select Mask EPC Dialog Interface Methods
	// ************************************************************************

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case SelectMaskEpcDialog.NEW_ITEM:
			ATLog.i(TAG, "INFO. onCreateDialog(NEW_ITEM)");
			if (mDialog == null) {
				mDialog = new SelectMaskEpcDialog(this);
				mDialog.setResultListener(this);
			}
			return mDialog.getDialog();
		case SelectMaskEpcDialog.MODIFY_ITEM:
			ATLog.i(TAG, "INFO. onCreateDialog(MODIFY_ITEM)");
			if (mDialog == null) {
				mDialog = new SelectMaskEpcDialog(this);
				mDialog.setResultListener(this);
			}
			return mDialog.getDialog();
		case REMOVE_QUESTION_DIALOG:
			ATLog.i(TAG, "INFO. onCreateDialog(REMOVE_QUESTION_DIALOG)");
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setTitle(R.string.action_remove);
			builder.setMessage(R.string.remove_mask_message);
			builder.setPositiveButton(R.string.action_yes, this);
			builder.setNegativeButton(R.string.action_no, null);
			return builder.create();
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case SelectMaskEpcDialog.NEW_ITEM:
			ATLog.i(TAG, "INFO. onPrepareDialog(NEW_ITEM)");
			mDialog.setState(SelectMaskEpcDialog.NEW_ITEM);
			mDialog.clearItem();
			break;
		case SelectMaskEpcDialog.MODIFY_ITEM:
			ATLog.i(TAG, "INFO. onPrepareDialog(MODIFY_ITEM)");
			mDialog.setState(SelectMaskEpcDialog.MODIFY_ITEM);
			int position = lstMasks.getCheckedItemPosition();
			SelectMaskEpcParam item = adpMasks.getItem(position);
			mDialog.setItem(item);
			break;
		case REMOVE_QUESTION_DIALOG:
			ATLog.i(TAG, "INFO. onPrepareDialog(REMOVE_QUESTION_DIALOG)");
			break;
		}
	}

	@Override
	public void onOkClick(int what, DialogInterface dialog) {
		SelectMaskEpcParam item = null;

		switch (what) {
		case SelectMaskEpcDialog.NEW_ITEM:
			ATLog.i(TAG, "INFO. onOkClick(NEW_ITEM)");
			item = mDialog.getItem();
			adpMasks.addItem(item);
			updateMaskCount();
			break;
		case SelectMaskEpcDialog.MODIFY_ITEM:
			ATLog.i(TAG, "INFO. onOkClick(MODIFY_ITEM)");
			item = mDialog.getItem();
			int position = lstMasks.getCheckedItemPosition();
			adpMasks.updateItem(position, item);
			updateMaskCount();
			break;
		}
		enableActionWidgets(true);
	}

	@Override
	public void onCancelClick(int what, DialogInterface dialog) {
		switch (what) {
		case SelectMaskEpcDialog.NEW_ITEM:
			ATLog.i(TAG, "INFO. onCancelClick(NEW_ITEM)");
			break;
		case SelectMaskEpcDialog.MODIFY_ITEM:
			ATLog.i(TAG, "INFO. onCancelClick(MODIFY_ITEM)");
			break;
		}
		enableActionWidgets(true);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		ATLog.i(TAG, "INFO. DialogInterface.onClick()");
		int position = lstMasks.getCheckedItemPosition();
		if (position < 0) 
			return;
		lstMasks.setItemChecked(position, false);
		adpMasks.removeItem(position);
		updateMaskCount();
		enableActionWidgets(true);
	}

	// ************************************************************************
	// RFID Reader Event Methods
	// ************************************************************************
	@Override
	public void onStateChanged(ATRfidReader reader, ConnectionState state) {
		ATLog.i(TAG, "EVENT. onStateChanged(%s)", state);
		if (state == ConnectionState.Disconnected) {
			setResult(RESULT_DISCONNECTED);
			finish();
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
		ATLog.i(TAG, "EVENT. onDebugMessage([%s])", msg);
	}

	@Override
	public void onDetactBarcode(ATRfidReader reader, BarcodeType type, String codeId, String barcode) {
		ATLog.i(TAG, "EVENT. onDetactBarcode(%s, [%s], [%s])", type, codeId, barcode);
	}

	@Override
	public void onRemoteKeyStateChanged(ATRfidReader reader, RemoteKeyState state) {
		ATLog.i(TAG, "EVENT. onRemoteKeyStateChanged(%s)", state);
	}

	// ************************************************************************
	// Helper Methods
	// ************************************************************************

	// Update Mask Count
	private void updateMaskCount() {
		adpMasks.notifyDataSetChanged();
		int count = adpMasks.getCount();
		txtCount.setText("" + count);
		ATLog.i(TAG, "INFO. updateMaskCount()");
	}

	// Initialize Widgets
	private void initWidgets() {
		lstMasks = (ListView) findViewById(R.id.mask_list);
		adpMasks = new SelectMaskEpcAdapter(this);
		lstMasks.setAdapter(adpMasks);
		lstMasks.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lstMasks.setOnItemClickListener(this);

		chkMatchMode = (CheckBox) findViewById(R.id.match_mode);

		txtCount = (TextView) findViewById(R.id.mask_count);

		btnMaskAdd = (Button) findViewById(R.id.add_mask);
		btnMaskAdd.setOnClickListener(this);
		btnMaskEdit = (Button) findViewById(R.id.edit_mask);
		btnMaskEdit.setOnClickListener(this);
		btnMaskRemove = (Button) findViewById(R.id.remove_mask);
		btnMaskRemove.setOnClickListener(this);
		btnMaskClear = (Button) findViewById(R.id.clear_mask);
		btnMaskClear.setOnClickListener(this);

		btnSave = (Button) findViewById(R.id.save);
		btnSave.setOnClickListener(this);

		btnCancel = (Button) findViewById(R.id.cancel);
		btnCancel.setOnClickListener(this);
		ATLog.i(TAG, "INFO. initWidgets()");
	}

	// Enable/Disable Action Widgets
	private void enableActionWidgets(boolean enabled) {
		int position = lstMasks.getCheckedItemPosition();
		int count = lstMasks.getCount();

		lstMasks.setEnabled(enabled);
		chkMatchMode.setEnabled(enabled);
		btnMaskAdd.setEnabled(enabled);
		btnMaskEdit.setEnabled(enabled && position >= 0);
		btnMaskRemove.setEnabled(enabled && position >= 0);
		btnMaskClear.setEnabled(enabled && count > 0);
		btnSave.setEnabled(enabled);
		btnCancel.setEnabled(enabled);
		ATLog.i(TAG, "INFO. enableActionWidgets(%s)", enabled);
	}
}
