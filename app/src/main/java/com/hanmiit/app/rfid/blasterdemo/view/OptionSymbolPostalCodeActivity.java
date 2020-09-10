package com.hanmiit.app.rfid.blasterdemo.view;

import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.view.base.RfidActivity;
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

public class OptionSymbolPostalCodeActivity extends RfidActivity implements
											OnClickListener {

	@SuppressWarnings("unused")
	private static final String TAG = OptionSymbolPostalCodeActivity.class.getSimpleName();
	private ParamName symbol_name;
	
	private CheckBox chkSymbolEnable;
	private CheckBox chkTransmitCheckDigit;
	private Button btnSetOption;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_option_symbol_postal);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		this.symbol_name = (ParamName)getIntent().getSerializableExtra("Symbol_Name");
		
		if(symbol_name == ParamName.USPostnet){
			this.setTitle(R.string.symbol_uspostnet_name);
		}else if(symbol_name == ParamName.USPlanet){
			this.setTitle(R.string.symbol_usplanet_name);
		}else if(symbol_name == ParamName.UKPostal){
			this.setTitle(R.string.symbol_ukpostal_name);
		}else{
			
		}
		
		this.chkSymbolEnable = (CheckBox) findViewById(R.id.symbol_enable);
		this.chkTransmitCheckDigit = (CheckBox) findViewById(R.id.transmit_checkdigit);
		this.btnSetOption = (Button) findViewById(R.id.set_option);
		this.btnSetOption.setOnClickListener(this);
		
		// Create Reader
		this.createReader();
		
		// Initialize Widget
		this.initWidgets();

		// Initialize Reader
		this.initReader();
		
		initSymbolStateList();
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
			
			if(symbol_name == ParamName.USPostnet){
				paramList.add(ParamName.USPostnet, this.chkSymbolEnable.isChecked() ? 1 : 0);
				paramList.add(ParamName.TransmitUSPostalCheckDigit, this.chkTransmitCheckDigit.isChecked() ? 1 : 0);
			}else if(symbol_name == ParamName.USPlanet){
				paramList.add(ParamName.USPlanet, this.chkSymbolEnable.isChecked() ? 1 : 0);
				paramList.add(ParamName.TransmitUSPostalCheckDigit, this.chkTransmitCheckDigit.isChecked() ? 1 : 0);
			}else if(symbol_name == ParamName.UKPostal){
				paramList.add(ParamName.UKPostal, this.chkSymbolEnable.isChecked() ? 1 : 0);
				paramList.add(ParamName.TransmitUKPostalCheckDigit, this.chkTransmitCheckDigit.isChecked() ? 1 : 0);
			}else{
				
			}
			
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

	// Initialize Symbol State List
	private void initSymbolStateList() {
		ParamValueList paramList = null;
		
		if(symbol_name == ParamName.USPostnet){
			paramList = getReader().getBarcodeParam(new ParamName[]{ParamName.USPostnet, 
																 ParamName.TransmitUSPostalCheckDigit});
			this.chkSymbolEnable.setChecked(paramList.getBoolean(ParamName.USPostnet));
			this.chkTransmitCheckDigit.setChecked(paramList.getBoolean(ParamName.TransmitUSPostalCheckDigit));
			
		}else if(symbol_name == ParamName.USPlanet){
			paramList = getReader().getBarcodeParam(new ParamName[]{ParamName.USPlanet, 
																 ParamName.TransmitUSPostalCheckDigit});
			this.chkSymbolEnable.setChecked(paramList.getBoolean(ParamName.USPlanet));
			this.chkTransmitCheckDigit.setChecked(paramList.getBoolean(ParamName.TransmitUSPostalCheckDigit));
			
		}else if(symbol_name == ParamName.UKPostal){
			paramList = getReader().getBarcodeParam(new ParamName[]{ParamName.UKPostal, 
																 ParamName.TransmitUKPostalCheckDigit});
			this.chkSymbolEnable.setChecked(paramList.getBoolean(ParamName.UKPostal));
			this.chkTransmitCheckDigit.setChecked(paramList.getBoolean(ParamName.TransmitUKPostalCheckDigit));
			
		}else{
			
		}
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
