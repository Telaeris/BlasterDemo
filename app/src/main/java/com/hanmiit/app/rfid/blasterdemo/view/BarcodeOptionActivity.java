package com.hanmiit.app.rfid.blasterdemo.view;

import com.hanmiit.lib.barcode.ParamValue;
import com.hanmiit.lib.barcode.ParamValueList;
import com.hanmiit.lib.barcode.type.ParamName;
import com.hanmiit.lib.diagnostics.ATLog;
import com.hanmiit.lib.rfid.exception.ATRfidReaderException;
import com.hanmiit.lib.rfid.type.OperationMode;
import com.hanmiit.lib.rfid.type.ResultCode;
import com.hanmiit.lib.util.Version;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.adapter.BarcodeSymbolListAdapter;
import com.hanmiit.app.rfid.blasterdemo.dialog.WaitDialog;
import com.hanmiit.app.rfid.blasterdemo.view.base.RfidActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

public class BarcodeOptionActivity extends RfidActivity implements OnClickListener, OnItemClickListener {

	private static final String TAG = BarcodeOptionActivity.class.getSimpleName();

	private ListView lstSymbols;
	private Button btnDefault;
	private Button btnDisableAll;
	private Button btnEnableAll;
	private Button btnSave;
	private Button btnGeneral;

	private BarcodeSymbolListAdapter adpSymbols;
	
	private boolean isInitialized;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_barcode_option);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		ATLog.i(TAG, "INFO. onCreate()");

		isInitialized = false;
		
		// Create Reader
		this.createReader();

		// Initialize Widget
		this.initWidgets();

		// Initialize Reader
		this.initReader();

		// Enable action widget
		this.enableActionWidgets(false);

		WaitDialog.show(this, R.string.barcode_mode_change);
		new Thread(mChangeBarcodeMode).start();
	}

	@Override
	protected void onDestroy() {

		ATLog.i(TAG, "INFO. onDestroy()");
		
		// Disable Barcode Mode
//		getReader().setBarcodeMode(false);

		// Destroy Reader
		destroyReader();

		super.onDestroy();
	}

