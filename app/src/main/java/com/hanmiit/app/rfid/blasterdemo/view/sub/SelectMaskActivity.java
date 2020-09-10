package com.hanmiit.app.rfid.blasterdemo.view.sub;

import com.hanmiit.lib.device.type.ConnectionState;
import com.hanmiit.lib.diagnostics.ATLog;
import com.hanmiit.lib.rfid.exception.ATRfidReaderException;
import com.hanmiit.lib.rfid.params.SelectMaskParam;
import com.hanmiit.lib.rfid.type.SelectFlag;
import com.hanmiit.lib.rfid.type.SessionFlag;
import com.hanmiit.lib.rfid.type.SessionType;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.adapter.SelectMaskAdapter;
import com.hanmiit.app.rfid.blasterdemo.adapter.SpinnerAdapter;
import com.hanmiit.app.rfid.blasterdemo.dialog.IDialogResultListener;
import com.hanmiit.app.rfid.blasterdemo.dialog.SelectMaskDialog;
import com.hanmiit.app.rfid.blasterdemo.dialog.WaitDialog;
import com.hanmiit.app.rfid.blasterdemo.util.ResUtil;
import com.hanmiit.app.rfid.blasterdemo.view.base.RfidActivity;
import com.hanmiit.lib.ATRfidReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

