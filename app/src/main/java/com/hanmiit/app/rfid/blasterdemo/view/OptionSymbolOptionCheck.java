package com.hanmiit.app.rfid.blasterdemo.view;

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
import android.widget.TextView;
import android.widget.Toast;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.adapter.ValueAdapter;
import com.hanmiit.lib.barcode.ParamValueList;
import com.hanmiit.lib.barcode.type.ParamName;
import com.hanmiit.lib.barcode.type.UpcCompositeMode;
import com.hanmiit.lib.barcode.type.CompositeBeepMode;
import com.hanmiit.lib.barcode.type.EncodingType;
import com.hanmiit.lib.barcode.type.InverseType;
import com.hanmiit.app.rfid.blasterdemo.adapter.ValueItem;
import com.hanmiit.app.rfid.blasterdemo.view.base.RfidActivity;

public class OptionSymbolOptionCheck extends RfidActivity implements OnClickListener {

	@SuppressWarnings("unused")
	private static final String TAG = OptionSymbolOptionCheck.class.getSimpleName();

	private CheckBox chkOption1;
	private CheckBox chkOption2;
	private CheckBox chkOption3;
	private LinearLayout lnlOption;
	private TextView txtOption4;
	private Spinner spnOption4;
	private Button btnSetOption;

	private TextView txtUpcCompositeMode;
	private TextView txtCompositeBeepMode;
	private CheckBox chkGS1EmulMode;
	private Spinner spnCompositeMode;
	private ValueAdapter<UpcCompositeMode> adpCompositeMode;
	private Spinner spnCompositeBeepMode;
	private ValueAdapter<CompositeBeepMode> adpCompositeBeepMode;

