package com.hanmiit.app.rfid.blasterdemo.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.hanmiit.lib.barcode.ParamValueList;
import com.hanmiit.lib.barcode.type.Code11CheckDigitVerification;
import com.hanmiit.lib.barcode.type.ParamName;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.adapter.ValueAdapter;
import com.hanmiit.app.rfid.blasterdemo.adapter.ValueItem;
import com.hanmiit.app.rfid.blasterdemo.view.base.RfidActivity;
import com.hanmiit.app.rfid.blasterdemo.widgets.SymbolLength;

public class OptionSymbolCode11Activity extends RfidActivity implements
		OnClickListener {

	@SuppressWarnings("unused")
	private static final String TAG = OptionSymbolCode11Activity.class.getSimpleName();

	private CheckBox chkCheckDigit;
	private Spinner spnCheckDigitVerify;
	private SymbolLength wgtLength;
	private Button btnSetOption;

	private ValueAdapter<Code11CheckDigitVerification> adpDigitVerify;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_option_symbol_code11);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		this.setTitle(R.string.symbol_code11_name);

		// Create Reader
		this.createReader();
		
		// Initialize Widget
		this.initWidgets();

		// Initialize Reader
		this.initReader();

		// Initialize Widgets
		this.chkCheckDigit = (CheckBox) findViewById(R.id.transmit_code_11_check_digit);
		this.spnCheckDigitVerify = (Spinner) findViewById(R.id.code_11_check_digit_verification);
		this.adpDigitVerify = new ValueAdapter<Code11CheckDigitVerification>(
				this);
		for (Code11CheckDigitVerification item : Code11CheckDigitVerification
				.values()) {
			this.adpDigitVerify
					.add(new ValueItem<Code11CheckDigitVerification>(item
							.toString(), item));
		}
		this.spnCheckDigitVerify.setAdapter(this.adpDigitVerify);
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

			paramList.add(ParamName.TransmitCode11CheckDigit,
					this.chkCheckDigit.isChecked() ? 1 : 0);
			paramList.add(ParamName.Code11CheckDigitVerification,
					this.adpDigitVerify.getItem(this.spnCheckDigitVerify
							.getSelectedItemPosition()).getCode());
			paramList.add(ParamName.Code11Length1,
					this.wgtLength.getLength1());
			paramList.add(ParamName.Code11Length2,
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
				ParamName.TransmitCode11CheckDigit,
				ParamName.Code11CheckDigitVerification,
				ParamName.Code11Length1, ParamName.Code11Length2 });
		this.chkCheckDigit.setChecked(paramList.getBoolean(ParamName.TransmitCode11CheckDigit));
		this.spnCheckDigitVerify
				.setSelection(this.adpDigitVerify.getPosition(Code11CheckDigitVerification.valueOf((byte) paramList
						.getValue(ParamName.Code11CheckDigitVerification))));
		this.wgtLength.setLength(
				paramList.getValue(ParamName.Code11Length1),
				paramList.getValue(ParamName.Code11Length2));
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
