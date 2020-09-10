package com.hanmiit.app.rfid.blasterdemo.view;

import com.hanmiit.lib.barcode.ParamValueList;
import com.hanmiit.lib.barcode.type.MsiCheckDigitAlgorithm;
import com.hanmiit.lib.barcode.type.MsiCheckDigits;
import com.hanmiit.lib.barcode.type.ParamName;
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

public class OptionSymbolMsiActivity extends RfidActivity implements
		OnClickListener {

	@SuppressWarnings("unused")
	private static final String TAG = OptionSymbolMsiActivity.class.getSimpleName();

	private Spinner spnCheckDigits;
	private CheckBox chkTransmit;
	private Spinner spnAlgorithm;
	private SymbolLength wgtLength;
	private Button btnSetOption;

	private ValueAdapter<MsiCheckDigits> adpCheckDigits;
	private ValueAdapter<MsiCheckDigitAlgorithm> adpAlgorithm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_option_symbol_msi);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		this.setTitle(R.string.symbol_msi_name);

		// Create Reader
		this.createReader();
		
		// Initialize Widget
		this.initWidgets();

		// Initialize Reader
		this.initReader();

		// Initialize Widgets
		this.spnCheckDigits = (Spinner) findViewById(R.id.msi_check_digits);
		this.adpCheckDigits = new ValueAdapter<MsiCheckDigits>(this);
		for (MsiCheckDigits item : MsiCheckDigits.values()) {
			this.adpCheckDigits.add(new ValueItem<MsiCheckDigits>(item
					.toString(), item));
		}
		this.spnCheckDigits.setAdapter(this.adpCheckDigits);
		this.chkTransmit = (CheckBox) findViewById(R.id.transmit_msi_check_digit);
		this.spnAlgorithm = (Spinner) findViewById(R.id.msi_check_digit_algorithm);
		this.adpAlgorithm = new ValueAdapter<MsiCheckDigitAlgorithm>(this);
		for (MsiCheckDigitAlgorithm item : MsiCheckDigitAlgorithm.values()) {
			this.adpAlgorithm.add(new ValueItem<MsiCheckDigitAlgorithm>(item
					.toString(), item));
		}
		this.spnAlgorithm.setAdapter(this.adpAlgorithm);
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

			paramList.add(ParamName.MSICheckDigit, MsiCheckDigits
					.valueOf((byte) this.spnCheckDigits
							.getSelectedItemPosition()).getCode());
			paramList.add(ParamName.TransmitMSICheckDigit,
					this.chkTransmit.isChecked() ? 1 : 0);
			paramList.add(ParamName.MSICheckDigitAlgorithm,
					MsiCheckDigitAlgorithm.valueOf((byte) this.spnAlgorithm
							.getSelectedItemPosition()).getCode());
			paramList.add(ParamName.MSILength1,
					this.wgtLength.getLength1());
			paramList.add(ParamName.MSILength2,
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
				ParamName.MSICheckDigit,
				ParamName.TransmitMSICheckDigit,
				ParamName.MSICheckDigitAlgorithm,
				ParamName.MSILength1, ParamName.MSILength2 });
		this.spnCheckDigits.setSelection(this.adpCheckDigits
				.getPosition(MsiCheckDigits.valueOf( (byte)paramList.getValue(ParamName.MSICheckDigit))));
		this.chkTransmit.setChecked(paramList.getBoolean(ParamName.TransmitMSICheckDigit));
		this.spnAlgorithm.setSelection(this.adpAlgorithm
				.getPosition(MsiCheckDigitAlgorithm.valueOf( (byte)paramList.getValue(ParamName.MSICheckDigitAlgorithm))));
		this.wgtLength.setLength(paramList.getValue(ParamName.MSILength1), paramList.getValue(ParamName.MSILength2));
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
