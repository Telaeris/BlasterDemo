package com.hanmiit.app.rfid.blasterdemo.view;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

//import com.atid.lib.dev.ATScanManager;
//import com.atid.lib.dev.ATScanner;
//import com.atid.lib.dev.barcode.motorola.param.SSIParamName;
//import com.atid.lib.dev.barcode.motorola.param.SSIParamValueList;
//import com.atid.lib.dev.barcode.params.ATScanSE4710Parameter;
//import com.atid.lib.diagnostics.ATLog;
import com.hanmiit.lib.barcode.type.RedundancyLevel;
//import com.atid.lib.dev.barcode.motorola.type.SSIScanAngleType;
//import com.atid.lib.dev.barcode.motorola.type.SSIBeepVolumeType;
import com.hanmiit.lib.barcode.ParamValueList;
import com.hanmiit.lib.barcode.type.InverseType;
import com.hanmiit.lib.barcode.type.ParamName;
import com.hanmiit.lib.barcode.type.SecurityLevel;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.adapter.ValueAdapter;
import com.hanmiit.app.rfid.blasterdemo.adapter.ValueItem;
import com.hanmiit.app.rfid.blasterdemo.view.base.RfidActivity;

import android.annotation.SuppressLint;
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

public class OptionGeneralConfigActivity extends RfidActivity implements
		OnClickListener {

	@SuppressWarnings("unused")
	private static final String TAG = OptionGeneralConfigActivity.class.getSimpleName();

	private Spinner spnLevel;
	private Spinner spnSecurityLevel;
	
//	private CheckBox chkBidirect;
//	private Spinner spnScanAngle;
//	private Spinner spnLaserOnTime;
//	private Spinner spnAimDuration;
//	private Spinner spnTimeout;
//	private CheckBox chkBeep;
//	private Spinner spnBeepVolume;
	private Button btnSetOption;
	private Spinner spnInverse1D;
	private Spinner spnCharSet;

	@SuppressWarnings("unused")
	private TextView txtLevel;
	@SuppressWarnings("unused")
	private TextView txtSecurityLevel;
	private LinearLayout scanAngle_lnl;
	private LinearLayout aimDuration_lnl;
	
	private ValueAdapter<RedundancyLevel> adpLevel;
	private ValueAdapter<SecurityLevel> adpSecurityLevel;
	
	
	//private ValueAdapter<SSIScanAngleType> adpScanAngle;
	private ValueAdapter<Float> adpLaserOnTime;
	private ValueAdapter<Float> adpAimDuration;
	private ValueAdapter<Float> adpTimeout;
	//private ValueAdapter<SSIBeepVolumeType> adpBeepVolume;
	private ValueAdapter<InverseType> adpInverse1D;
	private ValueAdapter<String> adpCharSet;

	private static final int MIN_LASER_ON_TIME = 5;
	private static final int MAX_LASER_ON_TIME = 99;
	private static final int MIN_AIM_DURATION = 0;
	private static final int MAX_AIM_DURATION = 99;
	private static final int MIN_TIMEOUT_BETWEEN_SAME_SYMBOL = 0;
	private static final int MAX_TIMEOUT_BETWEEN_SAME_SYMBOL = 99;

	@SuppressWarnings("rawtypes")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_option_general_config);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Create Reader
		this.createReader();
		
		// Initialize Widget
		this.initWidgets();

		// Initialize Reader
		this.initReader();

		// Initialize Widgets
		this.spnLevel = (Spinner) findViewById(R.id.linear_code_type_security_level);
		this.adpLevel = new ValueAdapter<RedundancyLevel>(this);
		for (RedundancyLevel item : RedundancyLevel
				.values()) {
			this.adpLevel.add(new ValueItem<RedundancyLevel>(item
					.toString(), item));
		}
		this.spnLevel.setAdapter(this.adpLevel);
		
		this.spnSecurityLevel = (Spinner) findViewById(R.id.security_level);
		this.adpSecurityLevel = new ValueAdapter<SecurityLevel>(this);
		for(SecurityLevel item: SecurityLevel.values()){
			this.adpSecurityLevel.add(new ValueItem<SecurityLevel>(item.toString(), item));			
		}
		this.spnSecurityLevel.setAdapter(this.adpSecurityLevel);
		////////////////////
		
		

