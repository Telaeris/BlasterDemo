package com.hanmiit.app.rfid.blasterdemo.view;

import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.view.base.RfidActivity;
import com.hanmiit.app.rfid.blasterdemo.adapter.ValueAdapter;
import com.hanmiit.app.rfid.blasterdemo.adapter.ValueItem;
import com.hanmiit.app.rfid.blasterdemo.widgets.SymbolLength;
import com.hanmiit.lib.barcode.ParamValueList;
import com.hanmiit.lib.barcode.type.ParamName;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class OptionSymbolCode128Activity extends RfidActivity implements OnClickListener {

	@SuppressWarnings("unused")
	private static final String TAG = OptionSymbolCode128Activity.class.getSimpleName();

	private CheckBox chkEnable;
	private CheckBox chkOption1;
	private CheckBox chkOption2;
	private CheckBox chkOption3;
	private CheckBox chkOption4;
	private CheckBox chkOption5;
	private CheckBox chkOption6;
	private LinearLayout IsbtConcatenationRedundancy_lbl;
	private Spinner spnIsbtConcatenationRedundancy;
	private SymbolLength wgtLength;
	private Button btnSetOption;

	private ValueAdapter<Integer> adpIsbtConcatenationRedundancy;
	private ParamName symbol_name;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_option_symbol_cod128);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Create Reader
		this.createReader();
		
		// Initialize Widget
		this.initWidgets();

		// Initialize Reader
		this.initReader();
				
		// Initialize Widgets
		this.chkEnable = (CheckBox) findViewById(R.id.symbol_enable);
		this.chkOption1 = (CheckBox) findViewById(R.id.symbol_opt1);
		this.chkOption2 = (CheckBox) findViewById(R.id.symbol_opt2);
		this.chkOption3 = (CheckBox) findViewById(R.id.symbol_opt3);
		this.chkOption4 = (CheckBox) findViewById(R.id.symbol_opt4);
		this.chkOption5 = (CheckBox) findViewById(R.id.symbol_opt5);
		this.chkOption6 = (CheckBox) findViewById(R.id.symbol_opt6);

		this.IsbtConcatenationRedundancy_lbl = (LinearLayout) findViewById(R.id.IsbtConcatenationRedundancy_lbl);
		this.spnIsbtConcatenationRedundancy = (Spinner) findViewById(R.id.IsbtConcatenationRedundancy);
		this.adpIsbtConcatenationRedundancy = new ValueAdapter<Integer>(this);
		for (int i = 2; i <= 20; i++) {
			adpIsbtConcatenationRedundancy.add(new ValueItem<Integer>("" + i, i));
		}
		this.adpIsbtConcatenationRedundancy.notifyDataSetChanged();
		this.spnIsbtConcatenationRedundancy.setAdapter(adpIsbtConcatenationRedundancy);
		this.wgtLength = (SymbolLength) findViewById(R.id.length);

		this.btnSetOption = (Button) findViewById(R.id.set_option);
		this.btnSetOption.setOnClickListener(this);

		// Initialize Symbol State List
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

			switch (symbol_name) {
			case Code128:
				paramList.add(ParamName.Code128, this.chkEnable.isChecked() ? 1 : 0);
				paramList.add(ParamName.GS1128, this.chkOption1.isChecked() ? 1 : 0);
				paramList.add(ParamName.ISBT128, this.chkOption2.isChecked() ? 1 : 0);
				paramList.add(ParamName.ISBTConcatenation, this.chkOption3.isChecked() ? 1 : 0);
				paramList.add(ParamName.CheckISBTTable, this.chkOption4.isChecked() ? 1 : 0);
				paramList.add(ParamName.ISBTConcatenationRedundancy,
						(Integer)this.spnIsbtConcatenationRedundancy.getSelectedItem());
				paramList.add(ParamName.Code128Length1, this.wgtLength.getLength1());
				paramList.add(ParamName.Code128Length2, this.wgtLength.getLength2());
				paramList.add(ParamName.Code128ReducedQuietZone, this.chkOption5.isChecked() ? 1 : 0);
				paramList.add(ParamName.IgnoreCode128FNC4, this.chkOption6.isChecked() ? 1 : 0);
				break;
			case M2of5:
				paramList.add(ParamName.M2of5, this.chkEnable.isChecked() ? 1 : 0);
				paramList.add(ParamName.M2of5CheckDigit, this.chkOption1.isChecked() ? 1 : 0);
				paramList.add(ParamName.TransmitM2of5CheckDigit, this.chkOption2.isChecked() ? 1 : 0);
				paramList.add(ParamName.M2of5Length1, this.wgtLength.getLength1());
				paramList.add(ParamName.M2of5Length2, this.wgtLength.getLength2());
				break;
			default:
				break;
			}

			// Set Scanner Parameter
			if (getReader().setBarcodeParam(paramList)) {
				this.setResult(RESULT_OK);
				this.finish();
			} else {
				Toast.makeText(this, R.string.faile_to_set_symbologies, Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

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
		symbol_name = (ParamName) getIntent().getSerializableExtra("Symbol_Name");

		ParamValueList paramList = getReader().getBarcodeParam(new ParamName[] { ParamName.Code128,
				ParamName.GS1128, ParamName.ISBT128, ParamName.ISBTConcatenation,
				ParamName.CheckISBTTable, ParamName.ISBTConcatenationRedundancy,
				ParamName.Code128Length1, ParamName.Code128Length2, ParamName.M2of5,
				ParamName.M2of5CheckDigit, ParamName.TransmitM2of5CheckDigit,
				ParamName.M2of5Length1, ParamName.M2of5Length2,
				ParamName.Code128ReducedQuietZone, ParamName.IgnoreCode128FNC4});

		switch (symbol_name) {
		case Code128:
			this.setTitle(R.string.symbol_code128_name);
			this.chkEnable.setText("Enabled");
			this.chkOption1.setText("GS1-128(formerly UCC/EAN-128");
			this.chkOption2.setText("ISBT 128");
			this.chkOption3.setText("ISBT Concatenation");
			this.chkOption4.setText("Check ISBT Table");
			this.chkOption5.setText("Reduced Quiet Zone");
			this.chkOption6.setText("Ignore Code 128 <FNC4>");

			this.chkEnable.setChecked(paramList.getBoolean(ParamName.Code128));
			this.chkOption1.setChecked(paramList.getBoolean(ParamName.GS1128));
			this.chkOption2.setChecked(paramList.getBoolean(ParamName.ISBT128));
			this.chkOption3.setChecked(paramList.getBoolean(ParamName.ISBTConcatenation));
			this.chkOption4.setChecked(paramList.getBoolean(ParamName.CheckISBTTable));
			this.chkOption5.setChecked(paramList.getBoolean(ParamName.Code128ReducedQuietZone));
			this.chkOption6.setChecked(paramList.getBoolean(ParamName.IgnoreCode128FNC4));

			this.spnIsbtConcatenationRedundancy
					.setSelection(paramList.getValue(ParamName.ISBTConcatenationRedundancy) - 2);

			this.wgtLength.setLength(paramList.getValue(ParamName.Code128Length1), paramList.getValue(ParamName.Code128Length2));

			break;
		case M2of5:
			this.setTitle(R.string.symbol_matrix2of5_name);
			this.chkEnable.setText("Enabled");
			this.chkOption1.setText("Check Digit");
			this.chkOption2.setText("Transmit Matrix2of5 Check Digit");
			this.chkOption3.setText("");
			this.chkOption4.setText("");
			this.chkOption3.setVisibility(View.GONE);
			this.chkOption4.setVisibility(View.GONE);
			this.chkOption5.setVisibility(View.GONE);
			this.chkOption6.setVisibility(View.GONE);
			this.IsbtConcatenationRedundancy_lbl.setVisibility(View.GONE);

			this.chkEnable.setChecked(paramList.getBoolean(ParamName.M2of5));
			this.chkOption1.setChecked(paramList.getBoolean(ParamName.M2of5CheckDigit));
			this.chkOption2.setChecked(paramList.getBoolean(ParamName.TransmitM2of5CheckDigit));
			this.wgtLength.setLength(paramList.getValue(ParamName.M2of5Length1), paramList.getValue(ParamName.M2of5Length2));

			break;
		default:
			break;
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
