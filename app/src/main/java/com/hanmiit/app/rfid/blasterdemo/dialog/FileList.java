package com.hanmiit.app.rfid.blasterdemo.dialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import com.hanmiit.lib.diagnostics.ATLog;

import android.content.Context;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class FileList extends ListView {

	private static final String TAG = FileList.class.getSimpleName();

	private Context mContext;
	private ArrayList<String> mList;
	private ArrayList<String> mFolderList;
	private ArrayList<String> mFileList;
	private ArrayAdapter<String> mAdapter;

	private File mPath;

	private OnPathChangedListener mListenerPathChanged;
	private OnFileSelectedListener mListenerFileSelected;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public FileList(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init(context);
	}

	public FileList(Context context, AttributeSet attrs) {
		super(context, attrs);

		init(context);
	}

	public FileList(Context context) {
		super(context);

		init(context);
	}

	private void init(Context context) {
		mContext = context;
		setOnItemClickListener(mListenerItemClick);

		mList = new ArrayList<String>();
		mFolderList = new ArrayList<String>();
		mFileList = new ArrayList<String>();
		mAdapter = null;

		mPath = new File("/");

		mListenerFileSelected = null;
		mListenerPathChanged = null;
		
		ATLog.i(TAG, "INFO. init()");
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	// Set Path
	public void setPath(String newPath) {
		File path = null;
		if (newPath.length() == 0) {
			path = new File("/");
		} else {
			path = new File(newPath);
		}

		if (openPath(path)) {
			mPath = path;
			updateAdapter();
			if (mListenerPathChanged != null)
				mListenerPathChanged.onChanged(newPath);
		} else {
			Toast.makeText(mContext, String.format(Locale.US, "Failed to open path [%s]", newPath),
					Toast.LENGTH_LONG).show();
		}
		ATLog.i(TAG, "INFO. setPath([%s])", newPath);
	}

	// Get Path
	public String getPath() {
		String path = mPath.getAbsolutePath();
		ATLog.i(TAG, "INFO. getPath() - [%s]", path);
		return path;
	}

	// Set Path Changed Listener
	public void setOnPathChangedListener(OnPathChangedListener listener) {
		mListenerPathChanged = listener;
		ATLog.i(TAG, "INFO. setOnPathChangedListener()");
	}

	// Get Path Changed Listener
	public OnPathChangedListener getOnPathChangedListener() {
		ATLog.i(TAG, "INFO. getOnPathChangedListener()");
		return mListenerPathChanged;
	}

	// Set File Selected Listener
	public void setOnFileSelectedListener(OnFileSelectedListener listener) {
		mListenerFileSelected = listener;
		ATLog.i(TAG, "INFO. setOnFileSelectedListener()");
	}

	// Get File Selected Listener
	public OnFileSelectedListener getFileSelectedListener() {
		ATLog.i(TAG, "INFO. getFileSelectedListener()");
		return mListenerFileSelected;
	}

	// ------------------------------------------------------------------------
	// Internal Helper Methods
	// ------------------------------------------------------------------------

	private boolean openPath(File path) {

		mFolderList.clear();
		mFileList.clear();

		File[] files = path.listFiles();

		if (files == null) {
			ATLog.e(TAG, "ERROR. openPath([%s]) - Failed to open path", path);
			return false;
		}

		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				mFolderList.add("<" + files[i].getName() + ">");
			} else {
				mFileList.add(files[i].getName());
			}
		}

		Collections.sort(mFolderList);
		Collections.sort(mFileList);
		mFolderList.add(0, "<.>");
		
		ATLog.i(TAG, "INFO. openPath([%s])", path);
		return true;
	}

	private void updateAdapter() {
		mList.clear();
		mList.addAll(mFolderList);
		mList.addAll(mFileList);

		mAdapter = new ArrayAdapter<String>(mContext,
				android.R.layout.simple_list_item_1, mList);
		setAdapter(mAdapter);
		ATLog.i(TAG, "INFO. updateAdapter()");
	}

	private String getRealPathName(String newPath) {
		newPath = newPath.substring(1, newPath.length() - 1);
		String res = "";
		
		if (newPath.equals(".")) {
			res = mPath.getParent();
		} else {
			File path = new File(mPath.getAbsoluteFile() + File.separator + newPath + File.separator);
			res = path.getAbsolutePath();
		}
		ATLog.i(TAG, "INFO. getRealPathName([%s])", newPath);
		return res;
	}

	private OnItemClickListener mListenerItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			String path = "";
			ATLog.i(TAG, "INFO. $mListenerItemClick.onItemClick(%d, %d)", position, id);

			String fileName = getItemAtPosition(position).toString();
			if (fileName.matches("<.*>")) {
				setPath(getRealPathName(fileName));
			} else {
				if (mListenerFileSelected != null)
					path = mPath.getAbsolutePath();
					mListenerFileSelected.onSelected(path, fileName);
			}
		}
	};
}