//		this.chkBidirect = (CheckBox) findViewById(R.id.bidirectional_redundancy);

//		this.spnScanAngle = (Spinner) findViewById(R.id.scan_angle);
//		this.adpScanAngle = new ValueAdapter<SSIScanAngleType>(this);
//		for (SSIScanAngleType item : SSIScanAngleType.values()) {
//			this.adpScanAngle.add(new ValueItem<SSIScanAngleType>(item.toString(),
//					item));
//		}
//		this.spnScanAngle.setAdapter(this.adpScanAngle);

//		this.spnLaserOnTime = (Spinner) findViewById(R.id.laser_on_time);
//		this.adpLaserOnTime = new ValueAdapter<Float>(this);
//		this.adpLaserOnTime
//				.add(new ValueItem<Float>("Not Used", (float) 25.5F));
//		for (int i = MIN_LASER_ON_TIME; i <= MAX_LASER_ON_TIME; i++) {
//			this.adpLaserOnTime.add(new ValueItem<Float>(String.format(Locale.US, 
//					"%.1f sec", (float) i / 10.0F), (float) i / 10.0F));
//		}
//		this.spnLaserOnTime.setAdapter(this.adpLaserOnTime);
//
//		this.spnAimDuration = (Spinner) findViewById(R.id.aim_duration);
//		this.adpAimDuration = new ValueAdapter<Float>(this);
//		for (int i = MIN_AIM_DURATION; i <= MAX_AIM_DURATION; i++) {
//			this.adpAimDuration.add(new ValueItem<Float>(String.format(Locale.US, 
//					"%.1f sec", (float) i / 10.0F), (float) i / 10.0F));
//		}
//		this.spnAimDuration.setAdapter(this.adpAimDuration);
//
//		this.spnTimeout = (Spinner) findViewById(R.id.time_out_between_same_symbol);
//		this.adpTimeout = new ValueAdapter<Float>(this);
//		for (int i = MIN_TIMEOUT_BETWEEN_SAME_SYMBOL; i <= MAX_TIMEOUT_BETWEEN_SAME_SYMBOL; i++) {
//			this.adpTimeout.add(new ValueItem<Float>(String.format(Locale.US, "%.1f sec",
//					(float) i / 10.0F), (float) i / 10.0F));
//		}
//		this.spnTimeout.setAdapter(this.adpTimeout);
//
//		this.chkBeep = (CheckBox) findViewById(R.id.beep_after_good_decode);
//		this.spnBeepVolume = (Spinner) findViewById(R.id.beeper_volume);
//		this.adpBeepVolume = new ValueAdapter<SSIBeepVolumeType>(this);
//		for (SSIBeepVolumeType item : SSIBeepVolumeType.values()) {
//			this.adpBeepVolume.add(new ValueItem<SSIBeepVolumeType>(item.toString(),
//					item));
//		}
//		this.spnBeepVolume.setAdapter(this.adpBeepVolume);
		
		this.spnInverse1D = (Spinner)findViewById(R.id.inverse_1d);
		adpInverse1D = new ValueAdapter<InverseType>(this);
		for(InverseType item : InverseType.values()) {
			this.adpInverse1D.add(new ValueItem<InverseType>(item.toString(), item));
		}
		this.spnInverse1D.setAdapter(this.adpInverse1D);
		
		this.txtLevel = (TextView) findViewById(R.id.linear_code_type_security_level_txt);
		this.txtSecurityLevel = (TextView) findViewById(R.id.security_level_text);
		this.btnSetOption = (Button) findViewById(R.id.set_option);
		this.btnSetOption.setOnClickListener(this);
		
		this.spnCharSet = (Spinner)findViewById(R.id.char_set);
		this.adpCharSet = new ValueAdapter<String>(this);
		SortedMap<String, Charset> charSets = Charset.availableCharsets();
		Set s = charSets.entrySet();
		Iterator i = s.iterator();
		while(i.hasNext()) {
			Map.Entry m = (Map.Entry)i.next();
			String key = (String)m.getKey();
			Charset value = (Charset)m.getValue();
			//ATLog.d(TAG, " -> %s", value.name());
			this.adpCharSet.add(new ValueItem<String>(value.name(), key));
		}
		this.spnCharSet.setAdapter(this.adpCharSet);

		

		
		
		
