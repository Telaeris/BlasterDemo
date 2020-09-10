package com.hanmiit.app.rfid.blasterdemo.adapter.listener;

import android.widget.CompoundButton;

public interface LbtChannelListener {
	void onLbtChannelCheckChanged(int position, CompoundButton buttonView,
                                  boolean isChecked);
}
