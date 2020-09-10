package com.hanmiit.app.rfid.blasterdemo.filter;

import android.text.InputFilter;
import android.text.Spanned;

public class InputFilterMinMax implements InputFilter {

	private int min, max;

	public InputFilterMinMax(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public InputFilterMinMax(String min, String max) {
		this.min = Integer.parseInt(min);
		this.max = Integer.parseInt(max);
	}

	@Override
	public CharSequence filter(CharSequence source, int start, int end,
			Spanned dest, int dstart, int dend) {
		try {
			String input = "";
			input += dest.subSequence(0, dstart);
			input += source.subSequence(start, end);
			input += dest.subSequence(dend, dest.length());
			int value = Integer.parseInt(input);
			if (isInRange(this.min, this.max, value)) {
				return null;
			}
		} catch (NumberFormatException e) {
			
		}
		return "";
	}

	private boolean isInRange(int min, int max, int value) {
		return max > min ? value >= min && value <= max : value >= max
				&& value <= min;
	}

}
