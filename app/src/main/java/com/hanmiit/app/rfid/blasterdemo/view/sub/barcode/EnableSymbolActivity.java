package com.hanmiit.app.rfid.blasterdemo.view.sub.barcode;

import com.hanmiit.lib.barcode.ParamValue;
import com.hanmiit.lib.barcode.ParamValueList;
import com.hanmiit.lib.barcode.type.ParamName;
import com.hanmiit.lib.diagnostics.ATLog;
import com.hanmiit.app.rfid.blasterdemo.R;
import com.hanmiit.app.rfid.blasterdemo.adapter.BarcodeSymbolStateListAdapter;
import com.hanmiit.app.rfid.blasterdemo.dialog.WaitDialog;
import com.hanmiit.app.rfid.blasterdemo.view.base.RfidActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class EnableSymbolActivity extends RfidActivity implements OnClickListener {

	private static final String TAG = EnableSymbolActivity.class.getSimpleName();

	private ListView lstSymbols;
	private Button btnOption;

	private BarcodeSymbolStateListAdapter adpSymbols;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_barcode_option_enable_symbol);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		ATLog.i(TAG, "INFO. onCreate()");

		// Create Reader
		this.createReader();

		// Initialize Widget
		this.initWidgets();

		// Initialize Reader
		this.initReader();
	}

	@Override
	protected void onDestroy() {

		ATLog.i(TAG, "INFO. onDestroy()");

		// Destroy Reader
		this.destroyReader();

		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		ATLog.d(TAG, "DEBUG. onActivityResult(%s, %s)", getRequestCode(requestCode), getResultCode(resultCode));

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.option:
			break;
		}
	}

	@Override
	protected void initWidgets() {
		lstSymbols = (ListView) findViewById(R.id.symbols);
		lstSymbols.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		adpSymbols = new BarcodeSymbolStateListAdapter(this);
		lstSymbols.setAdapter(adpSymbols);
		btnOption = (Button) findViewById(R.id.set_option);
		btnOption.setOnClickListener(this);
	}

	@Override
	protected void enableActionWidgets(boolean enabled) {
		lstSymbols.setEnabled(enabled);
		btnOption.setEnabled(enabled);
	}

	@Override
	protected void initReader() {
		WaitDialog.show(this, R.string.barcode_load_param);

		new Thread(new Runnable() {

			@Override
			public void run() {
				ParamValueList values = getReader().getBarcodeParam(new ParamName[] { ParamName.UPCA, ParamName.UPCE,
						ParamName.UPCE1, ParamName.EAN8, ParamName.EAN13, ParamName.Code128, ParamName.Code39,
						ParamName.Code93, ParamName.Code11, ParamName.I2of5, ParamName.D2of5, ParamName.Codabar,
						ParamName.MSI, ParamName.C2of5, ParamName.M2of5, ParamName.K3of5, ParamName.USPostnet,
						ParamName.USPlanet, ParamName.UKPostal, ParamName.JapanPostal, ParamName.AustraliaPost,
						ParamName.NetherlandsKIXCode, ParamName.USPS4CB, ParamName.UPUFICSPostal, ParamName.GS1Databar,
						ParamName.CompositeCCC, ParamName.CompositeCCAB, ParamName.CompositeTLC39, ParamName.PDF417,
						ParamName.MicroPDF417, ParamName.DataMatrix, ParamName.Maxicode, ParamName.QRCode,
						ParamName.MicroQR, ParamName.Aztec });

				if (values.getLength() <= 0) {
					ATLog.e(TAG, "ERROR. Failed to load barcode symbol state parameter");

					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							WaitDialog.hide();

							AlertDialog.Builder alert = new AlertDialog.Builder(EnableSymbolActivity.this);
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
						}

					});
					return;
				}

				adpSymbols.clear();
				for (ParamValue value : values) {
					adpSymbols.addItem(value.getName(), value.getBoolean());
				}

				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						adpSymbols.notifyDataSetChanged();
						// Enable action widget
						enableActionWidgets(true);

						WaitDialog.hide();
					}

				});
			}

		}).start();
	}

}
