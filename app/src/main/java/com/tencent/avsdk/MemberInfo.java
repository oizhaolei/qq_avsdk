package com.tencent.avsdk;

import android.graphics.Bitmap;

public class MemberInfo {
	public String identifier = "";
	public boolean isSpeaking = false;
	public boolean isVideoIn = false;
	public boolean isShareSrc = false;
	public boolean isShareMovie = false;
	public boolean hasGetInfo = false;
	public String name = null;
	public Bitmap faceBitmap = null;

	@Override
	public String toString() {
		return "MemberInfo identifier = " + identifier + ", isSpeaking = " + isSpeaking
				+ ", isVideoIn = " + isVideoIn + ", isShareSrc = " + isShareSrc
				+ ", isShareMovie = " + isShareMovie + ", hasGetInfo = "
				+ hasGetInfo + ", name = " + name;
	}
}