//	@Override
//	protected void onStart() {
//		super.onStart();
//
//		if (isInitialized) {
//			// Set Key Action
//			try {
//				getReader().setUseKeyAction(false);
//			} catch (ATRfidReaderException e) {
//				ATLog.e(TAG, e, "ERROR. onStart() - Failed to set use key action");
//			}
//		}
//		
//		ATLog.i(TAG, "INFO. onStart()");
//	}
//
//	@Override
//	protected void onStop() {
//		
//		// Set Key Action
//		try {
//			getReader().setUseKeyAction(false);
//		} catch (ATRfidReaderException e) {
//			ATLog.e(TAG, e, "ERROR. onStop() - Failed to set use key action");
//		}
//
//		ATLog.i(TAG, "INFO. onStop()");
//		super.onStop();
//	}
	
	@Override
	protected void onResume() {
		super.onResume();

		if (isInitialized) {
			// Set Key Action
			try {
				getReader().setUseKeyAction(false);
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. onResume() - Failed to set use key action");
			}
		}
		
		ATLog.i(TAG, "INFO. onResume()");
	}

	@Override
	protected void onPause() {
		// Set Key Action
		try {
			getReader().setUseKeyAction(false);
		} catch (ATRfidReaderException e) {
			ATLog.e(TAG, e, "ERROR. onPause() - Failed to set use key action");
		}

		ATLog.i(TAG, "INFO. onPause()");
		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		ATLog.i(TAG, "INFO. onActivityResult(%s, %s)", getRequestCode(requestCode), getResultCode(resultCode));

		enableActionWidgets(true);
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == RESULT_OK) {
			WaitDialog.show(this, R.string.barcode_mode_change);
			new Thread(mChangeBarcodeMode).start();
		}
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.default_all_symbologies:
			adpSymbols.defaultAll();
			break;
		case R.id.disable_all_symbologies:
			adpSymbols.disableAll();
			break;
		case R.id.enable_all_symbologies:
			adpSymbols.enableAll();
			break;
		case R.id.general_configuration:
			Intent intent = new Intent(this, OptionGeneralConfigActivity.class);
			startActivityForResult(intent, 1);
			break;
		case R.id.save_symbologies:
			enableActionWidgets(false);
			WaitDialog.show(this, R.string.baroce_save_symbols);
			new Thread(mSaveSymbol).start();
			break;
		}
	}

	@Override
	protected void initWidgets() {
		lstSymbols = (ListView) findViewById(R.id.symbol_list);
		lstSymbols.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		adpSymbols = new BarcodeSymbolListAdapter(this);
		lstSymbols.setAdapter(adpSymbols);
		lstSymbols.setOnItemClickListener(this);
		btnDefault = (Button) findViewById(R.id.default_all_symbologies);
		btnDefault.setOnClickListener(this);
		btnDisableAll = (Button) findViewById(R.id.disable_all_symbologies);
		btnDisableAll.setOnClickListener(this);
		btnEnableAll = (Button) findViewById(R.id.enable_all_symbologies);
		btnEnableAll.setOnClickListener(this);
		btnSave = (Button) findViewById(R.id.save_symbologies);
		btnSave.setOnClickListener(this);
		btnGeneral = (Button)findViewById(R.id.general_configuration);
		btnGeneral.setOnClickListener(this);
	}

	@Override
	protected void enableActionWidgets(boolean enabled) {
		lstSymbols.setEnabled(enabled);
		btnDefault.setEnabled(enabled);
		btnDisableAll.setEnabled(enabled);
		btnEnableAll.setEnabled(enabled);
		btnSave.setEnabled(enabled);
		btnGeneral.setEnabled(enabled);
	}

	@Override
	protected void initReader() {
	}

	private Runnable mChangeBarcodeMode = new Runnable() {

		@Override
		public void run() {
			isInitialized = false;
			
			ResultCode res = ResultCode.NoError;
			ParamValueList values = null;

			ATLog.d(TAG, "DEBUG. Load Symbologies");

			try {
				boolean result = true;
				if (Version.isLaster(Version.V7_2_5_13)) {
					try {
						getReader().setOperationMode(OperationMode.Barcode);
					} catch (ATRfidReaderException e) {
						ATLog.e(TAG, "ERROR. mChangeBarcodeMode.$run() - Failed to change barcode mode [%s]", e.getResultCode());
						result = false;
					}
				} else {
					if ((res = getReader().setBarcodeMode(true,0)) != ResultCode.NoError) {
						ATLog.e(TAG, "ERROR. mChangeBarcodeMode.$run() - Failed to change barcode mode [%s]", res);
						result = false;
					}
				}
				if (!result) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							WaitDialog.hide();

							AlertDialog.Builder alert = new AlertDialog.Builder(BarcodeOptionActivity.this);
							alert.setTitle(R.string.system_alert);
							alert.setIcon(android.R.drawable.ic_dialog_alert);
							alert.setMessage(R.string.failed_activate_barcode);
							alert.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									finish();
								}
							});
							alert.show();
							isInitialized = false;
						}

					});
					return;
				}

				values = getReader().getBarcodeParam(new ParamName[] { ParamName.UPCA, ParamName.UPCE, ParamName.UPCE1,
						ParamName.EAN8, ParamName.EAN13, ParamName.Code128, ParamName.Code39, ParamName.Code93,
						ParamName.Code11, ParamName.I2of5, ParamName.D2of5, ParamName.Codabar, ParamName.MSI,
						ParamName.C2of5, ParamName.M2of5, ParamName.K3of5, ParamName.USPostnet, ParamName.USPlanet,
						ParamName.UKPostal, ParamName.JapanPostal, ParamName.AustraliaPost, ParamName.NetherlandsKIXCode,
						ParamName.USPS4CB, ParamName.UPUFICSPostal, ParamName.GS1Databar, ParamName.CompositeCCC,
						ParamName.CompositeCCAB, ParamName.CompositeTLC39, ParamName.PDF417, ParamName.MicroPDF417,
						ParamName.DataMatrix, ParamName.Maxicode, ParamName.QRCode, ParamName.MicroQR, ParamName.Aztec,
						ParamName.HanXin});

				if (values.getLength() <= 0) {
					ATLog.e(TAG, "ERROR. Failed to load barcode symbol paramenter");
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// Barcode Module Initialize Error..
							WaitDialog.hide();

							AlertDialog.Builder alert = new AlertDialog.Builder(BarcodeOptionActivity.this);
							alert.setTitle(R.string.system_alert);
							alert.setIcon(android.R.drawable.ic_dialog_alert);
							alert.setMessage(R.string.failed_load_barcode_option);
							alert.setCancelable(false);
							alert.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									finish();
								}
							});
							alert.show();
							isInitialized = false;
						}

					});
					return;
				}
			} catch (Exception e) {
				ATLog.e(TAG, e, "ERROR. $mChangeBarcodeMode.run() - Faield to change barcode mode");
			}
			adpSymbols.clear();
			for (ParamValue value : values) {
				ATLog.d(TAG, "LOAD.    %s : %s", value.getName(), value.getBoolean());
				adpSymbols.addItem(value.getName(), value.getBoolean());
			}

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// Barcode Initialize Complete..
					adpSymbols.notifyDataSetChanged();
					WaitDialog.hide();
					// Enable action widget
					enableActionWidgets(true);
					isInitialized = true;
				}

			});
		}

	};

	private Runnable mSaveSymbol = new Runnable() {

		@Override
		public void run() {
			int count = adpSymbols.getCount();
			ParamValueList list = new ParamValueList();
			ParamValue value;

			ATLog.d(TAG, "DEBUG. Save Symbologies");

			// Set Key Action
			try {
				getReader().setUseKeyAction(false);
			} catch (ATRfidReaderException e) {
				ATLog.e(TAG, e, "ERROR. onStart() - Failed to set use key action");
			}
			
			for (int i = 0; i < count; i++) {
				value = adpSymbols.getParamValue(i);
				ATLog.d(TAG, "SAVE.    %s : %s", value.getName(), value.getBoolean());
				list.add(value);
			}
			if (!getReader().setBarcodeParam(list)) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// Barcode Module Initialize Error..
						WaitDialog.hide();

						AlertDialog.Builder alert = new AlertDialog.Builder(BarcodeOptionActivity.this);
						alert.setTitle(R.string.system_alert);
						alert.setIcon(android.R.drawable.ic_dialog_alert);
						alert.setMessage(R.string.failed_save_barcode_option);
						alert.setCancelable(false);
						alert.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								finish();
							}
						});
						alert.show();
					}

				});
				return;
			}
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					WaitDialog.hide();
					// Enable action widget
					enableActionWidgets(true);
					finish();
				}

			});
		}

	};

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ATLog.e(TAG, "position:%d, id:%d", position, id);
		
		ParamName p = adpSymbols.getItem(position);
		ATLog.e(TAG, "=>> " + p.toString());
		
		Intent intent;
		
		switch(p) {
		case Code11:
			intent = new Intent(this, OptionSymbolCode11Activity.class);
			startActivityForResult(intent, position);
			break;
		case Code39:
			intent = new Intent(this, OptionSymbolCode39Activity.class);
			startActivityForResult(intent, position);
			break;
		case Code93:
			intent = new Intent(this, OptionSymbolCode93Activity.class);
			startActivityForResult(intent, position);
			break;
		case D2of5:
			intent = new Intent(this, OptionSymbolD2of5Activity.class);
			startActivityForResult(intent, position);
			break;
		case Codabar:
			intent = new Intent(this, OptionSymbolCodabarActivity.class);
			startActivityForResult(intent, position);
			break;
		case I2of5:
			intent = new Intent(this, OptionSymbolI2of5Activity.class);
			startActivityForResult(intent, position);
			break;
		case MSI:
			intent = new Intent(this, OptionSymbolMsiActivity.class);
			startActivityForResult(intent, position);
			break;
		case EAN8:
		case EAN13:
		case UPCA: 
		case UPCE: 
		case UPCE1:
			intent = new Intent(this, OptionSymbolUpcEanActivity.class);
			startActivityForResult(intent, position);
			break;
		case Code128:
		case M2of5:
			intent = new Intent(this, OptionSymbolCode128Activity.class);
			intent.putExtra("Symbol_Name", p);
			startActivityForResult(intent, position);
			break;
		case CompositeCCAB:		
		case CompositeCCC:	
		case CompositeTLC39:
		case GS1128:
		case ISSNEAN:
		case ISBT128:
		case JapanPostal:
		case Maxicode:
		case MicroPDF417:
		case MicroQR:
		case NetherlandsKIXCode:
		case PDF417:
		case USPS4CB:
		case UPUFICSPostal:
		case Aztec:
		case AustraliaPost:
		case DataMatrix:
		case K3of5:
		case QRCode:
		case HanXin:
		case C2of5:
			intent = new Intent(this, OptionSymbolOptionCheck.class);
			intent.putExtra("Symbol_Name", p);
			startActivityForResult(intent, position);
			break;
		case USPostnet:
		case USPlanet:
		case UKPostal:
			intent = new Intent(this, OptionSymbolPostalCodeActivity.class);
			intent.putExtra("Symbol_Name", p);
			startActivityForResult(intent, position);
			break;
		case GS1Databar:
			intent = new Intent(this, OptionSymbolRssActivity.class);
			intent.putExtra("Symbol_Name", p);
			startActivityForResult(intent, position);
			break;
		default:
			return;
		}
	}
}
