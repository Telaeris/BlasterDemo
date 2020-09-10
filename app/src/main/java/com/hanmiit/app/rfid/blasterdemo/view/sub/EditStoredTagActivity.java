package com.hanmiit.app.rfid.blasterdemo.view.sub;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import com.hanmiit.lib.barcode.type.BarcodeType;
import com.hanmiit.lib.device.type.ConnectionState;
import com.hanmiit.lib.diagnostics.ATLog;
import com.hanmiit.lib.rfid.event.ATRfidEventListener;
import com.hanmiit.lib.rfid.type.ActionState;
import com.hanmiit.lib.rfid.type.CommandType;
import com.hanmiit.lib.rfid.type.RemoteKeyState;
import com.hanmiit.lib.rfid.type.ResultCode;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.data.SharedData;
import com.hanmiit.app.rfid.blasterdemo.dialog.IDialogResultListener;
import com.hanmiit.app.rfid.blasterdemo.dialog.OnFileSelectedListener;
import com.hanmiit.app.rfid.blasterdemo.dialog.OpenFileDialog;
import com.hanmiit.app.rfid.blasterdemo.dialog.SaveFileDialog;
import com.hanmiit.app.rfid.blasterdemo.dialog.StoredTagDialog;
import com.hanmiit.app.rfid.blasterdemo.dialog.WaitDialog;
import com.hanmiit.app.rfid.blasterdemo.util.ResUtil;
import com.hanmiit.lib.ATRfidManager;
import com.hanmiit.lib.ATRfidReader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class EditStoredTagActivity extends Activity implements ATRfidEventListener, OnClickListener,
		OnItemClickListener, IDialogResultListener, DialogInterface.OnClickListener {

	private static final String TAG = EditStoredTagActivity.class.getSimpleName();

	private static final String KEY_TAG_LIST = "tag_list";
	private static final int RESULT_DISCONNECTED = Activity.RESULT_FIRST_USER + 1;
	private static final int REMOVE_QUESTION_DIALOG = 2003;
	private static final int IMPORT_DIALOG = 2004;
	private static final int EXPORT_DIALOG = 2005;

	private ATRfidReader mReader = null;

	private TextView txtCount;
	private ListView lstTags;
	private Button btnAdd;
	private Button btnEdit;
	private Button btnRemove;
	private Button btnClear;
	private Button btnImport;
	private Button btnExport;
	private Button btnOk;
	private Button btnCancel;

	private ArrayAdapter<String> adpTags;
	private ArrayList<String> aryTags;

	private StoredTagDialog dlgTag = null;
	private OpenFileDialog dlgImport = null;
	private SaveFileDialog dlgExport = null;

	private ImportThread mImport = null;
	private ExportThread mExport = null;

	// ************************************************************************
	// Activity Event Methods
	// ************************************************************************

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_stored_tag);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		ATLog.i(TAG, "INFO. onCreate()");

		mReader = ATRfidManager.getInstance();
		mReader.setEventListener(this);

