package com.hanmiit.app.rfid.blasterdemo.view;

import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.adapter.ValueAdapter;
import com.hanmiit.app.rfid.blasterdemo.adapter.ValueItem;
import com.hanmiit.app.rfid.blasterdemo.view.base.RfidActivity;
import com.hanmiit.app.rfid.blasterdemo.widgets.SymbolLength;
import com.hanmiit.lib.barcode.ParamValueList;
import com.hanmiit.lib.barcode.type.CodabarStartStopCharactersDetection;
import com.hanmiit.lib.barcode.type.ParamName;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

public class OptionSymbolCodabarActivity extends RfidActivity implements
		OnClickListener {

	@SuppressWarnings("unused")
	private static final String TAG = OptionSymbolCodabarActivity.class.getSimpleName();

	private CheckBox chkClsiEditing;
	private CheckBox chkNotisEditing;
	private Spinner spnCharDetection;
	private SymbolLength wgtLength;
	private Button btnSetOption;
	
	private ValueAdapter<CodabarStartStopCharactersDetection> adpCharsDetection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_option_symbol_codabar);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		this.setTitle(R.string.symbol_codabar_name);

		// Create Reader
		this.createReader();
		
		// Initialize Widget
		this.initWidgets();

		// Initialize Reader
		this.initReader();
				
		// Initialize Widgets
		this.chkClsiEditing = (CheckBox) findViewById(R.id.clsi_editing);
		this.chkNotisEditing = (CheckBox) findViewById(R.id.notis_editing);
		
		this.wgtLength = (SymbolLength) findViewById(R.id.length);
		
		this.btnSetOption = (Button) findViewById(R.id.set_option);
		this.btnSetOption.setOnClickListener(this);
		
		this.spnCharDetection = (Spinner) findViewById(R.id.codabar_start_stop_chars_detection);
		this.adpCharsDetection = new ValueAdapter<CodabarStartStopCharactersDetection>(this);
		for (CodabarStartStopCharactersDetection item : CodabarStartStopCharactersDetection
				.values()) {
			this.adpCharsDetection
					.add(new ValueItem<CodabarStartStopCharactersDetection>(item
							.toString(), item));
		}
		this.spnCharDetection.setAdapter(adpCharsDetection);

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

			paramList.add(ParamName.CLSIEditing,
					this.chkClsiEditing.isChecked() ? 1 : 0);
			paramList.add(ParamName.NOTISEditing,
					this.chkNotisEditing.isChecked() ? 1 : 0);
			paramList.add(ParamName.CodabarLength1,
					this.wgtLength.getLength1());
			paramList.add(ParamName.CodabarLength2,
					this.wgtLength.getLength2());
			
			paramList.add(ParamName.CodabarCharDetection,
					CodabarStartStopCharactersDetection
							.valueOf((byte) this.spnCharDetection.getSelectedItemPosition()).getCode());

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
				ParamName.CodabarLength1, ParamName.CodabarLength2,
				ParamName.CLSIEditing, ParamName.NOTISEditing,
				ParamName.CodabarCharDetection});
		this.chkClsiEditing.setChecked(paramList.getBoolean(ParamName.CLSIEditing));
		this.chkNotisEditing.setChecked(paramList.getBoolean(ParamName.NOTISEditing));
		this.wgtLength.setLength(paramList.getValue(ParamName.CodabarLength1), paramList.getValue(ParamName.CodabarLength2));
		this.spnCharDetection.setSelection(this.adpCharsDetection
				.getPosition(CodabarStartStopCharactersDetection.valueOf( paramList
						.getValue(ParamName.CodabarCharDetection))));
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
