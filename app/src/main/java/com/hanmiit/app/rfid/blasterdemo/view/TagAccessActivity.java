package com.hanmiit.app.rfid.blasterdemo.view;

import com.hanmiit.lib.rfid.exception.ATRfidReaderException;
import com.hanmiit.lib.rfid.params.LockParam;
import com.hanmiit.lib.rfid.type.ActionState;
import com.hanmiit.lib.rfid.type.MemoryBank;
import com.hanmiit.lib.rfid.type.ResultCode;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.dialog.IDialogResultListener;
import com.hanmiit.app.rfid.blasterdemo.dialog.PasswordDialog;
import com.hanmiit.app.rfid.blasterdemo.type.MaskType;
import com.hanmiit.app.rfid.blasterdemo.util.ResUtil;
import com.hanmiit.app.rfid.blasterdemo.view.base.AccessActivity;
import com.hanmiit.lib.ATRfidReader;
import com.hanmiit.lib.diagnostics.ATLog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

public class TagAccessActivity extends AccessActivity {

	private static final String TAG = TagAccessActivity.class.getSimpleName();

	private static final int KILL_PASSWORD = 0;
	private static final int ACCESS_PASSWORD = 1;
	private static final int EPC = 2;
	private static final int TID = 3;
	private static final int USER = 4;
	private static final int MAX_ACCESS = 5;

	private static final int SUBACTION_IDLE = 0;
	private static final int SUBACTION_LOCK = 1;
	private static final int SUBACTION_UNLOCK = 2;
	private static final int SUBACTION_PERMALOCK = 3;
	private static final int SUBACTION_KILL = 4;
	private static final int SUBACTION_SET_ACCESS_PWD = 5;
	private static final int SUBACTION_SET_KILL_PWD = 6;

	private CheckBox[] chkAccess;
	private Button btnLock;
	private Button btnUnlock;
	private Button btnPermaLock;
	private Button btnKill;
	private Button btnSetAccessPassword;
	private Button btnSetKillPassword;

