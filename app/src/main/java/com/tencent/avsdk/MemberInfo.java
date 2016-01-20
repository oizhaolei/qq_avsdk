package com.tencent.avsdk;

import android.graphics.Bitmap;

public class MemberInfo {
	public String identifier = "";
	public boolean hasAudio = false;
	public boolean hasCameraVideo = false;
	public boolean hasScreenVideo = false;
	public boolean isShareMovie = false;
	public boolean hasGetInfo = false;
	public String name = null;
	public Bitmap faceBitmap = null;

	@Override
	public String toString() {
		return "MemberInfo identifier = " + identifier + ", hasAudio = " + hasAudio
				+ ", hasCameraVideo = " + hasCameraVideo + ", hasScreenVideo = " + hasScreenVideo
				+ ", isShareMovie = " + isShareMovie + ", hasGetInfo = "
				+ hasGetInfo + ", name = " + name;
	}

}