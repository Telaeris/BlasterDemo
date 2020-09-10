package com.hanmiit.app.rfid.blasterdemo.view.base;

import java.util.Locale;

import com.hanmiit.lib.diagnostics.ATLog;
import com.hanmiit.lib.rfid.type.MemoryBank;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.adapter.SpinnerAdapter;
import com.hanmiit.app.rfid.blasterdemo.filter.InputFilterMinMax;
import com.hanmiit.app.rfid.blasterdemo.util.ResUtil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MemoryActivity extends AccessActivity {

	private static final String TAG = MemoryActivity.class.getSimpleName();

	protected static final int MAX_MEM_OFFSET = 16;
	protected static final int DEFAULT_MEM_OFFSET = 2;

	private TextView txtBank;
	private TextView txtOffset;

	private MemoryBank mBank;
	private int mOffset;

	@Override
	protected void initWidgets() {
		super.initWidgets();

		// Bank Spinner
		txtBank = (TextView) findViewById(R.id.bank);
		txtBank.setOnClickListener(this);

		// Offset Spinner
		txtOffset = (TextView) findViewById(R.id.offset);
		txtOffset.setOnClickListener(this);

		setMemBank(MemoryBank.EPC);
		setMemOffset(DEFAULT_MEM_OFFSET);

		ATLog.i(TAG, "INFO. initWidgets()");
	}

	@Override
	public void onClick(View v) {
		ATLog.i(TAG, "INFO. onClick(%s)", ResUtil.getId(v.getId()));

		switch (v.getId()) {
		case R.id.bank:
			showMemBankDialog();
			break;
		case R.id.offset:
			showMemOffsetDialog();
			break;
		}
		super.onClick(v);
	}

	@Override
	protected void clearWidgets() {
		super.clearWidgets();

		ATLog.i(TAG, "INFO. clearWidgets()");
	}

	protected void setMemBank(MemoryBank bank) {
		mBank = bank;
		txtBank.setText(mBank.toString());
		ATLog.i(TAG, "INFO. setMemBank(%s)", bank);
	}

	protected MemoryBank getMemBank() {
		ATLog.i(TAG, "INFO. getMemBank() - [%s]", mBank);
		return mBank;
	}

	protected void setMemOffset(int offset) {
		mOffset = offset;
		txtOffset.setText(String.format(Locale.US, "%d WORD", offset));
		ATLog.i(TAG, "INFO. setMemOffset(%d)", offset);
	}

	protected int getMemOffset() {
		ATLog.i(TAG, "INFO. getMemOffset() - [%d]", mOffset);
		return mOffset;
	}

	private void showMemBankDialog() {

		if (!mIsEnabled)
			return;

		LinearLayout root = (LinearLayout) LinearLayout.inflate(this, R.layout.dialog_list_view, null);
		final ListView bank = (ListView) root.findViewById(R.id.list);
		final SpinnerAdapter adapter = new SpinnerAdapter(this, android.R.layout.simple_list_item_single_choice);
		for (MemoryBank item : MemoryBank.values()) {
			adapter.addItem(item.getValue(), item.toString());
		}
		bank.setAdapter(adapter);
		bank.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.bank);
		builder.setView(root);
		builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int position = bank.getCheckedItemPosition();
				MemoryBank value = MemoryBank.valueOf(adapter.getValue(position));
				setMemBank(value);
				ATLog.i(TAG, "INFO. showMemBankDialog().$PositiveButton.onClick()");
			}
		});
		builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ATLog.i(TAG, "INFO. showMemBankDialog().$NegativeButton.onClick()");
			}
		});
		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				ATLog.i(TAG, "INFO. showMemBankDialog().onCancel()");
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				int position = adapter.getPosition(mBank.getValue());
				bank.setItemChecked(position, true);
				bank.setSelectionFromTop(position, 0);
				ATLog.i(TAG, "INFO. showMemBankDialog().onShow()");
			}
		});
		dialog.show();
		
		ATLog.i(TAG, "INFO. showMemBankDialog()");
	}

	private void showMemOffsetDialog() {

		if (!mIsEnabled)
			return;

		LinearLayout root = (LinearLayout) LinearLayout.inflate(this, R.layout.dialog_input_word, null);
		final EditText offset = (EditText) root.findViewById(R.id.value);
		offset.setFilters(new InputFilter[] { new InputFilterMinMax(0, Integer.MAX_VALUE) });
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.offset);
		builder.setView(root);
		builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int value = 0;
				try {
					value = Integer.parseInt(offset.getText().toString());
				} catch (Exception e) {
					value = DEFAULT_MEM_OFFSET;
				}
				setMemOffset(value);
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(offset.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showMemOffsetDialog().$PositiveButton.onClick()");
			}
		});
		builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(offset.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showMemOffsetDialog().$NegativeButton.onClick()");
			}
		});
		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(offset.getWindowToken(), 0);
				ATLog.i(TAG, "INFO. showMemOffsetDialog().onCancel()");
			}
		});
		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				offset.setText(String.format(Locale.US, "%d", mOffset));
				offset.selectAll();
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(offset, InputMethodManager.SHOW_FORCED);
				ATLog.i(TAG, "INFO. showMemOffsetDialog().onShow()");
			}
		});
		dialog.show();
		
		ATLog.i(TAG, "INFO. showMemOffsetDialog()");
	}
}