//		Intent intent = getIntent();
//		String[] tags = intent.getExtras().getStringArray(KEY_TAG_LIST);
//
//		aryTags = new ArrayList<String>();
//		Collections.addAll(aryTags, tags);

		aryTags = SharedData.getTagList();
		if (aryTags == null) {
			aryTags = new ArrayList<String>();
		}
		// Initialize Widget
		initWidgets();
		// Enable action widget
		enableActionWidgets(true);
	}

	@Override
	protected void onDestroy() {

		ATLog.i(TAG, "INFO. onDestroy()");

		mReader.removeEventListener(this);
		// Destroy Rfid Reader
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
		case R.id.add:
			showDialog(StoredTagDialog.NEW_TAG);
			break;
		case R.id.edit:
			showDialog(StoredTagDialog.EDIT_TAG);
			break;
		case R.id.remove:
			showDialog(REMOVE_QUESTION_DIALOG);
			break;
		case R.id.clear:
			int position = lstTags.getCheckedItemPosition();
			if (position >= 0) {
				lstTags.setItemChecked(position, false);
			}
			aryTags.clear();
			adpTags.notifyDataSetChanged();
			displayCount();
			enableActionWidgets(true);
			break;
		case R.id.import_tags:
			showDialog(IMPORT_DIALOG);
			break;
		case R.id.export_tags:
			showDialog(EXPORT_DIALOG);
			break;
		case R.id.ok:
//			Intent intent = new Intent();
//			intent.putExtra(KEY_TAG_LIST, aryTags);
//			setResult(Activity.RESULT_OK, intent);
			SharedData.setTagList(aryTags);
			setResult(Activity.RESULT_OK);
			finish();
			break;
		case R.id.cancel:
			setResult(Activity.RESULT_CANCELED);
			finish();
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ATLog.i(TAG, "INFO. onItemClick(tag_list, %d, %d)", position, id);
		// Enable action widget
		enableActionWidgets(true);
	}

	// ************************************************************************
	// Dialog Interface Methods
	// ************************************************************************

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case StoredTagDialog.NEW_TAG:
			ATLog.i(TAG, "INFO. onCreateDialog(NEW_TAG)");
			if (dlgTag == null) {
				dlgTag = new StoredTagDialog(this, R.string.add_stored_tag);
				dlgTag.setResultListener(this);
			}
			return dlgTag.getDialog();
		case StoredTagDialog.EDIT_TAG:
			ATLog.i(TAG, "INFO. onCreateDialog(EDIT_TAG)");
			if (dlgTag == null) {
				dlgTag = new StoredTagDialog(this, R.string.edit_stored_tag);
				dlgTag.setResultListener(this);
			}
			return dlgTag.getDialog();
		case REMOVE_QUESTION_DIALOG:
			ATLog.i(TAG, "INFO. onCreateDialog(REMOVE_QUESTION_DIALOG)");
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setTitle(R.string.action_remove);
			builder.setMessage(R.string.remove_mask_message);
			builder.setPositiveButton(R.string.action_yes, this);
			builder.setNegativeButton(R.string.action_no, null);
			return builder.create();
		case IMPORT_DIALOG:
			ATLog.i(TAG, "INFO. onCreateDialog(IMPORT_DIALOG)");
			dlgImport = new OpenFileDialog(this);
			dlgImport.setOnFileSelectedListener(mImportFileSelected);
			return dlgImport.create();
		case EXPORT_DIALOG:
			ATLog.i(TAG, "INFO. onCreateDialog(EXPORT_DIALOG)");
			dlgExport = new SaveFileDialog(this);
			dlgExport.setOnFileSelectedListener(mExportFileSelected);
			return dlgExport.create();
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case StoredTagDialog.NEW_TAG:
			ATLog.i(TAG, "INFO. onPrepareDialog(NEW_TAG)");
			dlgTag.setState(StoredTagDialog.NEW_TAG);
			dlgTag.setTag("");
			break;
		case StoredTagDialog.EDIT_TAG:
			ATLog.i(TAG, "INFO. onPrepareDialog(EDIT_TAG)");
			dlgTag.setState(StoredTagDialog.EDIT_TAG);
			int position = lstTags.getCheckedItemPosition();
			dlgTag.setTag(aryTags.get(position));
			break;
		case REMOVE_QUESTION_DIALOG:
			ATLog.i(TAG, "INFO. onPrepareDialog(REMOVE_QUESTION_DIALOG)");
			break;
		case IMPORT_DIALOG:
			ATLog.i(TAG, "INFO. onPrepareDialog(IMPORT_DIALOG)");
			break;
		case EXPORT_DIALOG:
			ATLog.i(TAG, "INFO. onPrepareDialog(EXPORT_DIALOG)");
			long now = System.currentTimeMillis();
			Date date = new Date(now);
			SimpleDateFormat sdfNow = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
			String fileName = String.format("StoredTagList_%s.txt", sdfNow.format(date));
			dlgExport.setFileName(fileName);
			break;
		}
	}

	@Override
	public void onOkClick(int what, DialogInterface dialog) {
		String tag = null;

		switch (what) {
		case StoredTagDialog.NEW_TAG:
			ATLog.i(TAG, "INFO. onOkClick(NEW_TAG)");
			tag = dlgTag.getTag();
			if (tag != null && !tag.equals("")) {
				aryTags.add(tag);
				adpTags.notifyDataSetChanged();
				displayCount();
			}
			break;
		case StoredTagDialog.EDIT_TAG:
			ATLog.i(TAG, "INFO. onOkClick(EDIT_TAG)");
			tag = dlgTag.getTag();
			if (tag != null && !tag.equals("")) {
				int position = lstTags.getCheckedItemPosition();
				aryTags.set(position, tag);
				adpTags.notifyDataSetChanged();
				displayCount();
			}
			break;
		}
		enableActionWidgets(true);
	}

	@Override
	public void onCancelClick(int what, DialogInterface dialog) {
		switch (what) {
		case StoredTagDialog.NEW_TAG:
			ATLog.i(TAG, "INFO. onCancelClick(NEW_TAG)");
			break;
		case StoredTagDialog.EDIT_TAG:
			ATLog.i(TAG, "INFO. onCancelClick(EDIT_TAG)");
			break;
		}
		enableActionWidgets(true);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		ATLog.i(TAG, "INFO. onClick()");
		
		int position = lstTags.getCheckedItemPosition();
		if (position < 0)
			return;
		aryTags.remove(position);
		adpTags.notifyDataSetChanged();
		lstTags.setItemChecked(position, false);
		displayCount();
		enableActionWidgets(true);
	}

	private OnFileSelectedListener mImportFileSelected = new OnFileSelectedListener() {

		@Override
		public void onSelected(String path, String fileName) {
			ATLog.i(TAG, "INFO. $mImportFileSelected.onSelected([%s], [%s])", path, fileName);
			enableActionWidgets(false);
			WaitDialog.show(EditStoredTagActivity.this, R.string.import_stored_tag_message);
			File filePath = new File(path + File.separator + fileName);
			mImport = new ImportThread(filePath.getAbsolutePath());
			btnImport.post(mImport);
		}
	};

	private OnFileSelectedListener mExportFileSelected = new OnFileSelectedListener() {

		@Override
		public void onSelected(String path, String fileName) {
			ATLog.i(TAG, "INFO. $mExportFileSelected.onSelected([%s], [%s])", path, fileName);
			enableActionWidgets(false);
			WaitDialog.show(EditStoredTagActivity.this, R.string.export_stored_tag_message);
			File filePath = new File(path + File.separator + fileName);
			mExport = new ExportThread(filePath.getAbsolutePath());
			btnExport.post(mExport);
		}
	};

	// ************************************************************************
	// RFID Reader Event Methods
	// ************************************************************************

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
		ATLog.d(TAG, "EVENT. onCommandComplete(%s)", command);
	}

	@Override
	public void onLoadTag(ATRfidReader reader, String tag) {
		ATLog.d(TAG, "EVENT. onLoadTag([%s])", tag);
	}

	@Override
	public void onDebugMessage(ATRfidReader reader, String msg) {
		ATLog.d(TAG, "EVENT. onDebugMessage([%s])", msg);
	}

	@Override
	public void onDetactBarcode(ATRfidReader reader, BarcodeType type, String codeId, String barcode) {
		ATLog.d(TAG, "EVENT. onDetactBarcode(%s, [%s], [%s])", type, codeId, barcode);
	}

	@Override
	public void onRemoteKeyStateChanged(ATRfidReader reader, RemoteKeyState state) {
		ATLog.d(TAG, "EVENT. onRemoteKeyStateChanged(%s)", state);
	}

	// ************************************************************************
	// Inner Helper Methods
	// ************************************************************************

	// Initialize Widgets
	private void initWidgets() {
		txtCount = (TextView) findViewById(R.id.count);
		displayCount();

		lstTags = (ListView) findViewById(R.id.tag_list);
		adpTags = new ArrayAdapter<String>(this, R.layout.item_tag_single_list, aryTags);
		lstTags.setAdapter(adpTags);
		lstTags.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lstTags.setOnItemClickListener(this);

		btnAdd = (Button) findViewById(R.id.add);
		btnAdd.setOnClickListener(this);

		btnEdit = (Button) findViewById(R.id.edit);
		btnEdit.setOnClickListener(this);

		btnRemove = (Button) findViewById(R.id.remove);
		btnRemove.setOnClickListener(this);

		btnClear = (Button) findViewById(R.id.clear);
		btnClear.setOnClickListener(this);

		btnImport = (Button) findViewById(R.id.import_tags);
		btnImport.setOnClickListener(this);

		btnExport = (Button) findViewById(R.id.export_tags);
		btnExport.setOnClickListener(this);

		btnOk = (Button) findViewById(R.id.ok);
		btnOk.setOnClickListener(this);

		btnCancel = (Button) findViewById(R.id.cancel);
		btnCancel.setOnClickListener(this);

		ATLog.i(TAG, "INFO. initWidgets()");
	}

	// Enable/Disable Action Widgets
	private void enableActionWidgets(boolean enabled) {
		int position = lstTags.getCheckedItemPosition();
		int count = aryTags.size();

		lstTags.setEnabled(enabled);
		btnAdd.setEnabled(enabled);
		btnEdit.setEnabled(enabled && position >= 0);
		btnRemove.setEnabled(enabled && position >= 0);
		btnClear.setEnabled(enabled && count > 0);
		btnImport.setEnabled(enabled);
		btnExport.setEnabled(enabled && count > 0);
		btnOk.setEnabled(enabled);
		btnCancel.setEnabled(enabled);

		ATLog.i(TAG, "INFO. enableActionWidgets(%s)", enabled);
	}

	private void displayCount() {
		int count = aryTags.size();
		txtCount.setText(String.format(Locale.US, "%d", count));

		ATLog.i(TAG, "INFO. displayCount()");
	}

	@SuppressLint("ShowToast")
	private class ImportThread implements Runnable {

		private String mFilePath;

		protected ImportThread(String filePath) {
			mFilePath = filePath;
		}

		@Override
		public void run() {
			ATLog.i(TAG, "+++ INFO. $ImportThread.run() - [%s]", mFilePath);

			adpTags.clear();
			try {
				File file = new File(mFilePath);
				FileInputStream stream = new FileInputStream(file);
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
				String tag;

				while ((tag = reader.readLine()) != null) {
					adpTags.add(tag);
				}
				adpTags.notifyDataSetChanged();
				txtCount.setText("" + adpTags.getCount());
				reader.close();
				stream.close();
			} catch (Exception e) {
				ATLog.e(TAG, e, "--- ERROR. $ImportThread.run() - Failed to imprt stored tag list [%s]", mFilePath);
				WaitDialog.hide();
				enableActionWidgets(true);
				Toast.makeText(EditStoredTagActivity.this, R.string.failed_import_stored_tag, Toast.LENGTH_LONG).show();
				return;
			}
			WaitDialog.hide();
			enableActionWidgets(true);

			Toast.makeText(EditStoredTagActivity.this, R.string.complete_import_stored_tag, Toast.LENGTH_LONG).show();
			ATLog.i(TAG, "--- INFO. $ImportThread.run() - [%s]", mFilePath);
		}

	}

	@SuppressLint("ShowToast")
	private class ExportThread implements Runnable {

		private String mFilePath;

		protected ExportThread(String filePath) {
			mFilePath = filePath;
		}

		@Override
		public void run() {
			ATLog.i(TAG, "+++ INFO. $ExportThread.run() - [%s]", mFilePath);

			try {
				File file = new File(mFilePath);
				FileOutputStream stream = new FileOutputStream(file);
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, "UTF-8"));
				int count = adpTags.getCount();
				String tag;
				for (int i = 0; i < count; i++) {
					tag = adpTags.getItem(i) + "\r\n";
					writer.write(tag);
				}
				writer.flush();
				writer.close();
				stream.close();
			} catch (Exception e) {
				ATLog.e(TAG, e, "--- ERROR. $ExportThread.run() - Failed to export stored tag list [%s]", mFilePath);
				WaitDialog.hide();
				enableActionWidgets(true);
				Toast.makeText(EditStoredTagActivity.this, R.string.failed_export_stored_tag, Toast.LENGTH_LONG).show();
				return;
			}
			WaitDialog.hide();
			enableActionWidgets(true);
			Toast.makeText(EditStoredTagActivity.this, R.string.complete_export_stored_tag, Toast.LENGTH_LONG).show();
			ATLog.i(TAG, "--- INFO. $ExportThread.run() - [%s]", mFilePath);
		}

	}
}
