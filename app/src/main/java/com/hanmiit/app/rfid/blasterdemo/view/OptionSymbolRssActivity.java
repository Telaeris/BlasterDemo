package com.hanmiit.app.rfid.blasterdemo.view;

import com.hanmiit.lib.barcode.ParamValueList;
import com.hanmiit.lib.barcode.type.Gs1DataBarLimitedSecurityLevel;
import com.hanmiit.lib.barcode.type.ParamName;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.view.base.RfidActivity;
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
import android.widget.Toast;

public class OptionSymbolRssActivity extends RfidActivity implements
										OnClickListener {

	@SuppressWarnings("unused")
	private static final String TAG = OptionSymbolRssActivity.class.getSimpleName();
	
	@SuppressWarnings("unused")
	private ParamName symbol_name;
	
	private CheckBox chkRss14Enable;
	private CheckBox chkRssLimitedEnable;
	private CheckBox chkRssExpandedEnable;
	private CheckBox chkConvertRssToUpcEanEnable;
	private Spinner spnRssSecurityLevel;
	private ValueAdapter<Gs1DataBarLimitedSecurityLevel> adpRssSecurityLevel;
	private Button btnSetOption;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_option_symbol_rss);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		this.setTitle(R.string.symbol_rss_name);
		
		// Create Reader
		this.createReader();
		
		// Initialize Widget
		this.initWidgets();

		// Initialize Reader
		this.initReader();
		
		this.symbol_name = (ParamName)getIntent().getSerializableExtra("Symbol_Name");
		
		this.adpRssSecurityLevel = new ValueAdapter<Gs1DataBarLimitedSecurityLevel>(this);
		for (Gs1DataBarLimitedSecurityLevel item : Gs1DataBarLimitedSecurityLevel.values()) {
			this.adpRssSecurityLevel.add(new ValueItem<Gs1DataBarLimitedSecurityLevel>(item.toString(), item));
		}
		this.spnRssSecurityLevel = (Spinner)findViewById(R.id.rss_limited_security_level);
		this.spnRssSecurityLevel.setAdapter(adpRssSecurityLevel);
		
		this.chkRss14Enable = (CheckBox)findViewById(R.id.rss_14_enable);
		this.chkRssLimitedEnable = (CheckBox)findViewById(R.id.rss_limited_enable);
		this.chkRssExpandedEnable = (CheckBox)findViewById(R.id.rss_expanded_enable);
		this.chkConvertRssToUpcEanEnable = (CheckBox)findViewById(R.id.convert_rss_to_upcean);
		
		this.btnSetOption = (Button) findViewById(R.id.set_option);
		this.btnSetOption.setOnClickListener(this);
		
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
			
			paramList.add(ParamName.GS1Databar, this.chkRss14Enable.isChecked() ? 1 : 0);
			paramList.add(ParamName.GS1DatabarLimited, this.chkRssLimitedEnable.isChecked() ? 1 : 0);
			paramList.add(ParamName.GS1DatabarExpanded, this.chkRssExpandedEnable.isChecked() ? 1 : 0);
			paramList.add(ParamName.ConvertGS1DatabarToUPCEAN, this.chkConvertRssToUpcEanEnable.isChecked() ? 1 : 0);
			paramList.add(ParamName.GS1DatabarLimitedSecurityLevel, 
									this.adpRssSecurityLevel.getItem(this.spnRssSecurityLevel.getSelectedItemPosition()).getCode());
			
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

	private void initSymbolStateList() {

		ParamValueList paramList = getReader().getBarcodeParam(new ParamName[]
				{ ParamName.GS1Databar, ParamName.GS1DatabarLimited, ParamName.GS1DatabarExpanded, 
				  ParamName.GS1DatabarLimitedSecurityLevel, ParamName.ConvertGS1DatabarToUPCEAN
				}
		);
		
		this.chkRss14Enable.setChecked(paramList.getBoolean(ParamName.GS1Databar));
		this.chkRssLimitedEnable.setChecked(paramList.getBoolean(ParamName.GS1DatabarLimited));
		this.chkRssExpandedEnable.setChecked(paramList.getBoolean(ParamName.GS1DatabarExpanded));
		this.chkConvertRssToUpcEanEnable.setChecked(paramList.getBoolean(ParamName.ConvertGS1DatabarToUPCEAN));
		this.spnRssSecurityLevel.setSelection(this.adpRssSecurityLevel.getPosition(Gs1DataBarLimitedSecurityLevel.valueOf((byte)paramList.getValue(ParamName.GS1DatabarLimitedSecurityLevel))));
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
