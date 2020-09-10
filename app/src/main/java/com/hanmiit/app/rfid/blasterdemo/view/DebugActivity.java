package com.hanmiit.app.rfid.blasterdemo.view;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.hanmiit.lib.device.type.ConnectionState;
import com.hanmiit.lib.diagnostics.ATLog;
import com.hanmiit.lib.rfid.exception.ATRfidReaderException;
import com.hanmiit.lib.rfid.type.CommandType;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.dialog.OnFileSelectedListener;
import com.hanmiit.app.rfid.blasterdemo.dialog.OnNotifyEventListener;
import com.hanmiit.app.rfid.blasterdemo.dialog.SaveFileDialog;
import com.hanmiit.app.rfid.blasterdemo.dialog.WaitDialog;
import com.hanmiit.app.rfid.blasterdemo.view.base.RfidActivity;
import com.hanmiit.lib.ATRfidReader;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class DebugActivity extends RfidActivity
		implements OnClickListener, OnFileSelectedListener, OnNotifyEventListener {

	private static final String TAG = DebugActivity.class.getSimpleName();

	private static final int SAVE_FILE_DIALOG = 1000;

	private ScrollView viewDebug;
	private TextView txtMsg;
	private Button btnDebugModeOn;
	private Button btnDebugModeOff;
	private Button btnLoad;
	private Button btnSave;

	private String debugMsg;
	private boolean mIsSavable = false;
	private SaveFileDialog dlgFileSave;
	private String saveFileName = "";
	private SaveDebugThread mThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		ATLog.i(TAG, "INFO. onCreate()");

		// Create Reader
		this.createReader();

		// Initialize Widget
		this.initWidgets();

		// Initialize Reader
		this.initReader();

		this.debugMsg = "";

		// Enable action widget
		this.enableActionWidgets(true);
	}

	@Override
	protected void onDestroy() {

		ATLog.i(TAG, "INFO. onDestroy()");

		// Destroy Reader
		this.destroyReader();

		super.onDestroy();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case SAVE_FILE_DIALOG:
			this.dlgFileSave = new SaveFileDialog(this);
			this.dlgFileSave.setOnFileSelectedListener(this);
			this.dlgFileSave.setOnCancelListener(this);
			return this.dlgFileSave.create();
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case SAVE_FILE_DIALOG:
			this.dlgFileSave.setFileName(this.saveFileName);
			break;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		enableActionWidgets(false);

		switch (v.getId()) {
		case R.id.debug_mode_on:
			try {
				this.getReader().setDebugMode(true);
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. Failed to set debug mode enabled");
			}
			enableActionWidgets(true);
			break;
		case R.id.debug_mode_off:
			try {
				this.getReader().setDebugMode(false);
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. Failed to set debug mode enabled");
			}
			enableActionWidgets(true);
			break;
		case R.id.load_debug:
			this.debugMsg = "";
			WaitDialog.show(this, R.string.load_debug_message);
			this.getReader().loadDebugMessage();
			break;
		case R.id.save_debug:
			showDialog(SAVE_FILE_DIALOG);
			break;
		}
	}

	@Override
	public void onStateChanged(ATRfidReader reader, ConnectionState state) {
		ATLog.i(TAG, "EVENT. onStateChanged (%s)", state);

		if (state == ConnectionState.Disconnected) {
			this.setResult(RESULT_DISCONNECTED);
			this.finish();
		}
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	public void onCommandComplete(ATRfidReader reader, CommandType command) {
		ATLog.i(TAG, "EVENT. onCommandComplete (%s)", command);

		switch (command) {
		case Debug:
			this.txtMsg.setText(this.debugMsg);
			this.debugMsg = "";
			this.viewDebug.post(new Runnable() {

				@Override
				public void run() {
					viewDebug.fullScroll(ScrollView.FOCUS_DOWN);
					WaitDialog.hide();
					mIsSavable = true;
					long now = System.currentTimeMillis();
					Date date = new Date(now);
					SimpleDateFormat sdfNow = new SimpleDateFormat("yyyyMMdd_HHmmss");
					saveFileName = String.format(Locale.US, "DEBUG_%s.txt", sdfNow.format(date));
					enableActionWidgets(true);
				}

			});
			break;
		default:
			break;
		}
	}

	@Override
	public void onDebugMessage(ATRfidReader reader, String msg) {
		this.debugMsg += msg + "\r\n";
	}

	@Override
	public void onNotify(Object sender) {
		enableActionWidgets(true);
	}

	@Override
	public void onSelected(String path, String fileName) {
		WaitDialog.show(this, R.string.save_debug_message);
		this.mThread = new SaveDebugThread(path + fileName);
		this.txtMsg.post(this.mThread);
	}

	private class SaveDebugThread implements Runnable {

		private String filePath;

		protected SaveDebugThread(String filePath) {
			this.filePath = filePath;
		}

		@Override
		public void run() {
			ATLog.i(TAG, "INFO. Begin Save Debug Message");

			String msg = txtMsg.getText().toString();
			try {
				File saveFile = new File(this.filePath);
				FileOutputStream stream = new FileOutputStream(saveFile);
				stream.write(msg.getBytes());
				stream.close();
			} catch (Exception e) {
				ATLog.e(TAG, e, "ERROR. Failed to save debug message");
				WaitDialog.hide();
				enableActionWidgets(true);
				return;
			}
			ATLog.i(TAG, "INFO. End Save Debug Message");
			WaitDialog.hide();
			enableActionWidgets(true);
		}

	}

	// Initialize Reader
	@Override
	protected void initReader() {

	}

	// Initialize Widgets
	@Override
	protected void initWidgets() {
		this.viewDebug = (ScrollView) findViewById(R.id.debug_view);
		this.txtMsg = (TextView) findViewById(R.id.debug_message);
		this.txtMsg.setVerticalScrollBarEnabled(true);
		this.txtMsg.setMovementMethod(new ScrollingMovementMethod());
		this.btnDebugModeOn = (Button) findViewById(R.id.debug_mode_on);
		this.btnDebugModeOn.setOnClickListener(this);
		this.btnDebugModeOff = (Button) findViewById(R.id.debug_mode_off);
		this.btnDebugModeOff.setOnClickListener(this);
		this.btnLoad = (Button) findViewById(R.id.load_debug);
		this.btnLoad.setOnClickListener(this);
		this.btnSave = (Button) findViewById(R.id.save_debug);
		this.btnSave.setOnClickListener(this);
	}

	// Enable/Disable Action Widgets
	@Override
	protected void enableActionWidgets(boolean enabled) {
		this.btnDebugModeOn.setEnabled(enabled);
		this.btnDebugModeOff.setEnabled(enabled);
		this.btnLoad.setEnabled(enabled);
		this.btnSave.setEnabled(enabled && mIsSavable);
	}
}
