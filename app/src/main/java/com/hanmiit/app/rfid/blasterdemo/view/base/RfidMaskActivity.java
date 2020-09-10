package com.hanmiit.app.rfid.blasterdemo.view.base;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.hanmiit.lib.diagnostics.ATLog;
import com.hanmiit.lib.rfid.exception.ATRfidReaderException;
import com.hanmiit.lib.rfid.params.SelectMaskParam;
import com.hanmiit.lib.rfid.type.MaskActionType;
import com.hanmiit.lib.rfid.type.MaskTargetType;
import com.hanmiit.lib.rfid.type.MemoryBank;
import com.hanmiit.lib.rfid.type.SelectFlag;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.type.MaskType;
import com.hanmiit.app.rfid.blasterdemo.util.ResUtil;
import com.hanmiit.app.rfid.blasterdemo.view.sub.EpcMaskActivity;
import com.hanmiit.app.rfid.blasterdemo.view.sub.SelectMaskActivity;

public abstract class RfidMaskActivity extends RfidPowerGainActivity implements OnClickListener {

	private static final String TAG = RfidMaskActivity.class.getSimpleName();

	public static final String KEY_MASK_TYPE = "mask_type";
	public static final String KEY_MASK = "mask_value";
	protected static final int DEFAULT_MASK_TYPE = 1;
	protected static final String DEFAULT_MASK = "";

	private static final int EPC_OFFSET = 32;
	private static final int MAX_MASK = 8;

	private Button btnMask;

	protected MaskType mMaskType;
	private String mMaskValue;

	private SelectMaskParam[] mBackupMasks;
	private SelectFlag mBackupUseMask;

	public RfidMaskActivity() {
		mMaskValue = "";
	}

	@Override
	public void onClick(View v) {
		ATLog.i(TAG, "INFO. onClick(%s)", ResUtil.getId(v.getId()));

		switch (v.getId()) {
		case R.id.mask:
			Intent intent = null;
			switch (mMaskType) {
			case SelectionMask:
				intent = new Intent(this, SelectMaskActivity.class);
				break;
			case EPCMask:
				intent = new Intent(this, EpcMaskActivity.class);
				break;
			default:
				return;
			}
			startActivityForResult(intent, VIEW_MASK);
			break;
		}
		super.onClick(v);
	}

	@Override
	protected void initWidgets() {
		super.initWidgets();

		btnMask = (Button) findViewById(R.id.mask);
		btnMask.setOnClickListener(this);

		ATLog.i(TAG, "INFO. initWidgets()");
	}

	@Override
	protected void enableActionWidgets(boolean enabled) {
		super.enableActionWidgets(enabled);

		btnMask.setEnabled(isEnabledWidget(enabled) && mMaskValue.equals(""));

		ATLog.i(TAG, "INFO. enableActionWidgets(%s)", enabled);
	}

	protected void initMask() {
		Intent intent = getIntent();
		int code = intent.getIntExtra(KEY_MASK_TYPE, DEFAULT_MASK_TYPE);
		mMaskType = MaskType.valueOf(code);
		if ((mMaskValue = intent.getStringExtra(KEY_MASK)) == null)
			mMaskValue = "";

//		// Clear EPC Mask
//		try {
//			getReader().clearEpcMask();
//		} catch (ATRfidReaderException e) {
//			ATLog.e(TAG, e, "ERROR. initMask() - Failed to clear EPC masks");
//		}
//		// Clear Selection Mask
//		try {
//			getReader().setUseSelectionMask(SelectFlag.NotUsed);
//		} catch (ATRfidReaderException e) {
//			ATLog.e(TAG, e, "ERROR. initMask() - Failed to not use selection mask");
//		}

		if (mMaskValue.equals(""))
			return;

		if (mMaskType == MaskType.SelectionMask) {
			try {
				mBackupUseMask = getReader().getUseSelectionMask();
			} catch (ATRfidReaderException e) {
				mBackupUseMask = SelectFlag.NotUsed;
			}
			if (mBackupUseMask != SelectFlag.NotUsed) {
				mBackupMasks = new SelectMaskParam[MAX_MASK];
				for (int i = 0; i < MAX_MASK; i++) {
					try {
						if (!getReader().usedSelectionMask(i))
							break;
						mBackupMasks[i] = getReader().getSelectionMask(i);
					} catch (ATRfidReaderException e) {
						mBackupMasks[i] = null;
					}
				}
			}
		} else {
			mBackupMasks = null;
			mBackupUseMask = SelectFlag.NotUsed;
		}

		try {
			getReader().setSelectionMask(0,
					new SelectMaskParam(MaskTargetType.SL, MaskActionType.AB, MemoryBank.EPC, EPC_OFFSET, mMaskValue));
			getReader().setUseSelectionMask(SelectFlag.SL);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. initMask() - Failed to set selection mask [%s]", mMaskValue);
		}

		ATLog.i(TAG, "INFO. initMask() - [%s]", mMaskValue);
	}

	protected void exitMask() {

		if (mMaskValue.equals(""))
			return;

		if (mMaskType != MaskType.SelectionMask)
			return;

		try {
			getReader().setUseSelectionMask(mBackupUseMask);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. exitMask() - Failed to rollback use selection mask [%s]", mBackupUseMask);
		}
		if (mBackupUseMask != SelectFlag.NotUsed && mBackupMasks != null) {
			for (int i = 0; i < MAX_MASK; i++) {
				if (mBackupMasks[i] == null)
					break;
				try {
					getReader().setSelectionMask(i, mBackupMasks[i]);
				} catch (ATRfidReaderException e) {
					ATLog.e(TAG, e, "ERROR. exitMask() - Failed to rollback selection mask [%s]", mBackupMasks[i]);
				}
			}
		}
		
		ATLog.i(TAG, "INFO. exitMask()");
	}

	protected MaskType getMaskType() {
		ATLog.i(TAG, "INFO. getMaskType() - [%s]", mMaskType);
		return mMaskType;
	}

	protected String getMask() {
		ATLog.i(TAG, "INFO. getMask() - [%s]", mMaskValue);
		return mMaskValue;
	}
}
