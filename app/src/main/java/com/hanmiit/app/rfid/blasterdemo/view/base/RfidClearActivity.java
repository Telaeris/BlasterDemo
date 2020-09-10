package com.hanmiit.app.rfid.blasterdemo.view.base;

import com.hanmiit.lib.diagnostics.ATLog;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.util.ResUtil;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public abstract class RfidClearActivity extends RfidActivity implements
		OnClickListener {

	private static final String TAG = RfidClearActivity.class.getSimpleName();
	
	protected Button btnClear;

	@Override
	public void onClick(View v) {
		
		ATLog.i(TAG, "INFO. onClick(%s)", ResUtil.getId(v.getId()));
		
		if (v.getId() == R.id.clear) {
			clearWidgets();
		}
	}

	protected abstract void clearWidgets();

	protected void initWidgets() {

		btnClear = (Button) findViewById(R.id.clear);
		btnClear.setOnClickListener(this);
		
		ATLog.i(TAG, "INFO. initWidgets()");
	}

	protected void enableActionWidgets(boolean enabled) {
		btnClear.setEnabled(isEnabledWidget(enabled));
		
		ATLog.i(TAG, "INFO. enableActionWidgets(%s)", enabled);
	}
}