//		scanAngle_lnl = (LinearLayout) findViewById(R.id.scan_angle_lnl);
		//aimDuration_lnl = (LinearLayout) findViewById(R.id.aim_duration_lnl);
		//txtLevel.setVisibility(View.GONE);
		//this.spnLevel.setVisibility(View.GONE);
//		this.chkBidirect.setVisibility(View.GONE);
//		this.scanAngle_lnl.setVisibility(View.GONE);
		//this.aimDuration_lnl.setVisibility(View.GONE);
		
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

			paramList.add(ParamName.RedundancyLevel,
					this.adpLevel.getItem(this.spnLevel
							.getSelectedItemPosition()).getCode());
			paramList.add(ParamName.SecurityLevel, this.adpSecurityLevel.getItem(this.spnSecurityLevel.getSelectedItemPosition()).getCode());
			
//			paramList.add(ParamName.DecodeSessionTimeout, this.adpLaserOnTime.getItem(this.spnLaserOnTime.getSelectedItemPosition()));
//			paramList.add(ParamName.TimeoutBetweenSameSymbol, this.adpTimeout
//					.getItem(this.spnTimeout.getSelectedItemPosition()));
//			paramList.add(ParamName.BeepAfterGoodDecode,
//					this.chkBeep.isChecked() ? 1 : 0);
//			paramList.add(ParamName.BeeperVolume, this.adpBeepVolume
//					.getItem(this.spnBeepVolume.getSelectedItemPosition()));
			
			paramList.add(ParamName.Inverse1D, this.adpInverse1D
					.getItem(this.spnInverse1D.getSelectedItemPosition()).getCode());
			
			//this.mScanner.setCharSetName(this.adpCharSet.getItem(this.spnCharSet.getSelectedItemPosition()));
			getReader().setCharset(Charset.forName(this.adpCharSet.getItem(this.spnCharSet.getSelectedItemPosition())));

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
				ParamName.RedundancyLevel,
				ParamName.SecurityLevel,
//				ParamName.DecodeSessionTimeout,
//				ParamName.TimeoutBetweenSameSymbol,
//				ParamName.BeepAfterGoodDecode, ParamName.BeeperVolume,
				ParamName.Inverse1D});
		this.spnLevel.setSelection(this.adpLevel
				.getPosition(RedundancyLevel.valueOf((byte)paramList.getValue(ParamName.RedundancyLevel))));
		this.spnSecurityLevel.setSelection(this.adpSecurityLevel
				.getPosition(SecurityLevel.valueOf((byte)paramList.getValue(ParamName.SecurityLevel))));
//		this.spnLaserOnTime.setSelection(this.adpLaserOnTime
//				.getPosition((float) paramList.getValue(ParamName.DecodeSessionTimeout)));
//		this.spnTimeout.setSelection(this.adpTimeout
//				.getPosition((Float) paramList.getValueAt(ParamName.TimeoutBetweenSameSymbol)));
//		this.chkBeep.setChecked(paramList.getBoolean(ParamName.BeepAfterGoodDecode));
//		this.spnBeepVolume.setSelection(this.adpBeepVolume
//				.getPosition((SSIBeepVolumeType) paramList.getValueAt(ParamName.BeeperVolume)));
		this.spnInverse1D.setSelection(
				this.adpInverse1D.getPosition(
						InverseType.valueOf((byte) paramList.getValue(ParamName.Inverse1D))
				)
		);
		
		this.spnCharSet.setSelection(
				this.adpCharSet.getPosition(
						getReader().getCharset().name()
				)
		);
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
