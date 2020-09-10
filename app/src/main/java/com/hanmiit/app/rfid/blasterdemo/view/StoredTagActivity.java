package com.hanmiit.app.rfid.blasterdemo.view;

import java.util.ArrayList;
import java.util.Locale;

import com.hanmiit.lib.rfid.exception.ATRfidReaderException;
import com.hanmiit.lib.rfid.type.ActionState;
import com.hanmiit.lib.rfid.type.CommandType;
import com.hanmiit.lib.rfid.type.ResultCode;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.data.SharedData;
import com.hanmiit.app.rfid.blasterdemo.dialog.WaitDialog;
import com.hanmiit.app.rfid.blasterdemo.util.ResUtil;
import com.hanmiit.app.rfid.blasterdemo.view.base.RfidClearActivity;
import com.hanmiit.app.rfid.blasterdemo.view.sub.EditStoredTagActivity;
import com.hanmiit.lib.ATRfidReader;
import com.hanmiit.lib.diagnostics.ATLog;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class StoredTagActivity extends RfidClearActivity {

	private static final String TAG = StoredTagActivity.class.getSimpleName();

	private static final int VIEW_EDIT_STORED_TAG = 2000;

	private ListView lstTags;
	private TextView txtStoredTagCount;
	private Button btnLoad;
	private Button btnSave;
	private Button btnEdit;
	private TextView txtCount;

	private ArrayAdapter<String> adpTags;
	private ArrayList<String> aryTags;
	private long lLastUpdateTime;
	
	private int mMaxCount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stored_tag);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		mMaxCount = 0;
		
		ATLog.i(TAG, "INFO. onCreate()");

		// Create Reader
		createReader();

		// Initialize Widget
		initWidgets();

		// Initialize Reader
		initReader();

		lLastUpdateTime = 0;

		// Enable action widget
		enableActionWidgets(true);
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

		// Set Key Action
		try {
			getReader().setUseKeyAction(false);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. onStart() - Failed to set use key action");
		}
		
		ATLog.i(TAG, "INFO. onStart()");
	}

	@Override
	protected void onStop() {
		
		// Set Key Action
		getReader().stop();// 2016-06-17 mkj

		ATLog.i(TAG, "INFO. onStop()");
		super.onStop();
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		// Set Key Action
		try {
			getReader().setUseKeyAction(false);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. onResume() - Failed to set use key action");
		}
		
		ATLog.i(TAG, "INFO. onResume()");
	}

	@Override
	protected void onPause() {
		// Set Key Action
		try {
			getReader().setUseKeyAction(false);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. onPause() - Failed to set use key action");
		}

		ATLog.i(TAG, "INFO. onPause()");
		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		ATLog.i(TAG, "INFO. onActivityResult(%s, %s)", getRequestCode(requestCode), getResultCode(resultCode));

		if (requestCode == VIEW_EDIT_STORED_TAG) {
			switch (resultCode) {
			case RESULT_DISCONNECTED:
				setResult(RESULT_DISCONNECTED);
				finish();
				break;
			case RESULT_OK:
				adpTags.notifyDataSetChanged();
				txtCount.setText("" + adpTags.getCount());
				break;
			default:
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {

		ATLog.i(TAG, "INFO. onClick(%s)", ResUtil.getId(v.getId()));

		ResultCode res;

		switch (v.getId()) {
		case R.id.load:
			clearTagList();
			WaitDialog.showProgess(this, R.string.load_stored_tag_message);
			WaitDialog.setMax(mMaxCount);
			lLastUpdateTime = System.currentTimeMillis();
			enableActionWidgets(false);
			if ((res = getReader().loadStoredTag()) != ResultCode.NoError) {
				ATLog.e(TAG, "ERROR. onClick(%s) - Failed to load stored tag [%s]", ResUtil.getId(v.getId()), res);
			}
			break;
		case R.id.save:
			SaveStoredTagTask task = new SaveStoredTagTask();
			task.execute();
			break;
		case R.id.edit:
			Intent intent = new Intent(this, EditStoredTagActivity.class);
			SharedData.setTagList(aryTags);
			startActivityForResult(intent, VIEW_EDIT_STORED_TAG);
			break;
		}
		super.onClick(v);
	}

	@Override
	public void onActionChanged(ATRfidReader reader, ActionState action) {
		ATLog.i(TAG, "EVENT. onActionChanged(%s)", action);

		enableActionWidgets(action == ActionState.Stop);
		if(action == ActionState.Stop) WaitDialog.hide();  //2016-06-17 mkj clear�� ��ȭ�� ���ų� �Ҷ� waitDialog hide.
	}

	@Override
	public void onLoadTag(ATRfidReader reader, String tag) {
		ATLog.i(TAG, "EVENT. onLoadTag([%s])", tag);

		adpTags.add(tag);
		int count = adpTags.getCount();
		txtCount.setText(String.format(Locale.US, "%d", count));
		WaitDialog.setProgress(count);
		long lTime = System.currentTimeMillis();
		if (lTime - lLastUpdateTime > 500) {
			adpTags.notifyDataSetChanged();
			ATLog.d(TAG, "DEBUG. onLoadTag([%s]) - [%d]", tag, adpTags.getCount());
			lLastUpdateTime = lTime;
		}
	}

	@Override
	public void onCommandComplete(ATRfidReader reader, CommandType command) {

		ATLog.d(TAG, "EVENT. onCommandComplete(%s)", command);

		switch (command) {
		case LoadStoredTag:
		case RemoveAllStoredTag:
			adpTags.notifyDataSetChanged();
			WaitDialog.hide();
			enableActionWidgets(true);
			displayStoredTagCount();
			txtCount.setText("" + adpTags.getCount());
			break;
		case SaveStoredTag:
			synchronized (getReader()) {
				getReader().notifyAll();
			}
			break;
		default:
			break;
		}
	}

	// Display Stored Tag Count
	private void displayStoredTagCount() {
		try {
			mMaxCount = getReader().getStoredTagCount();
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. displayStoredTagCount() - Failed to get stored tag count");
			mMaxCount = 0;
		}
		txtStoredTagCount.setText(String.format(Locale.US, "%d", mMaxCount));
		ATLog.i(TAG, "INFO. displayStoredTagCount()");
	}

	private void clearTagList() {
		adpTags.clear();
		adpTags.notifyDataSetChanged();
		txtCount.setText("" + adpTags.getCount());
		ATLog.i(TAG, "INFO. clearTagList()");
	}

	private class SaveStoredTagTask extends AsyncTask<Void, Void, Void> {

		private int mMaxCount = 0;
		
		@Override
		protected void onPreExecute() {
			
			mMaxCount = adpTags.getCount();
			WaitDialog.showProgess(StoredTagActivity.this, R.string.save_stored_tag_message);
			WaitDialog.setMax(mMaxCount);

			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			for (int i = 0; i < mMaxCount; i++) {
				String tag = adpTags.getItem(i);
				getReader().saveStoredTag(tag);
				WaitDialog.setProgress(i);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			
			displayStoredTagCount();
			WaitDialog.hide();
			enableActionWidgets(true);

			AlertDialog.Builder builder = new AlertDialog.Builder(StoredTagActivity.this);
			builder.setMessage(R.string.complete_save_stored_tag);
			builder.setPositiveButton(R.string.action_ok, null);
			builder.setCancelable(true);
			builder.show();
			
			super.onPostExecute(result);
		}
		
	}

	// Initialize Reader
	@Override
	protected void initReader() {
		ATLog.i(TAG, "INFO. initReader()");
	}

	// Initialize Widgets
	@Override
	protected void initWidgets() {
		super.initWidgets();

		lstTags = (ListView) findViewById(R.id.tag_list);
		aryTags = new ArrayList<String>();
		adpTags = new ArrayAdapter<String>(this, R.layout.item_tag_select_list, aryTags);
		lstTags.setAdapter(adpTags);
		lstTags.setChoiceMode(ListView.CHOICE_MODE_NONE);

		txtStoredTagCount = (TextView) findViewById(R.id.stored_tag_count);
		displayStoredTagCount();

		txtCount = (TextView) findViewById(R.id.tag_count);
		txtCount.setText("" + adpTags.getCount());

		btnLoad = (Button) findViewById(R.id.load);
		btnLoad.setOnClickListener(this);

		btnSave = (Button) findViewById(R.id.save);
		btnSave.setOnClickListener(this);

		btnEdit = (Button) findViewById(R.id.edit);
		btnEdit.setOnClickListener(this);
		
		ATLog.i(TAG, "INFO. initWidgets()");
	}

	// Enable/Disable Action Widgets
	@Override
	protected void enableActionWidgets(boolean enabled) {
		super.enableActionWidgets(enabled);

		lstTags.setEnabled(isEnabledWidget(enabled));
		btnLoad.setEnabled(isEnabledWidget(enabled));
		btnSave.setEnabled(isEnabledWidget(enabled));
		btnEdit.setEnabled(isEnabledWidget(enabled));
		
		ATLog.i(TAG, "INFO. enableActionWidgets()");
	}

	@Override
	protected void clearWidgets() {
		ResultCode res;
		clearTagList();

		enableActionWidgets(false);

		WaitDialog.show(this, R.string.remove_all_stored_tag_message);
		if ((res = getReader().removeAllStoredTags()) != ResultCode.NoError) {
			ATLog.e(TAG, "ERROR. Failed to remove all tag [%s]", res);
			WaitDialog.hide();
			enableActionWidgets(true);
		}
		ATLog.i(TAG, "INFO. clearWidgets()");
	}
}
