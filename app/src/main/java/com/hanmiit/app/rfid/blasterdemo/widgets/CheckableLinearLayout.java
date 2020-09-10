package com.hanmiit.app.rfid.blasterdemo.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class CheckableLinearLayout extends LinearLayout implements Checkable {

	private final String NS = "http://schemas.android.com/apk/res/com.hanmiit.app.rfid.blasterdemo.";
	private final String ATTR = "checkable";

	private int mCheckableId;
	private Checkable mCheckable;

	public CheckableLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mCheckableId = attrs.getAttributeResourceValue(NS, ATTR, 0);
	}

	@Override
	public void setChecked(boolean checked) {
		this.mCheckable = (Checkable)findViewById(this.mCheckableId);
		if (this.mCheckable == null) {
			return;
		}
		this.mCheckable.setChecked(checked);
	}

	@Override
	public boolean isChecked() {
		this.mCheckable = (Checkable)findViewById(this.mCheckableId);
		if (this.mCheckable == null) {
			return false;
		}
		return this.mCheckable.isChecked();
	}

	@Override
	public void toggle() {
		this.mCheckable = (Checkable)findViewById(this.mCheckableId);
		if (this.mCheckable == null) {
			return;
		}
		this.mCheckable.toggle();
	}
}
