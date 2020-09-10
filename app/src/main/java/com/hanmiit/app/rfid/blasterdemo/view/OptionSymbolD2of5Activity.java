package com.hanmiit.app.rfid.blasterdemo.view;

import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.view.base.RfidActivity;
import com.hanmiit.app.rfid.blasterdemo.widgets.SymbolLength;
import com.hanmiit.lib.barcode.ParamValueList;
import com.hanmiit.lib.barcode.type.ParamName;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class OptionSymbolD2of5Activity extends RfidActivity implements
		OnClickListener {

	@SuppressWarnings("unused")
	private static final String TAG = OptionSymbolD2of5Activity.class.getSimpleName();

	private SymbolLength wgtLength;
	private Button btnSetOption;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_option_symbol_discrete2of5);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		this.setTitle(R.string.symbol_discete2of5_name);

		// Create Reader
		this.createReader();
		
		// Initialize Widget
		this.initWidgets();

		// Initialize Reader
		this.initReader();
				
		// Initialize Widgets
		this.wgtLength = (SymbolLength) findViewById(R.id.length);
		this.btnSetOption = (Button) findViewById(R.id.set_option);
		this.btnSetOption.setOnClickListener(this);

		// Load Scanner Symbol Detail Option
		loadOption();
	}

	@Override
	protected void onDestroy() {
		
		// Destroy Reader
		destroyReader();
				
		super.onDestroy();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
	}

	@Override
	protected void onStop() {

		super.onStop();
	}

	@SuppressLint("ShowToast")
	@Override
	public void onClick(View v) {
		if (R.id.set_option == v.getId()) {
			ParamValueList paramList = new ParamValueList();

			paramList.add(ParamName.D2of5Length1,
					this.wgtLength.getLength1());
			paramList.add(ParamName.D2of5Length2,
					this.wgtLength.getLength2());

			if (getReader().setBarcodeParam(paramList)) {
				this.setResult(RESULT_OK);
				this.finish();
			} else {
				Toast.makeText(this, R.string.faile_to_set_symbologies,
						Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// Load Scanner Symbol Detail Option
	private void loadOption() {
		ParamValueList paramList = getReader().getBarcodeParam(new ParamName[] {
				ParamName.D2of5Length1, ParamName.D2of5Length2 });
		this.wgtLength.setLength(paramList.getValue(ParamName.D2of5Length1), paramList.getValue(ParamName.D2of5Length2));
	}

	@Override
	protected void initReader() {
		
	}

	@Override
	protected void initWidgets() {
				
	}

	@Override
	protected void enableActionWidgets(boolean enabled) {
		
	}
}