	private ParamName symbol_name;
	private ValueAdapter<EncodingType> encodingAdapter;
	private ValueAdapter<InverseType> inverseAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_option_symbol_option_check);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Create Reader
		this.createReader();
		
		// Initialize Widget
		this.initWidgets();

		// Initialize Reader
		this.initReader();
		
		this.symbol_name = (ParamName) getIntent().getSerializableExtra("Symbol_Name");

		// Initialize Widgets
		this.chkOption1 = (CheckBox) findViewById(R.id.symbol_enable);
		this.chkOption2 = (CheckBox) findViewById(R.id.symbol_opt1);
		this.chkOption3 = (CheckBox) findViewById(R.id.symbol_opt2);
		this.lnlOption = (LinearLayout) findViewById(R.id.symbol_opt3__lnl);
		this.txtOption4 = (TextView) findViewById(R.id.symbol_opt3_txt);
		this.spnOption4 = (Spinner) findViewById(R.id.symbol_opt3);

		this.txtUpcCompositeMode = (TextView) findViewById(R.id.symbol_upcCompositeMode);
		this.txtCompositeBeepMode = (TextView) findViewById(R.id.symbol_CompositeBeepMode);
		this.chkGS1EmulMode = (CheckBox) findViewById(R.id.symbol_GS1_128EmulMode);
		this.spnCompositeMode = (Spinner) findViewById(R.id.spinnerUpcCompositeMode);
		this.adpCompositeMode = new ValueAdapter<UpcCompositeMode>(this);
		this.spnCompositeBeepMode = (Spinner) findViewById(R.id.spinnerCompositeBeepMode);
		this.adpCompositeBeepMode = new ValueAdapter<CompositeBeepMode>(this);

		if (symbol_name != ParamName.CompositeCCAB && symbol_name != ParamName.CompositeCCC && 
				symbol_name != ParamName.CompositeTLC39) {
			this.txtUpcCompositeMode.setVisibility(View.INVISIBLE);
			this.txtCompositeBeepMode.setVisibility(View.INVISIBLE);
			this.chkGS1EmulMode.setVisibility(View.INVISIBLE);
			this.spnCompositeMode.setVisibility(View.INVISIBLE);
			this.spnCompositeBeepMode.setVisibility(View.INVISIBLE);
		}

		if (symbol_name == ParamName.AustraliaPost) {
			this.encodingAdapter = new ValueAdapter<EncodingType>(this);
			for (EncodingType item : EncodingType.values()) {
				this.encodingAdapter.add(new ValueItem<EncodingType>(item.toString(), item));
			}

			this.encodingAdapter.notifyDataSetChanged();
			this.spnOption4.setAdapter(encodingAdapter);
		} else {
			this.inverseAdapter = new ValueAdapter<InverseType>(this);
			for (InverseType item : InverseType.values()) {
				this.inverseAdapter.add(new ValueItem<InverseType>(item.toString(), item));
			}

			this.inverseAdapter.notifyDataSetChanged();
			this.spnOption4.setAdapter(inverseAdapter);
		}

		this.btnSetOption = (Button) findViewById(R.id.set_option);
		this.btnSetOption.setOnClickListener(this);

		// Initialize Symbol State List
		enableSymbolOptionClear();
		initSymbolStateList();
	}

	@Override
	protected void onDestroy() {
		symbol_name = null;

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
			case Aztec:
				paramList.add(ParamName.Aztec, this.chkOption1.isChecked() ? 1 : 0);
				paramList.add(ParamName.AztecInverse, this.spnOption4.getSelectedItemPosition());
				break;
			case AustraliaPost:
				paramList.add(ParamName.AustraliaPost, this.chkOption1.isChecked() ? 1 : 0);
				paramList.add(ParamName.AustraliaPostFormat, this.spnOption4.getSelectedItemPosition());
				break;
			case DataMatrix:
				paramList.add(ParamName.DataMatrix, this.chkOption1.isChecked() ? 1 : 0);
				paramList.add(ParamName.DataMatrixInverse, this.spnOption4.getSelectedItemPosition());
				break;
			case K3of5:
				paramList.add(ParamName.K3of5, this.chkOption1.isChecked() ? 1 : 0);
				break;
			case QRCode:
				paramList.add(ParamName.QRCode, this.chkOption1.isChecked() ? 1 : 0);
				paramList.add(ParamName.QRInverse, this.spnOption4.getSelectedItemPosition());
				break;
			case CompositeCCAB:		
			case CompositeCCC:	
			case CompositeTLC39:
				paramList.add(ParamName.CompositeCCC, this.chkOption1.isChecked() ? 1 : 0);
				paramList.add(ParamName.CompositeCCAB, this.chkOption2.isChecked() ? 1 : 0);
				paramList.add(ParamName.CompositeTLC39, this.chkOption3.isChecked() ? 1 : 0);
				paramList.add(ParamName.GS1128EmulMode, this.chkGS1EmulMode.isChecked() ? 1 : 0);
				paramList.add(ParamName.UPCCompositeMode,
						this.adpCompositeMode.getItem(this.spnCompositeMode.getSelectedItemPosition()).getCode());
				paramList.add(ParamName.CompositeBeepMode,
						this.adpCompositeBeepMode.getItem(this.spnCompositeBeepMode.getSelectedItemPosition()).getCode());
				break;
			case GS1128:
				paramList.add(ParamName.GS1128, this.chkOption1.isChecked() ? 1 : 0);
				break;
			case ISSNEAN:
				paramList.add(ParamName.ISSNEAN, this.chkOption1.isChecked() ? 1 : 0);
				break;
			case ISBT128:
				paramList.add(ParamName.ISBT128, this.chkOption1.isChecked() ? 1 : 0);
				break;
			case JapanPostal:
				paramList.add(ParamName.JapanPostal, this.chkOption1.isChecked() ? 1 : 0);
				break;
			case Maxicode:
				paramList.add(ParamName.Maxicode, this.chkOption1.isChecked() ? 1 : 0);
				break;
			case MicroPDF417:
				paramList.add(ParamName.MicroPDF417, this.chkOption1.isChecked() ? 1 : 0);
				break;
			case MicroQR:
				paramList.add(ParamName.MicroQR, this.chkOption1.isChecked() ? 1 : 0);
				break;
			case NetherlandsKIXCode:
				paramList.add(ParamName.NetherlandsKIXCode, this.chkOption1.isChecked() ? 1 : 0);
				break;
			case PDF417:
				paramList.add(ParamName.PDF417, this.chkOption1.isChecked() ? 1 : 0);
				break;
			case USPostnet:
				paramList.add(ParamName.USPostnet, this.chkOption1.isChecked() ? 1 : 0);
				this.chkOption1.setChecked(paramList.getBoolean(ParamName.USPostnet));
				break;
			case USPlanet:
				paramList.add(ParamName.USPlanet, this.chkOption1.isChecked() ? 1 : 0);
				break;
			case USPS4CB:
				paramList.add(ParamName.USPS4CB, this.chkOption1.isChecked() ? 1 : 0);
				break;
			case UPUFICSPostal:
				paramList.add(ParamName.UPUFICSPostal, this.chkOption1.isChecked() ? 1 : 0);
				break;
			case HanXin:
				paramList.add(ParamName.HanXin, this.chkOption1.isChecked() ? 1 : 0);
				paramList.add(ParamName.HanXinInverse,
						this.inverseAdapter.getItem(this.spnOption4.getSelectedItemPosition()).getCode());
				break;
			case C2of5:
				paramList.add(ParamName.C2of5, this.chkOption1.isChecked() ? 1 : 0);
				break;
			default:
				break;
			}
			
			if(getReader().setBarcodeParam(paramList)) {
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

		ParamValueList paramList = getReader().getBarcodeParam(new ParamName[] { ParamName.CompositeCCC,
				ParamName.CompositeCCAB, ParamName.CompositeTLC39, ParamName.GS1128,
				ParamName.ISSNEAN, ParamName.ISBT128, ParamName.JapanPostal, ParamName.Maxicode,
				ParamName.MicroPDF417, ParamName.MicroQR, ParamName.NetherlandsKIXCode, ParamName.PDF417,
				ParamName.USPostnet, ParamName.USPlanet, ParamName.USPS4CB,
				ParamName.UPUFICSPostal, ParamName.Aztec, ParamName.AztecInverse,
				ParamName.AustraliaPost, ParamName.AustraliaPostFormat, ParamName.DataMatrix,
				ParamName.DataMatrixInverse,
				ParamName.K3of5, 
				ParamName.QRCode, ParamName.QRInverse, ParamName.UPCCompositeMode,
				ParamName.CompositeBeepMode, ParamName.HanXin, ParamName.HanXinInverse, ParamName.C2of5,
				ParamName.GS1128EmulMode });

		if ((symbol_name == ParamName.Aztec) || (symbol_name == ParamName.AustraliaPost)
				|| (symbol_name == ParamName.DataMatrix)  || (symbol_name == ParamName.QRCode)
				|| (symbol_name == ParamName.HanXin)) {

			this.chkOption1.setText(R.string.symbol_enable);
			this.chkOption1.setVisibility(View.VISIBLE);
			this.lnlOption.setVisibility(View.VISIBLE);

			switch (symbol_name) {
			case Aztec:
				this.txtOption4.setText("Aztec Inverse");
				this.chkOption1.setChecked(paramList.getBoolean(ParamName.Aztec));
				this.spnOption4.setSelection((Integer) paramList.getValue(ParamName.AztecInverse));
				this.setTitle(R.string.symbol_aztec_name);
				break;
			case AustraliaPost:
				this.txtOption4.setText("Australia Post Format");
				this.chkOption1.setChecked( paramList.getBoolean(ParamName.AustraliaPost));
				this.spnOption4.setSelection((Integer) paramList.getValue(ParamName.AustraliaPostFormat));
				this.setTitle(R.string.symbol_australiapost_name);
				break;
			case DataMatrix:
				this.txtOption4.setText("Data Matrix Inverse");
				this.chkOption1.setChecked( paramList.getBoolean(ParamName.DataMatrix));
				this.spnOption4.setSelection((Integer) paramList.getValue(ParamName.DataMatrixInverse));
				this.setTitle(R.string.symbol_datamatrix_name);
				break;
			case K3of5:
				this.chkOption1.setChecked( paramList.getBoolean(ParamName.K3of5));
				this.setTitle(R.string.symbol_korea3of5_name);
				break;
			case QRCode:
				this.txtOption4.setText("QR Inverse");
				this.chkOption1.setChecked( paramList.getBoolean(ParamName.QRCode));
				this.spnOption4.setSelection((Integer) paramList.getValue(ParamName.QRInverse));
				this.setTitle(R.string.symbol_qrcode_name);
				break;
			case HanXin:
				this.chkOption1.setChecked( paramList.getBoolean(ParamName.HanXin));
				this.txtOption4.setText("Han Xin Inverse");
				this.spnOption4.setSelection(this.inverseAdapter
						.getPosition(InverseType.valueOf((byte)paramList.getValue(ParamName.HanXinInverse))));
				this.setTitle(R.string.symbol_hanxin_name);
				break;
			default:
				break;
			}
		} else if (symbol_name == ParamName.CompositeCCAB || symbol_name == ParamName.CompositeCCC ||
				symbol_name == ParamName.CompositeTLC39) {
			this.chkOption1.setText("Composite CC-C");
			this.chkOption2.setText("Composite CC-AB");
			this.chkOption3.setText("Composite TLC-39");
			this.txtUpcCompositeMode.setText("UPC Composite Mode");
			this.txtCompositeBeepMode.setText("Composite Beep Mode");
			this.chkGS1EmulMode.setText("GS1-128 Emulation Mode for UCC/EAN Composite Codes");
			this.setTitle(R.string.symbol_compostite_name);

			for (UpcCompositeMode item : UpcCompositeMode.values()) {
				this.adpCompositeMode.add(new ValueItem<UpcCompositeMode>(item.toString(), item));
			}
			this.spnCompositeMode.setAdapter(this.adpCompositeMode);

			for (CompositeBeepMode item : CompositeBeepMode.values()) {
				this.adpCompositeBeepMode.add(new ValueItem<CompositeBeepMode>(item.toString(), item));
			}
			this.spnCompositeBeepMode.setAdapter(this.adpCompositeBeepMode);

			this.chkOption1.setVisibility(View.VISIBLE);
			this.chkOption2.setVisibility(View.VISIBLE);
			this.chkOption3.setVisibility(View.VISIBLE);

			this.chkOption1.setChecked( paramList.getBoolean(ParamName.CompositeCCC));
			this.chkOption2.setChecked( paramList.getBoolean(ParamName.CompositeCCAB));
			this.chkOption3.setChecked( paramList.getBoolean(ParamName.CompositeTLC39));

			this.spnCompositeMode.setSelection(this.adpCompositeMode
					.getPosition(UpcCompositeMode.valueOf((byte)paramList.getValue(ParamName.UPCCompositeMode))));

			this.spnCompositeBeepMode.setSelection(this.adpCompositeBeepMode
					.getPosition(CompositeBeepMode.valueOf((byte)paramList.getValue(ParamName.CompositeBeepMode))));

			this.chkGS1EmulMode.setChecked(
					 paramList.getBoolean(ParamName.GS1128EmulMode));

		} else {
			switch (symbol_name) {
			case GS1128:
				this.chkOption1.setChecked( paramList.getBoolean(ParamName.GS1128));
				break;
			case ISSNEAN:
				this.chkOption1.setChecked( paramList.getBoolean(ParamName.ISSNEAN));
				this.setTitle(R.string.symbol_issnean_name);
				break;
			case ISBT128:
				this.chkOption1.setChecked( paramList.getBoolean(ParamName.ISBT128));
				break;
			case JapanPostal:
				this.chkOption1.setChecked( paramList.getBoolean(ParamName.JapanPostal));
				this.setTitle(R.string.symbol_japanpostal_name);
				break;
			case Maxicode:
				this.chkOption1.setChecked( paramList.getBoolean(ParamName.Maxicode));
				this.setTitle(R.string.symbol_maxicode_name);
				break;
			case MicroPDF417:
				this.chkOption1.setChecked( paramList.getBoolean(ParamName.MicroPDF417));
				this.setTitle(R.string.symbol_micropdf417_name);
				break;
			case MicroQR:
				this.chkOption1.setChecked( paramList.getBoolean(ParamName.MicroQR));
				this.setTitle(R.string.symbol_microqr_name);
				break;
			case NetherlandsKIXCode:
				this.chkOption1.setChecked( paramList.getBoolean(ParamName.NetherlandsKIXCode));
				this.setTitle(R.string.symbol_kixcode_name);
				break;
			case PDF417:
				this.chkOption1.setChecked( paramList.getBoolean(ParamName.PDF417));
				this.setTitle(R.string.symbol_pdf417_name);
				break;
			case USPostnet:
				this.chkOption1.setChecked( paramList.getBoolean(ParamName.USPostnet));
				this.setTitle(R.string.symbol_uspostnet_name);
				break;
			case USPlanet:
				this.chkOption1.setChecked( paramList.getBoolean(ParamName.USPlanet));
				this.setTitle(R.string.symbol_usplanet_name);
				break;
			case USPS4CB:
				this.chkOption1
						.setChecked( paramList.getBoolean(ParamName.USPS4CB));
				this.setTitle(R.string.symbol_intelligentmail_name);
				break;
			case UPUFICSPostal:
				this.chkOption1.setChecked( paramList.getBoolean(ParamName.UPUFICSPostal));
				this.setTitle(R.string.symbol_ficspostal_name);
				break;
			case K3of5:
				this.chkOption1.setChecked( paramList.getBoolean(ParamName.K3of5));
				this.setTitle(R.string.symbol_korea3of5_name);
				break;
			case C2of5:
				this.chkOption1.setChecked( paramList.getBoolean(ParamName.C2of5));
				this.setTitle(R.string.symbol_ch2of5_name);
				break;
			default:
				break;
			}
			this.chkOption1.setText(R.string.symbol_enable);
			this.chkOption1.setVisibility(View.VISIBLE);
		}
	}

	private void enableSymbolOptionClear() {
		this.chkOption1.setText("");
		this.chkOption2.setText("");
		this.chkOption3.setText("");
		this.chkOption1.setChecked(false);
		this.chkOption2.setChecked(false);
		this.chkOption3.setChecked(false);
		this.chkOption1.setVisibility(View.GONE);
		this.chkOption2.setVisibility(View.GONE);
		this.chkOption3.setVisibility(View.GONE);
		this.lnlOption.setVisibility(View.GONE);
		this.spnOption4.setSelection(0);
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
