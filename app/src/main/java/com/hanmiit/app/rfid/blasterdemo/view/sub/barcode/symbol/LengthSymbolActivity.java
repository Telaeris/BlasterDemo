package com.hanmiit.app.rfid.blasterdemo.view.sub.barcode.symbol;

import com.hanmiit.lib.diagnostics.ATLog;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.view.base.RfidActivity;

import android.content.Intent;
import android.os.Bundle;

public class LengthSymbolActivity extends RfidActivity {

	private static final String TAG = LengthSymbolActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_barcode_option_symbol_length);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		ATLog.i(TAG, "INFO. onCreate()");

		// Create Reader
		this.createReader();

		// Initialize Widget
		this.initWidgets();

		// Initialize Reader
		this.initReader();

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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		ATLog.d(TAG, "DEBUG. onActivityResult(%s, %s)", getRequestCode(requestCode), getResultCode(resultCode));

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void initWidgets() {
	}

	@Override
	protected void enableActionWidgets(boolean enabled) {
	}

	@Override
	protected void initReader() {

	}

}
