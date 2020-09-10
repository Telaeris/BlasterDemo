package com.hanmiit.app.rfid.blasterdemo.data;

import java.util.ArrayList;

public class SharedData {

	public static ArrayList<String> mTags;
	
	public static void setTagList(ArrayList<String> tags) {
		mTags = tags;
	}
	
	public static ArrayList<String> getTagList() {
		return mTags;
	}
}
