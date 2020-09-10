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
import android.widget.CheckBox;
import android.widget.Toast;

public class OptionSymbolCode39Activity extends RfidActivity implements
		OnClickListener {

	@SuppressWarnings("unused")
	private static final String TAG = OptionSymbolCode39Activity.class.getSimpleName();

	private CheckBox chkTrioptic;
	private CheckBox chkConvert;
	private CheckBox chkPrefix;
	private CheckBox chkCheckDigitVerify;
	private CheckBox chkCheckDigit;
	private CheckBox chkFullAscii;
	private CheckBox chkBufferCode39;
	private CheckBox chkReducedQuietZone;
	private SymbolLength wgtLength;
	private Button btnSetOption;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_option_symbol_code39);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		this.setTitle(R.string.symbol_code39_name);

		// Initialize Widgets
		this.chkTrioptic = (CheckBox) findViewById(R.id.trioptic_code_39);
		this.chkConvert = (CheckBox) findViewById(R.id.convert_code_39_to_code_32);
		this.chkPrefix = (CheckBox) findViewById(R.id.code_32_prefix);
		this.chkCheckDigitVerify = (CheckBox) findViewById(R.id.code_39_check_digit_verification);
		this.chkCheckDigit = (CheckBox) findViewById(R.id.transmit_code_39_check_digit);
		this.chkFullAscii = (CheckBox) findViewById(R.id.code_39_full_ascii);
		this.chkBufferCode39 = (CheckBox) findViewById(R.id.buffer_code_39);
		this.chkReducedQuietZone = (CheckBox) findViewById(R.id.code_39_reduced_quiet_zone);
		this.wgtLength = (SymbolLength) findViewById(R.id.length);
		this.btnSetOption = (Button) findViewById(R.id.set_option);
		this.btnSetOption.setOnClickListener(this);

		// Create Reader
		this.createReader();
		
		// Initialize Widget
		this.initWidgets();

		// Initialize Reader
		this.initReader();
		
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

			paramList.add(ParamName.TriopticCode39,
					this.chkTrioptic.isChecked() ? 1 : 0);
			paramList.add(ParamName.ConvertCode39toCode32,
					this.chkConvert.isChecked() ? 1 : 0);
			paramList
					.add(ParamName.Code32Prefix, this.chkPrefix.isChecked() ? 1 : 0);
			paramList.add(ParamName.Code39CheckDigitVerification,
					this.chkCheckDigitVerify.isChecked() ? 1 : 0);
			paramList.add(ParamName.TransmitCode39CheckDigit,
					this.chkCheckDigit.isChecked() ? 1 : 0);
			paramList.add(ParamName.Code39FullASCIIConversion,
					this.chkFullAscii.isChecked() ? 1 : 0);
			
			paramList.add(ParamName.Code39Length1,
					this.wgtLength.getLength1());
			paramList.add(ParamName.Code39Length2,
					this.wgtLength.getLength2());
			
			paramList.add(ParamName.BufferCode39, this.chkBufferCode39.isChecked() ? 1 : 0);
			paramList.add(ParamName.Code39ReducedQuietZone, this.chkReducedQuietZone.isChecked() ? 1 : 0);
			
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
		
		ParamValueList paramList = getReader().getBarcodeParam(new ParamName[]{
			ParamName.TriopticCode39,
			ParamName.ConvertCode39toCode32,
			ParamName.Code32Prefix,
			ParamName.Code39CheckDigitVerification,
			ParamName.TransmitCode39CheckDigit,
			ParamName.Code39FullASCIIConversion,
			ParamName.Code39Length1, ParamName.Code39Length2,
			ParamName.BufferCode39, ParamName.Code39ReducedQuietZone
		});
		
		this.chkTrioptic.setChecked(paramList.getBoolean(ParamName.TriopticCode39));
		this.chkConvert.setChecked(paramList.getBoolean(ParamName.ConvertCode39toCode32));
		this.chkPrefix.setChecked(paramList.getBoolean(ParamName.Code32Prefix));
		this.chkCheckDigitVerify.setChecked(paramList.getBoolean(ParamName.Code39CheckDigitVerification));
		this.chkCheckDigit.setChecked(paramList.getBoolean(ParamName.TransmitCode39CheckDigit));
		this.chkFullAscii.setChecked(paramList.getBoolean(ParamName.Code39FullASCIIConversion));
		this.wgtLength.setLength(paramList.getValue(ParamName.Code39Length1),
								paramList.getValue(ParamName.Code39Length2));
		this.chkBufferCode39.setChecked(paramList.getBoolean(ParamName.BufferCode39));
		this.chkReducedQuietZone.setChecked(paramList.getBoolean(ParamName.Code39ReducedQuietZone));
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
