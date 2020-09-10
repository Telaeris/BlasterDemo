package com.hanmiit.app.rfid.blasterdemo.dialog;

import java.util.Locale;

import com.hanmiit.lib.rfid.params.SelectMaskParam;
import com.hanmiit.lib.rfid.type.MaskActionType;
import com.hanmiit.lib.rfid.type.MaskTargetType;
import com.hanmiit.lib.rfid.type.MemoryBank;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.adapter.SpinnerAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class SelectMaskDialog implements OnClickListener, OnCancelListener {

	public static final int NEW_ITEM = 2001;
	public static final int MODIFY_ITEM = 2002;
	
	private static final int MAX_OFFSET = 16;
	
	private static final int DEFAULT_OFFSET = 16;
	private static final int DEFAULT_LENGTH = 0;

	private Spinner spnTarget;
	private Spinner spnAction;
	private Spinner spnBank;
	private Spinner spnOffset;
	private EditText edtMask;
	private Spinner spnLength;

	private SpinnerAdapter adpTarget;
	private SpinnerAdapter adpAction;
	private SpinnerAdapter adpBank;
	private SpinnerAdapter adpOffset;
	private SpinnerAdapter adpLength;

	private int mState;
	
	private AlertDialog.Builder mBuilder;
	private Dialog mDialog;
	private IDialogResultListener mListener;

	public SelectMaskDialog(Context context) {
		this.mState = NEW_ITEM;
		this.mListener = null;
		
		LinearLayout root = (LinearLayout) LinearLayout.inflate(context,
				R.layout.dialog_mask_item, null);
		this.spnTarget = (Spinner) root.findViewById(R.id.mask_target);
		this.adpTarget = new SpinnerAdapter(context,
				R.layout.item_spinner_dialog, R.layout.item_dialog_list);
		for (MaskTargetType item : MaskTargetType.values())
			this.adpTarget.addItem(item.getValue(), item.toString());
		this.spnTarget.setAdapter(this.adpTarget);

		this.spnAction = (Spinner) root.findViewById(R.id.mask_action);
		this.adpAction = new SpinnerAdapter(context,
				R.layout.item_spinner_dialog, R.layout.item_dialog_list);
		for (MaskActionType item : MaskActionType.values())
			this.adpAction.addItem(item.getValue(), item.toString());
		this.spnAction.setAdapter(this.adpAction);

		this.spnBank = (Spinner) root.findViewById(R.id.mask_bank);
		this.adpBank = new SpinnerAdapter(context,
				R.layout.item_spinner_dialog, R.layout.item_dialog_list);
		for (MemoryBank item : MemoryBank.values())
			this.adpBank.addItem(item.getValue(), item.toString());
		this.spnBank.setAdapter(this.adpBank);

		this.spnOffset = (Spinner) root.findViewById(R.id.mask_offset);
		this.adpOffset = new SpinnerAdapter(context,
				R.layout.item_spinner_dialog, R.layout.item_dialog_list);
		for (int i = 0; i < MAX_OFFSET; i++)
			this.adpOffset.addItem(i * 16, String.format(Locale.US, "%d bit", i * 16));
		this.spnOffset.setAdapter(this.adpOffset);

		this.edtMask = (EditText) root.findViewById(R.id.mask_value);

		this.spnLength = (Spinner) root.findViewById(R.id.mask_length);
		this.adpLength = new SpinnerAdapter(context,
				R.layout.item_spinner_dialog, R.layout.item_dialog_list);
		for (int i = 0; i < MAX_OFFSET; i++)
			this.adpLength.addItem(i * 16, String.format(Locale.US, "%d bit", i * 16));
		this.spnLength.setAdapter(this.adpLength);
		
		this.mBuilder = new AlertDialog.Builder(context);
		this.mBuilder.setTitle(R.string.mask_item_dialog);
		this.mBuilder.setView(root);
		this.mBuilder.setPositiveButton(R.string.action_ok, this);
		this.mBuilder.setNegativeButton(R.string.action_cancel, this);
		
		this.mDialog = this.mBuilder.create();
		this.mDialog.setCancelable(true);
		this.mDialog.setOnCancelListener(this);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case Dialog.BUTTON_POSITIVE:
			if (this.mListener != null)
				this.mListener.onOkClick(this.mState, dialog);
			break;
		case Dialog.BUTTON_NEGATIVE:
			if (this.mListener != null)
				this.mListener.onCancelClick(this.mState, dialog);
			break;
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		if (this.mListener != null)
			this.mListener.onCancelClick(this.mState, dialog);
	}
	
	public Dialog getDialog() {
		return this.mDialog;
	}
	
	public void setResultListener(IDialogResultListener listener) {
		this.mListener = listener;
	}

	public void initDialog() {
		this.mState = NEW_ITEM;
		this.spnTarget.setSelection(this.adpTarget.indexOf(MaskTargetType.SL.getValue()));
		this.spnAction.setSelection(this.adpAction.indexOf(MaskActionType.AB.getValue()));
		this.spnBank.setSelection(this.adpBank.indexOf(MemoryBank.EPC.getValue()));
		this.spnOffset.setSelection(this.adpOffset.indexOf(DEFAULT_OFFSET));
		this.edtMask.setText("");
		this.spnLength.setSelection(this.adpLength.indexOf(DEFAULT_LENGTH));
	}

	public void initDialog(SelectMaskParam param) {
		this.mState = MODIFY_ITEM;
		this.spnTarget.setSelection(this.adpTarget.indexOf(param.getTarget().getValue()));
		this.spnAction.setSelection(this.adpAction.indexOf(param.getAction().getValue()));
		this.spnBank.setSelection(this.adpBank.indexOf(param.getBank().getValue()));
		this.spnOffset.setSelection(this.adpOffset.indexOf(param.getOffset()));
		this.edtMask.setText(param.getMask());
		this.spnLength.setSelection(this.adpLength.indexOf(param.getLength()));
	}
	
	public SelectMaskParam getItem() {
		SelectMaskParam item = new SelectMaskParam(
				MaskTargetType.valueOf(this.adpTarget.getValue(this.spnTarget.getSelectedItemPosition())),
				MaskActionType.valueOf(this.adpAction.getValue(this.spnAction.getSelectedItemPosition())),
				MemoryBank.valueOf(this.adpBank.getValue(this.spnBank.getSelectedItemPosition())),
				this.adpOffset.getValue(this.spnOffset.getSelectedItemPosition()),
				this.edtMask.getText().toString(),
				this.adpLength.getValue(this.spnLength.getSelectedItemPosition()));
		return item;
	}
}
