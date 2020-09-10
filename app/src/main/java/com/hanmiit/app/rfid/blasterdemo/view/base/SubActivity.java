package com.hanmiit.app.rfid.blasterdemo.view.base;

import java.util.Locale;

import com.hanmiit.lib.diagnostics.ATLog;
import com.hanmiit.app.rfid.blasterdemo.util.ResUtil;

import android.app.Activity;
import android.view.MenuItem;

public abstract class SubActivity extends Activity {

	private static final String TAG = SubActivity.class.getSimpleName();
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		ATLog.i(TAG, "INFO. onOptionsItemSelected(%s)", ResUtil.getId(item.getItemId()));
		
		switch (item.getItemId()) {
		case android.R.id.home:
			this.setResult(Activity.RESULT_CANCELED);
			this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected abstract void initWidgets();

	protected abstract void enableActionWidgets(boolean enabled);

	protected String getRequestCode(int requestCode) {
		return String.format(Locale.US, "%d", requestCode);
	}

	protected String getResultCode(int resultCode) {
		switch (resultCode) {
		case Activity.RESULT_CANCELED:
			return "RESULT_CANCELED";
		case Activity.RESULT_OK:
			return "RESULT_OK";
		case Activity.RESULT_FIRST_USER:
			return "RESULT_FIRST_USER";
		}
		return String.format(Locale.US, "%d", resultCode);
	}
}
