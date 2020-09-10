package com.hanmiit.app.rfid.blasterdemo.adapter;

public class ValueItem<T> {
	
	private String text;
	private T value;
	
	public ValueItem(String text, T value) {
		this.text = text;
		this.value = value;
	}
	
	public String getText() {
		return this.text;
	}
	
	public T getValue() {
		return this.value;
	}
}
