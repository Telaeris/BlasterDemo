package com.hanmiit.app.rfid.blasterdemo.view;

import com.hanmiit.lib.barcode.ParamValueList;
import com.hanmiit.lib.barcode.type.I2of5CheckDigitVerification;
import com.hanmiit.lib.barcode.type.ParamName;
import com.hanmiit.lib.diagnostics.ATLog;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.adapter.ValueAdapter;
import com.hanmiit.app.rfid.blasterdemo.adapter.ValueItem;
import com.hanmiit.app.rfid.blasterdemo.view.base.RfidActivity;
import com.hanmiit.app.rfid.blasterdemo.widgets.SymbolLength;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

public class OptionSymbolI2of5Activity extends RfidActivity implements
		OnClickListener {

	@SuppressWarnings("unused")
	private static final String TAG = OptionSymbolI2of5Activity.class.getSimpleName();

	private CheckBox chkCheckDigit;
	private CheckBox chkConvert;
	private CheckBox chkReducedQuietZone;
	private Spinner chkCheckDigitVerify;
	private SymbolLength wgtLength;
	private Button btnSetOption;

	private ValueAdapter<I2of5CheckDigitVerification> adpCheckDigitVerify;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_option_symbol_interleaved2of5);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		this.setTitle(R.string.symbol_interleaved2of5_name);

		// Create Reader
		this.createReader();
		
		// Initialize Widget
		this.initWidgets();

		// Initialize Reader
		this.initReader();

		// Initialize Widgets
		this.chkCheckDigit = (CheckBox) findViewById(R.id.transmit_i2of5_check_digit);
		this.chkConvert = (CheckBox) findViewById(R.id.convert_i2of5_to_ean_13);
		this.chkReducedQuietZone = (CheckBox) findViewById(R.id.i2of5_reduced_quiet_znoe);
		this.chkCheckDigitVerify = (Spinner) findViewById(R.id.i2of5_check_digit_verification);
		this.adpCheckDigitVerify = new ValueAdapter<I2of5CheckDigitVerification>(
				this);
		for (I2of5CheckDigitVerification item : I2of5CheckDigitVerification
				.values()) {
			this.adpCheckDigitVerify
					.add(new ValueItem<I2of5CheckDigitVerification>(item
							.toString(), item));
		}
		this.chkCheckDigitVerify.setAdapter(this.adpCheckDigitVerify);
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

			paramList.add(ParamName.TransmitI2of5CheckDigit,
					this.chkCheckDigit.isChecked() ? 1 : 0);
			paramList.add(ParamName.ConvertI2of5toEAN13,
					this.chkConvert.isChecked() ? 1 : 0);
			paramList.add(ParamName.I2of5CheckDigitVerification,
					I2of5CheckDigitVerification
							.valueOf((byte) this.chkCheckDigitVerify
									.getSelectedItemPosition()).getCode());
			paramList.add(ParamName.I2of5Length1,
					this.wgtLength.getLength1());
			paramList.add(ParamName.I2of5Length2,
					this.wgtLength.getLength2());
			paramList.add(ParamName.I2of5ReducedQuietZone, this.chkReducedQuietZone.isChecked() ? 1 : 0);

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
				ParamName.TransmitI2of5CheckDigit,
				ParamName.ConvertI2of5toEAN13,
				ParamName.I2of5CheckDigitVerification,
				ParamName.I2of5Length1, ParamName.I2of5Length2,
				ParamName.I2of5ReducedQuietZone});
		this.chkCheckDigit.setChecked(paramList.getBoolean(ParamName.TransmitI2of5CheckDigit));
		this.chkConvert.setChecked(paramList.getBoolean(ParamName.ConvertI2of5toEAN13));
		this.chkCheckDigitVerify.setSelection(this.adpCheckDigitVerify
				.getPosition(I2of5CheckDigitVerification.valueOf( paramList
						.getValue(ParamName.I2of5CheckDigitVerification))));
		this.wgtLength.setLength(paramList.getValue(ParamName.I2of5Length1), paramList.getValue(ParamName.I2of5Length2));
		this.chkReducedQuietZone.setChecked(paramList.getBoolean(ParamName.I2of5ReducedQuietZone));
		
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
