package com.hanmiit.app.rfid.blasterdemo.dialog;

import com.hanmiit.lib.diagnostics.ATLog;
import com.hanmiit.app.rfid.blasterdemo.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SaveFileDialog implements OnPathChangedListener, OnFileSelectedListener {

	private static final String TAG = SaveFileDialog.class.getSimpleName();
	
	private Builder mDialog;
	private OnFileSelectedListener mListenerFileSelected;
	private OnNotifyEventListener mListenerCancel;

	private TextView txtPath;
	private EditText edtFileName;
	private FileList lstFiles;
	
	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public SaveFileDialog(Context context) {
		String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();

		// Initialize Dialog Layout
		LinearLayout layoutRoot = (LinearLayout) LinearLayout.inflate(context,
				R.layout.dialog_save_file, null);
		txtPath = (TextView)layoutRoot.findViewById(R.id.path);
		edtFileName = (EditText)layoutRoot.findViewById(R.id.file_name);
		lstFiles = (FileList)layoutRoot.findViewById(R.id.file_list);
		lstFiles.setPath(rootPath);
		lstFiles.setOnPathChangedListener(this);
		lstFiles.setOnFileSelectedListener(this);
		
		mDialog = new Builder(context);
		mDialog.setTitle(R.string.open_file_dialog);
		mDialog.setView(layoutRoot);
		mDialog.setPositiveButton(R.string.action_ok, mPositiveClicked);
		mDialog.setNegativeButton(R.string.action_cancel, mNegativeClicked);
		mDialog.setCancelable(true);
		mDialog.setOnCancelListener(mCanceled);
		
		ATLog.i(TAG, "INFO. $Constructor()");
	}
	
	public Dialog create() {
		ATLog.i(TAG, "INFO. create()");
		return mDialog.create();
	}
	
	public void show() {
		mDialog.show();
		ATLog.i(TAG, "INFO. show()");
	}
	
	public void setOnFileSelectedListener(OnFileSelectedListener listener) {
		mListenerFileSelected = listener;
		ATLog.i(TAG, "INFO. setOnFileSelectedListener()");
	}
	
	public void setOnCancelListener(OnNotifyEventListener listener) {
		mListenerCancel = listener;
		ATLog.i(TAG, "INFO. setOnCancelListener()");
	}
	
	private DialogInterface.OnClickListener mPositiveClicked = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			ATLog.i(TAG, "INFO. $mPositiveClicked.onClick()");
			if (mListenerFileSelected != null) {
				mListenerFileSelected.onSelected(lstFiles.getPath(), getFileName());
			}
		}
	};
	
	private DialogInterface.OnClickListener mNegativeClicked = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			ATLog.i(TAG, "INFO. $mNegativeClicked.onClick()");
			if (mListenerCancel != null) {
				mListenerCancel.onNotify(SaveFileDialog.this);
			}
		}
	};
	
	private DialogInterface.OnCancelListener mCanceled = new DialogInterface.OnCancelListener() {
		
		@Override
		public void onCancel(DialogInterface dialog) {
			ATLog.i(TAG, "INFO. $mCanceled.onCancel()");
			if (mListenerCancel != null) {
				mListenerCancel.onNotify(SaveFileDialog.this);
			}
		}
	};
	
	private String getPath() {
		String path = txtPath.getText().toString();
		ATLog.i(TAG, "INFO. getPath() - [%s]", path);
		return path;
	}
	
	private String getFileName() {
		String fileName = edtFileName.getText().toString();
		ATLog.i(TAG, "INFO. getFileName() - [%s]", fileName);
		return fileName;
	}
	
	public void setFileName(String fileName) {
		edtFileName.setText(fileName);
		ATLog.i(TAG, "INFO. setFileName([%s])", fileName);
	}

	@Override
	public void onSelected(String path, String fileName) {
		edtFileName.setText(fileName);
		ATLog.i(TAG, "INFO. onSelected([%s], [%s])", path, fileName);
	}

	@Override
	public void onChanged(String path) {
		txtPath.setText(path);
		ATLog.i(TAG, "INFO. onChanged([%s])", path);
	}
}
