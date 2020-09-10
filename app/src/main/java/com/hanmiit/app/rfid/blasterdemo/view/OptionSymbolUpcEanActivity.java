package com.hanmiit.app.rfid.blasterdemo.view;

import com.hanmiit.app.rfid.blasterdemo.view.base.RfidActivity;
import com.hanmiit.lib.barcode.ParamValueList;
import com.hanmiit.lib.barcode.type.BooklandIsbnFormat;
import com.hanmiit.lib.barcode.type.CouponReportFormat;
import com.hanmiit.lib.barcode.type.DecodeUpcEanSupplementals;
import com.hanmiit.lib.barcode.type.DecodeUpcSupplementalsAIMID;
import com.hanmiit.lib.barcode.type.ParamName;
import com.hanmiit.lib.barcode.type.Preamble;
import com.hanmiit.lib.barcode.type.UpcEanSecurityLevel;

import java.util.Locale;

import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.adapter.StringAdapter;
import com.hanmiit.app.rfid.blasterdemo.adapter.ValueAdapter;
import com.hanmiit.app.rfid.blasterdemo.adapter.ValueItem;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class OptionSymbolUpcEanActivity extends RfidActivity implements
		OnClickListener {

	@SuppressWarnings("unused")
	private static final String TAG = OptionSymbolUpcEanActivity.class.getSimpleName();

	private CheckBox chkTransmitUpcA;
	private CheckBox chkTransmitUpcE;
	private CheckBox chkTransmitUpcE1;
	private CheckBox chkConvertUpcE;
	private CheckBox chkConvertUpcE1;
	private CheckBox chkEanZeroExtend;
	private CheckBox chkConvertEan8toEan13;
	private CheckBox chkUccCoupon;
	private CheckBox chkBooklandEan;
	private CheckBox chkIssnEan;
	private CheckBox chkReducedQuietZone;
	private Spinner spnDecode;
	private Spinner spnDecodeRedundancy;
	private TextView txtLevel;
	private Spinner spnLevel;
	private Spinner spnUpcA;
	private Spinner spnUpcE;
	private Spinner spnUpcE1;
	private Spinner spnBooklandIsbnFormat;
	private Spinner spnCouponReportFormat;
	private Spinner spnUpcAimIdFormat;
	private Button btnSetOption;
	
	private ValueAdapter<DecodeUpcEanSupplementals> adpDecode;
	private StringAdapter adpDecodeRedundancy;
	private ValueAdapter<UpcEanSecurityLevel> adpLevel;
	private ValueAdapter<Preamble> adpUpcA;
	private ValueAdapter<Preamble> adpUpcE;
	private ValueAdapter<Preamble> adpUpcE1;
	private ValueAdapter<BooklandIsbnFormat> adpBooklandIsbnFormat;
	private ValueAdapter<CouponReportFormat> adpCouponReportFormat;
	private ValueAdapter<DecodeUpcSupplementalsAIMID> adpUpcAimIdFormat;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_option_symbol_upc_ean);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		this.setTitle(R.string.symbol_upc_ean_name);

		// Create Reader
		this.createReader();
		
		// Initialize Widget
		this.initWidgets();

		// Initialize Reader
		this.initReader();

		// Initialize Widgets
		this.chkTransmitUpcA = (CheckBox) findViewById(R.id.transmit_upc_a_check_digit);
		this.chkTransmitUpcE = (CheckBox) findViewById(R.id.transmit_upc_e_check_digit);
		this.chkTransmitUpcE1 = (CheckBox) findViewById(R.id.transmit_upc_e1_check_digit);
		this.chkConvertUpcE = (CheckBox) findViewById(R.id.convert_upc_e_to_upc_a);
		this.chkConvertUpcE1 = (CheckBox) findViewById(R.id.convert_upc_e1_to_upc_a);
		this.chkEanZeroExtend = (CheckBox) findViewById(R.id.ean_zero_extend);
		this.chkConvertEan8toEan13 = (CheckBox) findViewById(R.id.convert_ean_8_to_ean_13_type);
		this.chkUccCoupon = (CheckBox) findViewById(R.id.ucc_coupon_extended_code);
		this.chkBooklandEan = (CheckBox) findViewById(R.id.bookland_ean);
		this.chkIssnEan = (CheckBox) findViewById(R.id.issn_ean);
		this.chkReducedQuietZone = (CheckBox) findViewById(R.id.upc_reducec_quiet_zone);
		this.spnDecode = (Spinner) findViewById(R.id.decode_upc_ean_supplement);
		this.adpDecode = new ValueAdapter<DecodeUpcEanSupplementals>(this);
		for (DecodeUpcEanSupplementals item : DecodeUpcEanSupplementals
				.values()) {
			this.adpDecode.add(new ValueItem<DecodeUpcEanSupplementals>(item
					.toString(), item));
		}
		this.adpDecode.notifyDataSetChanged();
		this.spnDecode.setAdapter(this.adpDecode);
		this.spnDecodeRedundancy = (Spinner) findViewById(R.id.decode_upc_ean_supplement_redundancy);
		this.adpDecodeRedundancy = new StringAdapter(this,
				R.array.decode_upc_ean_supplemental_redundancy);
		this.spnDecodeRedundancy.setAdapter(this.adpDecodeRedundancy);
		this.txtLevel = (TextView) findViewById(R.id.upc_ean_security_level_txt);
		this.spnLevel = (Spinner) findViewById(R.id.upc_ean_security_level);
		this.adpLevel = new ValueAdapter<UpcEanSecurityLevel>(this);
		for (UpcEanSecurityLevel item : UpcEanSecurityLevel.values()) {
			this.adpLevel.add(new ValueItem<UpcEanSecurityLevel>(item
					.toString(), item));
		}
		this.spnLevel.setAdapter(this.adpLevel);
		this.spnUpcA = (Spinner) findViewById(R.id.upc_a_preamble);
		this.adpUpcA = new ValueAdapter<Preamble>(this);
		for (Preamble item : Preamble.values()) {
			this.adpUpcA.add(new ValueItem<Preamble>(item.toString(), item));
		}
		this.spnUpcA.setAdapter(this.adpUpcA);
		this.spnUpcE = (Spinner) findViewById(R.id.upc_e_preamble);
		this.adpUpcE = new ValueAdapter<Preamble>(this);
		for (Preamble item : Preamble.values()) {
			this.adpUpcE.add(new ValueItem<Preamble>(item.toString(), item));
		}
		this.spnUpcE.setAdapter(this.adpUpcE);
		this.spnUpcE1 = (Spinner) findViewById(R.id.upc_e1_preamble);
		this.adpUpcE1 = new ValueAdapter<Preamble>(this);
		for (Preamble item : Preamble.values()) {
			this.adpUpcE1.add(new ValueItem<Preamble>(item.toString(), item));
		}
		this.spnUpcE1.setAdapter(this.adpUpcE1);
		this.btnSetOption = (Button) findViewById(R.id.set_option);
		this.btnSetOption.setOnClickListener(this);
		
		this.spnBooklandIsbnFormat = (Spinner) findViewById(R.id.bookland_isbn_format);
		this.adpBooklandIsbnFormat = new ValueAdapter<BooklandIsbnFormat>(this);
		for (BooklandIsbnFormat item : BooklandIsbnFormat.values()) {
			this.adpBooklandIsbnFormat.add(new ValueItem<BooklandIsbnFormat>(item.toString(), item));
		}
		this.spnBooklandIsbnFormat.setAdapter(this.adpBooklandIsbnFormat);
		
		this.spnCouponReportFormat = (Spinner) findViewById(R.id.coupon_report_format);
		this.adpCouponReportFormat = new ValueAdapter<CouponReportFormat>(this);
		for (CouponReportFormat item : CouponReportFormat.values()) {
			this.adpCouponReportFormat.add(new ValueItem<CouponReportFormat>(item.toString(), item));
		}
		this.spnCouponReportFormat.setAdapter(this.adpCouponReportFormat);
		
		this.spnUpcAimIdFormat = (Spinner) findViewById(R.id.upc_supplemental_aim_id_format);
		this.adpUpcAimIdFormat = new ValueAdapter<DecodeUpcSupplementalsAIMID>(this);
		for (DecodeUpcSupplementalsAIMID item : DecodeUpcSupplementalsAIMID.values()) {
			this.adpUpcAimIdFormat.add(new ValueItem<DecodeUpcSupplementalsAIMID>(item.toString(), item));
		}
		this.spnUpcAimIdFormat.setAdapter(this.adpUpcAimIdFormat);

		// Load Scanner Symbol Detail Option
		chkConvertEan8toEan13.setVisibility(View.GONE); //Not Support
		txtLevel.setVisibility(View.GONE);
		spnLevel.setVisibility(View.GONE);//Not Support
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
			paramList.add(ParamName.TransmitUPCACheckDigit,
					this.chkTransmitUpcA.isChecked() ? 1 : 0);
			paramList.add(ParamName.TransmitUPCECheckDigit,
					this.chkTransmitUpcE.isChecked() ? 1 : 0);
			paramList.add(ParamName.TransmitUPCE1CheckDigit,
					this.chkTransmitUpcE1.isChecked() ? 1 : 0);
			paramList.add(ParamName.ConvertUPCEtoA,
					this.chkConvertUpcE.isChecked() ? 1 : 0);
			paramList.add(ParamName.ConvertUPCE1toA,
					this.chkConvertUpcE1.isChecked() ? 1 : 0);
			paramList.add(ParamName.EAN8Extend,
					this.chkEanZeroExtend.isChecked() ? 1 : 0);
			paramList.add(ParamName.UCCCouponExtendCode,
					this.chkUccCoupon.isChecked() ? 1 : 0);
			paramList.add(ParamName.DecodeUPCEANSupply, DecodeUpcEanSupplementals
					.valueOf((byte) this.spnDecode.getSelectedItemPosition()).getCode());
			paramList.add(ParamName.UPCEANSupplyRedundancy,
					this.spnDecodeRedundancy.getSelectedItemPosition());
			paramList.add(ParamName.UPCAPreamble, Preamble
					.valueOf((byte) this.spnUpcA.getSelectedItemPosition()).getCode());
			paramList.add(ParamName.UPCEPreamble, Preamble
					.valueOf((byte) this.spnUpcE.getSelectedItemPosition()).getCode());
			paramList.add(ParamName.UPCE1Preamble, Preamble
					.valueOf((byte) this.spnUpcE1.getSelectedItemPosition()).getCode());
			paramList.add(ParamName.ISSNEAN, this.chkIssnEan.isChecked() ? 1 : 0);
			paramList.add(ParamName.UPCReducedQuietZone, this.chkReducedQuietZone.isChecked() ? 1 : 0);
			paramList.add(ParamName.BooklandEAN, this.chkBooklandEan.isChecked() ? 1 : 0);
			paramList.add(ParamName.BooklandISBNFormat, BooklandIsbnFormat
					.valueOf((byte) this.spnBooklandIsbnFormat.getSelectedItemPosition()).getCode());
			paramList.add(ParamName.CouponReport, CouponReportFormat
					.valueOf((byte) this.spnCouponReportFormat.getSelectedItemPosition()).getCode());
			paramList.add(ParamName.DecodeUPCEANSupplyAIMID, DecodeUpcSupplementalsAIMID
					.valueOf((byte) this.spnUpcAimIdFormat.getSelectedItemPosition()).getCode());
			
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
				ParamName.DecodeUPCEANSupply,
				ParamName.UPCEANSupplyRedundancy,
				ParamName.TransmitUPCACheckDigit,
				ParamName.TransmitUPCECheckDigit,
				ParamName.TransmitUPCE1CheckDigit,
				ParamName.UPCAPreamble, ParamName.UPCEPreamble,
				ParamName.UPCE1Preamble, ParamName.ConvertUPCEtoA,
				ParamName.ConvertUPCE1toA, ParamName.EAN8Extend,
				ParamName.UCCCouponExtendCode,
				ParamName.ISSNEAN, ParamName.UPCReducedQuietZone,
				ParamName.BooklandEAN,
				ParamName.BooklandISBNFormat,
				ParamName.CouponReport,
				ParamName.DecodeUPCEANSupplyAIMID,
				ParamName.UserProgrammableSupply1,
				ParamName.UserProgrammableSupply2});
		this.chkTransmitUpcA.setChecked(paramList.getBoolean(ParamName.TransmitUPCACheckDigit));
		this.chkTransmitUpcE.setChecked(paramList.getBoolean(ParamName.TransmitUPCECheckDigit));
		this.chkTransmitUpcE1.setChecked(paramList.getBoolean(ParamName.TransmitUPCE1CheckDigit));
		this.chkConvertUpcE.setChecked(paramList.getBoolean(ParamName.ConvertUPCEtoA));
		this.chkConvertUpcE1.setChecked(paramList.getBoolean(ParamName.ConvertUPCE1toA));
		this.chkEanZeroExtend.setChecked(paramList.getBoolean(ParamName.EAN8Extend));
		this.chkUccCoupon.setChecked(paramList.getBoolean(ParamName.UCCCouponExtendCode));
		this.chkIssnEan.setChecked(paramList.getBoolean(ParamName.ISSNEAN));
		this.chkReducedQuietZone.setChecked(paramList.getBoolean(ParamName.UPCReducedQuietZone));
		this.chkBooklandEan.setChecked(paramList.getBoolean(ParamName.BooklandEAN));
		
		this.spnDecode.setSelection(this.adpDecode
				.getPosition(DecodeUpcEanSupplementals.valueOf( (byte)paramList.getValue(ParamName.DecodeUPCEANSupply))));
		
		this.spnDecodeRedundancy.setSelection(paramList.getValue(ParamName.UPCEANSupplyRedundancy));
		this.spnUpcA.setSelection(this.adpUpcA.getPosition(Preamble.valueOf((byte) paramList.getValue(ParamName.UPCAPreamble))));
		this.spnUpcE.setSelection(this.adpUpcE.getPosition(Preamble.valueOf((byte) paramList.getValue(ParamName.UPCEPreamble))));
		this.spnUpcE1.setSelection(this.adpUpcE1
				.getPosition(Preamble.valueOf( (byte)paramList.getValue(ParamName.UPCE1Preamble))));
		
		this.spnBooklandIsbnFormat.setSelection(this.adpBooklandIsbnFormat.getPosition(BooklandIsbnFormat.valueOf((byte) paramList.getValue(ParamName.BooklandISBNFormat))));
		this.spnCouponReportFormat.setSelection(this.adpCouponReportFormat.getPosition(CouponReportFormat.valueOf((byte) paramList.getValue(ParamName.CouponReport))));
		this.spnUpcAimIdFormat.setSelection(this.adpUpcAimIdFormat.getPosition(DecodeUpcSupplementalsAIMID.valueOf((byte) paramList.getValue(ParamName.DecodeUPCEANSupplyAIMID))));
		
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