	private int mSubAction = SUBACTION_IDLE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_access);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		ATLog.i(TAG, "INFO. onCreate()");

		// Create Reader
		createReader();

		// Initialize Widget
		initWidgets();

		// Initialize Reader
		initReader();

		// Initialize Mask
		initMask();

		resetActionResult();

		setMask(getMask());
		mMaskType = MaskType.SelectionMask;

		// Enable action widget
		enableActionWidgets(true);

		outputMessage(R.string.access_intro_message);
		mSubAction = SUBACTION_IDLE;
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

		ATLog.i(TAG, "INFO. onStart()");
	}

	@Override
	protected void onStop() {
		
		// Rollback Mask
		exitMask();

		ATLog.i(TAG, "INFO. onStart()");
		super.onStop();
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
	public void onClick(View v) {

		ATLog.i(TAG, "INFO. onClick(%s)", ResUtil.getId(v.getId()));

		switch (v.getId()) {
		case R.id.action_lock:
			if (getReader().getAction() == ActionState.Stop) {
				lockTag();
			} else {
				mSubAction = SUBACTION_IDLE;
				stopAction();
			}
			break;
		case R.id.action_unlock:
			if (getReader().getAction() == ActionState.Stop) {
				unlockTag();
			} else {
				mSubAction = SUBACTION_IDLE;
				stopAction();
			}
			break;
		case R.id.action_perma_lock:
			if (getReader().getAction() == ActionState.Stop) {
				permaLockTag();
			} else {
				mSubAction = SUBACTION_IDLE;
				stopAction();
			}
			break;
		case R.id.action_kill:
			if (getReader().getAction() == ActionState.Stop) {
				killTag();
			} else {
				mSubAction = SUBACTION_IDLE;
				stopAction();
			}
			break;
		case R.id.action_access_password:
			if (getReader().getAction() == ActionState.Stop) {
				setAccessPassword();
			} else {
				mSubAction = SUBACTION_IDLE;
				stopAction();
			}
			break;
		case R.id.action_kill_password:
			if (getReader().getAction() == ActionState.Stop) {
				setKillPassword();
			} else {
				mSubAction = SUBACTION_IDLE;
				stopAction();
			}
			break;
		}
		super.onClick(v);
	}

	@Override
	public void onActionChanged(ATRfidReader reader, ActionState action) {
		ATLog.d(TAG, "EVENT. onActionChanged(%s)", action);

		enableActionWidgets(true);

		if (action == ActionState.Stop) {
			mSubAction = SUBACTION_IDLE;
			if (!isActionResult()) {
				outputMessage(R.string.access_intro_message);
			}
		}
	}

	@Override
	public void onAccessResult(ATRfidReader reader, ResultCode code, ActionState action, String epc, String data,
			float rssi, float phase) {
		ATLog.i(TAG, "EVENT. onAccessResult(%s, %s, [%s], [%s], %.2f, %.2f)", code, action, epc == null ? "" : epc,
				data == null ? "" : data, rssi, phase);

		setActionResult();

		if (code != ResultCode.NoError) {
			outputFailMessage(code.toString());
			return;
		}

		outputSelection(epc.length() > 4 ? epc.substring(4) : epc);
		outputSuccessMessage(R.string.access_success);
		setRssi(rssi, phase);
		mSubAction = SUBACTION_IDLE;
	}

	// Lock Tag
	private void lockTag() {
		mSubAction = SUBACTION_LOCK;
		clearWidgets();
		enableActionWidgets(false);
		outputSelection("");
		outputMessage(R.string.access_lock_message);

		resetActionResult();

		LockParam param = new LockParam(chkAccess[KILL_PASSWORD].isChecked(), chkAccess[ACCESS_PASSWORD].isChecked(),
				chkAccess[EPC].isChecked(), chkAccess[TID].isChecked(), chkAccess[USER].isChecked());

		String password = getAccessPassword();
		try {
			getReader().setAccessPassword(password);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, "ERROR. lockTag() - Failed to set password [%s]", password);
		}

		int time = getOperationTime();
		try {
			getReader().setOperationTime(time);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. lockTag() - Failed to set operation time [%d]", time);
		}

		if (getReader().lock(param) != ResultCode.NoError) {
			ATLog.e(TAG, "ERROR. lockTag() - Failed to start lock tag [%s]", param);
			enableActionWidgets(true);
			outputFailMessage(R.string.access_fail_lock_message);
			return;
		}
		ATLog.i(TAG, "INFO. lockTag()");
	}

	// Unlock Tag
	private void unlockTag() {
		mSubAction = SUBACTION_UNLOCK;
		clearWidgets();
		enableActionWidgets(false);
		outputSelection("");
		outputMessage(R.string.access_unlock_message);

		resetActionResult();

		LockParam param = new LockParam(chkAccess[KILL_PASSWORD].isChecked(), chkAccess[ACCESS_PASSWORD].isChecked(),
				chkAccess[EPC].isChecked(), chkAccess[TID].isChecked(), chkAccess[USER].isChecked());

		String password = getAccessPassword();
		try {
			getReader().setAccessPassword(password);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. unlockTag() - Failed to set password [%s]", password);
		}

		int time = getOperationTime();
		try {
			getReader().setOperationTime(time);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. unlockTag() - Failed to set operation time [%d]", time);
		}

		if (getReader().unlock(param) != ResultCode.NoError) {
			ATLog.e(TAG, "ERROR. unlockTag() - Failed to start lock tag [%s]", param);
			enableActionWidgets(true);
			outputFailMessage(R.string.access_fail_unlock_message);
			return;
		}
		ATLog.i(TAG, "INFO. unlockTag()");
	}

	// PermaLock Tag
	private void permaLockTag() {
		mSubAction = SUBACTION_PERMALOCK;
		clearWidgets();
		enableActionWidgets(false);
		outputSelection("");
		outputMessage(R.string.access_perma_lock_message);

		resetActionResult();

		LockParam param = new LockParam(chkAccess[KILL_PASSWORD].isChecked(), chkAccess[ACCESS_PASSWORD].isChecked(),
				chkAccess[EPC].isChecked(), chkAccess[TID].isChecked(), chkAccess[USER].isChecked());

		String password = getAccessPassword();
		try {
			getReader().setAccessPassword(password);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, "ERROR. permaLockTag() - Failed to set password [%s]", password);
		}

		int time = getOperationTime();
		try {
			getReader().setOperationTime(time);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. permaLockTag() - Failed to set operation time [%d]", time);
		}

		if (getReader().permaLock(param) != ResultCode.NoError) {
			ATLog.e(TAG, "ERROR. permaLockTag() - Failed to start lock tag [%s]", param);
			enableActionWidgets(true);
			outputFailMessage(R.string.access_fail_perma_lock_message);
			return;
		}
		ATLog.i(TAG, "INFO. permaLockTag()");
	}

	// Kill Tag
	private void killTag() {

		enableActionWidgets(false);
		final PasswordDialog dlg = new PasswordDialog(this, R.string.kill_password);
		dlg.setResultListener(new IDialogResultListener() {

			@Override
			public void onOkClick(int what, DialogInterface dialog) {
				mSubAction = SUBACTION_KILL;
				String password = dlg.getPassword();

				ATLog.i(TAG, "INFO. killTag().onOkClick() - [%s]", password);

				clearWidgets();
				outputSelection("");
				outputMessage(R.string.access_kill_message);

				resetActionResult();

				int time = getOperationTime();

				try {
					getReader().setOperationTime(time);
				} catch (ATRfidReaderException e) {
					ATLog.e(TAG, e, "ERROR. killTag().onOkClick() - Failed to set operation time [%d]", time);
				}

				if (getReader().kill(password) != ResultCode.NoError) {
					ATLog.e(TAG, "ERROR. killTag().onOkClick() - Failed to start kill tag [%s]", password);
					enableActionWidgets(true);
					outputFailMessage(R.string.access_fail_kill_message);
					return;
				}
			}

			@Override
			public void onCancelClick(int what, DialogInterface dialog) {
				ATLog.i(TAG, "INFO. killTag().onCancelClick()");
				enableActionWidgets(true);
			}

		});
		dlg.show();
		ATLog.i(TAG, "INFO. killTag()");
	}

	// Set Access Password
	private void setAccessPassword() {
		enableActionWidgets(false);
		final PasswordDialog dlg = new PasswordDialog(this, R.string.set_access_password_title);
		dlg.setResultListener(new IDialogResultListener() {

			@Override
			public void onOkClick(int what, DialogInterface dialog) {
				mSubAction = SUBACTION_SET_ACCESS_PWD;
				String password = dlg.getPassword();
				ATLog.i(TAG, "INFO. setAccessPassword().onOkClick() - [%s]", password);

				clearWidgets();
				outputSelection("");
				outputMessage(R.string.access_set_access_password_message);

				resetActionResult();

				String accessPwd = getAccessPassword();
				try {
					getReader().setAccessPassword(accessPwd);
				} catch (ATRfidReaderException e) {
					ATLog.e(TAG, "ERROR. setAccessPassword().onOkClick() - Failed to set password [%s]", accessPwd);
				}

				int time = getOperationTime();

				try {
					getReader().setOperationTime(time);
				} catch (ATRfidReaderException e) {
					ATLog.e(TAG, e, "ERROR. setAccessPassword().onOkClick() - Failed to set operation time [%d]", time);
				}

				if (getReader().writeMemory(MemoryBank.Reserved, 2, password) != ResultCode.NoError) {
					ATLog.e(TAG, "ERROR. setAccessPassword().onOkClick() - Failed to start write access password [%s]",
							password);
					enableActionWidgets(true);
					outputFailMessage(R.string.access_fail_set_access_password_message);
					return;
				}
			}

			@Override
			public void onCancelClick(int what, DialogInterface dialog) {
				ATLog.i(TAG, "INFO. setAccessPassword().onCancelClick()");
				enableActionWidgets(true);
			}

		});
		dlg.show();
		ATLog.i(TAG, "INFO. setAccessPassword()");
	}

	// Set Kill Password
	private void setKillPassword() {
		enableActionWidgets(false);
		final PasswordDialog dlg = new PasswordDialog(this, R.string.set_kill_password_title);
		dlg.setResultListener(new IDialogResultListener() {

			@Override
			public void onOkClick(int what, DialogInterface dialog) {
				mSubAction = SUBACTION_SET_KILL_PWD;
				String password = dlg.getPassword();
				ATLog.i(TAG, "INFO. setKillPassword().onOkClick() - [%s]", password);

				clearWidgets();
				outputSelection("");
				outputMessage(R.string.access_set_kill_password_message);

				resetActionResult();

				String accessPwd = getAccessPassword();
				try {
					getReader().setAccessPassword(accessPwd);
				} catch (ATRfidReaderException e) {
					ATLog.e(TAG, e, "ERROR. setKillPassword() - Failed to set password [%s]", accessPwd);
				}

				int time = getOperationTime();

				try {
					getReader().setOperationTime(time);
				} catch (ATRfidReaderException e) {
					ATLog.e(TAG, e, "ERROR. setKillPassword() - Failed to set operation time [%d]", time);
				}

				if (getReader().writeMemory(MemoryBank.Reserved, 0, password) != ResultCode.NoError) {
					ATLog.e(TAG, "ERROR. setKillPassword() - Failed to start write kill password [%s]", password);
					enableActionWidgets(true);
					outputFailMessage(R.string.access_fail_set_kill_password_message);
					return;
				}
			}

			@Override
			public void onCancelClick(int what, DialogInterface dialog) {
				ATLog.i(TAG, "INFO. setKillPassword().onCancelClick()");
				enableActionWidgets(true);
			}

		});
		dlg.show();
		ATLog.i(TAG, "INFO. setKillPassword()");
	}

	// Initialize Reader
	@Override
	protected void initReader() {
		super.initReader();
		ATLog.i(TAG, "INFO. initReader()");
	}

	// Initialize Widgets
	protected void initWidgets() {
		super.initWidgets();

		int[] ids = new int[] { R.id.kill_password, R.id.access_password, R.id.epc, R.id.tid, R.id.user };
		chkAccess = new CheckBox[MAX_ACCESS];

		for (int i = 0; i < MAX_ACCESS; i++) {
			chkAccess[i] = (CheckBox) findViewById(ids[i]);
		}
		// Lock Button
		btnLock = (Button) findViewById(R.id.action_lock);
		btnLock.setOnClickListener(this);
		// Unlock Button
		btnUnlock = (Button) findViewById(R.id.action_unlock);
		btnUnlock.setOnClickListener(this);
		// PermaLock Button
		btnPermaLock = (Button) findViewById(R.id.action_perma_lock);
		btnPermaLock.setOnClickListener(this);
		// Kill Button
		btnKill = (Button) findViewById(R.id.action_kill);
		btnKill.setOnClickListener(this);
		// Set Access Password Button
		btnSetAccessPassword = (Button) findViewById(R.id.action_access_password);
		btnSetAccessPassword.setOnClickListener(this);
		// Set Kill Password Button
		btnSetKillPassword = (Button) findViewById(R.id.action_kill_password);
		btnSetKillPassword.setOnClickListener(this);

		ATLog.i(TAG, "INFO. initWidgets()");
	}

	private boolean isEnabledWidget(boolean enabled, ActionState state, int subAction) {
		ActionState action = getReader().getAction();
		return enabled ? (action == ActionState.Stop ? enabled
				: (action == state ? (mSubAction == subAction ? enabled : false) : false)) : enabled;
	}

	// Enable/Disable Action Widgets
	protected void enableActionWidgets(boolean enabled) {
		super.enableActionWidgets(enabled);

		for (int i = 0; i < MAX_ACCESS; i++) {
			chkAccess[i].setEnabled(isEnabledWidget(enabled));
		}
		btnLock.setEnabled(isEnabledWidget(enabled, ActionState.Lock, SUBACTION_LOCK));
		btnUnlock.setEnabled(isEnabledWidget(enabled, ActionState.Lock, SUBACTION_UNLOCK));
		btnPermaLock.setEnabled(isEnabledWidget(enabled, ActionState.Lock, SUBACTION_PERMALOCK));
		btnKill.setEnabled(isEnabledWidget(enabled, ActionState.Kill, SUBACTION_KILL));
		btnSetAccessPassword.setEnabled(isEnabledWidget(enabled, ActionState.WriteMemory, SUBACTION_SET_ACCESS_PWD));
		btnSetKillPassword.setEnabled(isEnabledWidget(enabled, ActionState.WriteMemory, SUBACTION_SET_KILL_PWD));
		if (enabled) {
			switch (getReader().getAction()) {
			case Lock:
				switch (mSubAction) {
				case SUBACTION_LOCK:
					btnLock.setText(R.string.action_stop);
					btnUnlock.setText(R.string.action_unlock_tag);
					btnPermaLock.setText(R.string.action_perma_lock_tag);
					btnKill.setText(R.string.action_kill_tag);
					btnSetAccessPassword.setText(R.string.set_access_password);
					btnSetKillPassword.setText(R.string.set_kill_password);
					break;
				case SUBACTION_UNLOCK:
					btnLock.setText(R.string.action_lock_tag);
					btnUnlock.setText(R.string.action_stop);
					btnPermaLock.setText(R.string.action_perma_lock_tag);
					btnKill.setText(R.string.action_kill_tag);
					btnSetAccessPassword.setText(R.string.set_access_password);
					btnSetKillPassword.setText(R.string.set_kill_password);
					break;
				case SUBACTION_PERMALOCK:
					btnLock.setText(R.string.action_lock_tag);
					btnUnlock.setText(R.string.action_unlock_tag);
					btnPermaLock.setText(R.string.action_stop);
					btnKill.setText(R.string.action_kill_tag);
					btnSetAccessPassword.setText(R.string.set_access_password);
					btnSetKillPassword.setText(R.string.set_kill_password);
					break;
				}
				break;
			case Kill:
				if (mSubAction == SUBACTION_KILL) {
					btnLock.setText(R.string.action_lock_tag);
					btnUnlock.setText(R.string.action_unlock_tag);
					btnPermaLock.setText(R.string.action_perma_lock_tag);
					btnKill.setText(R.string.action_stop);
					btnSetAccessPassword.setText(R.string.set_access_password);
					btnSetKillPassword.setText(R.string.set_kill_password);
				}
				break;
			case WriteMemory:
				btnLock.setText(R.string.action_lock_tag);
				btnUnlock.setText(R.string.action_unlock_tag);
				btnPermaLock.setText(R.string.action_perma_lock_tag);
				btnKill.setText(R.string.action_kill_tag);
				switch (mSubAction) {
				case SUBACTION_SET_ACCESS_PWD:
					btnSetAccessPassword.setText(R.string.action_stop);
					btnSetKillPassword.setText(R.string.set_kill_password);
					break;
				case SUBACTION_SET_KILL_PWD:
					btnSetAccessPassword.setText(R.string.set_access_password);
					btnSetKillPassword.setText(R.string.action_stop);
					break;
				}
				break;
			case Stop:
				btnLock.setText(R.string.action_lock_tag);
				btnUnlock.setText(R.string.action_unlock_tag);
				btnPermaLock.setText(R.string.action_perma_lock_tag);
				btnKill.setText(R.string.action_kill_tag);
				btnSetAccessPassword.setText(R.string.set_access_password);
				btnSetKillPassword.setText(R.string.set_kill_password);
				break;
			case BlockErase:
				break;
			case BlockWrite:
				break;
			case Inventory:
				break;
			case PermaLock:
				break;
			case ReadMemory:
				break;
			case Unlock:
				break;
			default:
				break;
			}
		}
		ATLog.i(TAG, "INFO. enableActionWidgets(%s)", enabled);
	}
}