public class SelectMaskActivity extends RfidActivity
		implements OnClickListener, IDialogResultListener, OnItemClickListener {

	private static final String TAG = SelectMaskActivity.class.getSimpleName();

	private static final int RESULT_DISCONNECTED = Activity.RESULT_FIRST_USER + 1;
	private static final int REMOVE_QUESTION_DIALOG = 2003;

	private static final int MIN_MASK = 0;
	private static final int MAX_MASK = 8;

	private ListView lstMasks;
	private Button btnAdd;
	private Button btnEdit;
	private Button btnRemove;
	private Spinner spnSelectFlag;
	private Spinner spnInventorySession;
	private Spinner spnSessionFlag;
	private Button btnSave;
	private Button btnCancel;

	private SelectMaskAdapter adpMasks;
	private SpinnerAdapter adpSelectFlag;
	private SpinnerAdapter adpInventorySession;
	private SpinnerAdapter adpSessionFlag;

	private SelectMaskDialog mDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selection_mask);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		ATLog.i(TAG, "INFO. onCreate()");

		// Create Reader
		createReader();

		// Initalize Widget
		initWidgets();

		// Initialize Reader
		initReader();

		// Enable action widget
		enableActionWidgets(false);

		// Load Selection Mask
		WaitDialog.show(this, R.string.load_selection_mask);

		new Thread(mLoadMask).start();
	}

	@Override
	protected void onDestroy() {

		ATLog.i(TAG, "INFO. onDestroy()");

		// Destroy Reader
		destroyReader();

		super.onDestroy();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		ATLog.i(TAG, "INFO. onClick(%s)", ResUtil.getId(v.getId()));

		switch (v.getId()) {
		case R.id.add_mask:
			showDialog(SelectMaskDialog.NEW_ITEM);
			break;
		case R.id.edit_mask:
			showDialog(SelectMaskDialog.MODIFY_ITEM);
			break;
		case R.id.remove_mask:
			showDialog(REMOVE_QUESTION_DIALOG);
			break;
		case R.id.save:
			enableActionWidgets(false);
			WaitDialog.show(this, R.string.save_selection_mask);
			new Thread(mSaveMask).start();
			break;
		case R.id.cancel:
			finish();
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		switch (parent.getId()) {
		case R.id.mask_list:
			ATLog.i(TAG, "INFO. onItemClick(%s, %d, %d)", ResUtil.getId(parent.getId()), position, id);
			enableActionWidgets(true);
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case SelectMaskDialog.NEW_ITEM:
			ATLog.i(TAG, "INFO. onCreateDialog(NEW_ITEM)");
			if (mDialog == null) {
				mDialog = new SelectMaskDialog(this);
				mDialog.setResultListener(this);
			}
			return mDialog.getDialog();
		case SelectMaskDialog.MODIFY_ITEM:
			ATLog.i(TAG, "INFO. onCreateDialog(MODIFY_ITEM)");
			if (mDialog == null) {
				mDialog = new SelectMaskDialog(this);
				mDialog.setResultListener(this);
			}
			return mDialog.getDialog();
		case REMOVE_QUESTION_DIALOG:
			ATLog.i(TAG, "INFO. onCreateDialog(REMOVE_QUESTION_DIALOG)");
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setTitle(R.string.action_remove);
			builder.setMessage(R.string.remove_mask_message);
			builder.setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					int position = lstMasks.getCheckedItemPosition();
					if (position < 0)
						return;
					lstMasks.setItemChecked(position, false);
					adpMasks.removeItem(position);
					enableActionWidgets(true);
				}
			});
			builder.setNegativeButton(R.string.action_no, null);
			return builder.create();
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case SelectMaskDialog.NEW_ITEM:
			ATLog.i(TAG, "INFO. onPrepareDialog(NEW_ITEM)");
			mDialog.initDialog();
			break;
		case SelectMaskDialog.MODIFY_ITEM:
			ATLog.i(TAG, "INFO. onPrepareDialog(MODIFY_ITEM)");
			int position = lstMasks.getCheckedItemPosition();
			SelectMaskParam param = adpMasks.getItem(position);
			mDialog.initDialog(param);
			break;
		}
	}

	@Override
	public void onOkClick(int what, DialogInterface dialog) {
		SelectMaskParam param = null;

		switch (what) {
		case SelectMaskDialog.NEW_ITEM:
			ATLog.i(TAG, "INFO. onOkClick(NEW_ITEM)");
			param = mDialog.getItem();
			adpMasks.addItem(param);
			break;
		case SelectMaskDialog.MODIFY_ITEM:
			ATLog.i(TAG, "INFO. onOkClick(MODIFY_ITEM)");
			int position = lstMasks.getCheckedItemPosition();
			param = mDialog.getItem();
			adpMasks.updateItem(position, param);
			break;
		}
		enableActionWidgets(true);
	}

	@Override
	public void onCancelClick(int what, DialogInterface dialog) {
		switch (what) {
		case SelectMaskDialog.NEW_ITEM:
			ATLog.i(TAG, "INFO. onCancelClick(NEW_ITEM)");
			break;
		case SelectMaskDialog.MODIFY_ITEM:
			ATLog.i(TAG, "INFO. onCancelClick(MODIFY_ITEM)");
			break;
		}
		enableActionWidgets(true);
	}

	@Override
	public void onStateChanged(ATRfidReader reader, ConnectionState state) {

		ATLog.i(TAG, "EVENT. onStateChanged(%s)", state);
		if (state == ConnectionState.Disconnected) {
			setResult(RESULT_DISCONNECTED);
			finish();
		}
	}

	@Override
	protected void initReader() {
		ATLog.i(TAG, "INFO. initReader()");
	}

	// Initialize Widgets
	@Override
	protected void initWidgets() {
		lstMasks = (ListView) findViewById(R.id.mask_list);
		adpMasks = new SelectMaskAdapter(this);
		lstMasks.setAdapter(adpMasks);
		lstMasks.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lstMasks.setOnItemClickListener(this);

		btnAdd = (Button) findViewById(R.id.add_mask);
		btnAdd.setOnClickListener(this);

		btnEdit = (Button) findViewById(R.id.edit_mask);
		btnEdit.setOnClickListener(this);

		btnRemove = (Button) findViewById(R.id.remove_mask);
		btnRemove.setOnClickListener(this);

		spnSelectFlag = (Spinner) findViewById(R.id.select_flag);
		adpSelectFlag = new SpinnerAdapter(this, android.R.layout.simple_list_item_1);
		spnSelectFlag.setAdapter(adpSelectFlag);

		spnInventorySession = (Spinner) findViewById(R.id.inventory_session);
		adpInventorySession = new SpinnerAdapter(this, android.R.layout.simple_list_item_1);
		spnInventorySession.setAdapter(adpInventorySession);

		spnSessionFlag = (Spinner) findViewById(R.id.session_flag);
		adpSessionFlag = new SpinnerAdapter(this, android.R.layout.simple_list_item_1);
		spnSessionFlag.setAdapter(adpSessionFlag);

		btnSave = (Button) findViewById(R.id.save);
		btnSave.setOnClickListener(this);

		btnCancel = (Button) findViewById(R.id.cancel);
		btnCancel.setOnClickListener(this);
		ATLog.i(TAG, "INFO. initWidgets()");
	}

	// Enable/Disable Action Widgets
	@Override
	protected void enableActionWidgets(boolean enabled) {
		int position = lstMasks.getCheckedItemPosition();
		int count = lstMasks.getCount();

		lstMasks.setEnabled(enabled);
		btnAdd.setEnabled(enabled && count < MAX_MASK);
		btnEdit.setEnabled(enabled && position >= 0);
		btnRemove.setEnabled(enabled && position >= 0);
		spnSelectFlag.setEnabled(enabled);
		spnInventorySession.setEnabled(enabled);
		spnSessionFlag.setEnabled(enabled);
		btnSave.setEnabled(enabled);
		btnCancel.setEnabled(enabled);
		ATLog.i(TAG, "INFO. enableActionWidgets(%s)", enabled);
	}

	// Asynchronous Load Selection Mask
	private Runnable mLoadMask = new Runnable() {

		@Override
		public void run() {

			ATLog.i(TAG, "+++ INFO. $mLoadMask.run()");

			SelectMaskParam param;

			try {
				if (getReader().getUseSelectionMask() == SelectFlag.NotUsed) {
					ATLog.d(TAG, "@@@ DEBUG. $mLoadMask.run() - Not used selection mask");
					runOnUiThread(mLoadedMask);
					return;
				}
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. $mLoadMask.run() - Failed to get used selection mask");
				runOnUiThread(mLoadedMask);
				return;
			}

			for (int i = ATRfidReader.MIN_SELECTION_MASK; i < ATRfidReader.MAX_SELECTION_MASK; i++) {
				try {
					if (!getReader().usedSelectionMask(i))
						break;
					param = getReader().getSelectionMask(i);
					ATLog.d(TAG, "@@@ DEBUG. $mLoadMask.run() - Load Selection Mask [%d, [%s]]", i, param);
				} catch (ATRfidReaderException e) {
					ATLog.e(TAG, e, "ERROR. $mLoadMask.run() - Failed to get selection mask [%d]", i);
					continue;
				}
				adpMasks.addItemNoUpdate(param);
			}
			runOnUiThread(mLoadedMask);

			ATLog.i(TAG, "--- INFO. $mLoadMask.run()");
		}

	};

	private Runnable mLoadedMask = new Runnable() {

		@Override
		public void run() {
			ATLog.i(TAG, "+++ INFO. $mLoadedMask.run()");

			// Update Mask List
			adpMasks.notifyDataSetChanged();

			// Fill Select Flag Spinner
			for (SelectFlag item : SelectFlag.values()) {
				if (item == SelectFlag.NotUsed)
					continue;
				adpSelectFlag.addItem(item.getValue(), item.toString());
			}
			adpSelectFlag.notifyDataSetChanged();

			// Fill Session Type Spinner
			for (SessionType item : SessionType.values()) {
				adpInventorySession.addItem(item.getValue(), item.toString());
			}
			adpInventorySession.notifyDataSetChanged();

			// Fill Flip Mode Spinner
			for (SessionFlag item : SessionFlag.values()) {
				adpSessionFlag.addItem(item.getValue(), item.toString());
			}
			adpSessionFlag.notifyDataSetChanged();

			// Update Select Flag
			try {
				SelectFlag selectFlag = getReader().getUseSelectionMask();
				if (selectFlag == SelectFlag.NotUsed) {
					selectFlag = SelectFlag.SL;
				}
				spnSelectFlag.setSelection(adpSelectFlag.indexOf(selectFlag.getValue()));
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. $mLoadedMask.run() - Failed to get select flag");
			}
			// Update Session Type
			try {
				SessionType sessionType = getReader().getInventorySession();
				spnInventorySession.setSelection(adpInventorySession.indexOf(sessionType.getValue()));
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. $mLoadedMask.run() - Failed to get inventory session");
			}
			// Update Session Flag
			try {
				SessionFlag flag = getReader().getSessionFlag();
				spnSessionFlag.setSelection(adpSessionFlag.indexOf(flag.getValue()));
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. $mLoadedMask.run() - Failed to get session flag");
			}

			enableActionWidgets(true);
			WaitDialog.hide();

			ATLog.i(TAG, "--- INFO. $mLoadedMask.run()");
		}

	};

	// Asynchronous Save Selection Mask
	private Runnable mSaveMask = new Runnable() {

		@Override
		public void run() {
			ATLog.i(TAG, "+++ INFO. $mSaveMask.run()");

			SelectMaskParam param;
			for (int i = MIN_MASK; i < MAX_MASK; i++) {
				param = adpMasks.getItem(i);
				try {
					if (param == null) {
						getReader().removeSelectionMask(i);
					} else {
						getReader().setSelectionMask(i, param);
					}
				} catch (ATRfidReaderException e) {
					ATLog.e(TAG, e, "ERROR. $mSaveMask.run() - Failed to set selection mask [%d, [%s]]", i, param);
					continue;
				}
			}
			if (adpMasks.getCount() > 0) {
				SelectFlag selectFlag = SelectFlag
						.valueOf(adpSelectFlag.getValue(spnSelectFlag.getSelectedItemPosition()));
				try {
					getReader().setUseSelectionMask(selectFlag);
				} catch (ATRfidReaderException e) {
					ATLog.e(TAG, e, "ERROR. $mSaveMask.run() - Failed to set use selection mask [%s]", selectFlag);
				}
			} else {
				try {
					getReader().clearSelectionMask();
				} catch (ATRfidReaderException e) {
					ATLog.e(TAG, e, "ERROR. $mSaveMask.run() - Failed to clear selection mask");
				}
			}

			SessionType sessionType = SessionType
					.valueOf(adpInventorySession.getValue(spnInventorySession.getSelectedItemPosition()));
			try {
				getReader().setInventorySession(sessionType);
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. $mSaveMask.run() - Failed to set inventory session [%s]", sessionType);
			}
			SessionFlag flag = SessionFlag.valueOf(adpSessionFlag.getValue(spnSessionFlag.getSelectedItemPosition()));
			try {
				getReader().setSessionFlag(flag);
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. $mSaveMask.run() - Failed to set session flag [%s]", flag);
			}
			runOnUiThread(mSavedMask);

			ATLog.i(TAG, "--- INFO. $mSaveMask.run()");
		}

	};

	private Runnable mSavedMask = new Runnable() {

		@Override
		public void run() {
			ATLog.i(TAG, "INFO. $mSavedMask.run()");
			WaitDialog.hide();
			finish();
		}

	};
}
