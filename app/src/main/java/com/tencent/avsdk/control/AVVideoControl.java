package com.tencent.avsdk.control;

//test
/*
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
*/

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tencent.av.sdk.AVConstants;
import com.tencent.av.sdk.AVError;
import com.tencent.av.sdk.AVVideoCtrl;
import com.tencent.av.sdk.AVVideoCtrl.EnableCameraCompleteCallback;
import com.tencent.av.sdk.AVVideoCtrl.EnableExternalCaptureCompleteCallback;
import com.tencent.av.sdk.AVVideoCtrl.RemoteVideoPreviewCallback;

import com.tencent.av.sdk.AVVideoCtrl.SwitchCameraCompleteCallback;
import com.tencent.av.sdk.AVVideoCtrl.VideoFrame;
import com.tencent.avsdk.QavsdkApplication;
import com.tencent.avsdk.Util;

public class AVVideoControl {
	private static final String TAG = "AVVideoControl";
	private Context mContext = null;
	private boolean mIsEnableCamera = false;
	private boolean mIsFrontCamera = true;
	private boolean mIsInOnOffCamera = false;
	private boolean mIsInSwitchCamera = false;
	private static final int CAMERA_NONE = -1;
	private static final int FRONT_CAMERA = 0;
	private static final int BACK_CAMERA = 1;
	private boolean mIsOnOffExternalCapture = false;
	private boolean mIsEnableExternalCapture = false;

	private EnableCameraCompleteCallback mEnableCameraCompleteCallback = new EnableCameraCompleteCallback() {
		protected void onComplete(boolean enable, int result) {
			super.onComplete(enable, result);
			Log.d(TAG, "WL_DEBUG mEnableCameraCompleteCallback.onComplete enable = " + enable);
			Log.d(TAG, "WL_DEBUG mEnableCameraCompleteCallback.onComplete result = " + result);
			mIsInOnOffCamera = false;

			if (result == AVError.AV_OK) {
				mIsEnableCamera = enable;
			}

			mContext.sendBroadcast(new Intent(Util.ACTION_ENABLE_CAMERA_COMPLETE).putExtra(Util.EXTRA_AV_ERROR_RESULT, result).putExtra(Util.EXTRA_IS_ENABLE, enable));
		}
	};
	
	private EnableExternalCaptureCompleteCallback mEnableExternalCaptureCompleteCallback = new EnableExternalCaptureCompleteCallback() {
			@Override
			protected void onComplete(boolean enable, int result) {
				super.onComplete(enable, result);
				Log.d(TAG, "WL_DEBUG mEnableExternalCaptureCompleteCallback.onComplete enable = " + enable);
				Log.d(TAG, "WL_DEBUG mEnableExternalCaptureCompleteCallback.onComplete result = " + result);
				mIsOnOffExternalCapture = false;
				
				if (result == AVError.AV_OK) {
					mIsEnableExternalCapture = enable;
				}
				
				mContext.sendBroadcast(new Intent(Util.ACTION_ENABLE_EXTERNAL_CAPTURE_COMPLETE).putExtra(Util.EXTRA_AV_ERROR_RESULT, result).putExtra(Util.EXTRA_IS_ENABLE, enable));
				
			}
	};

	private SwitchCameraCompleteCallback mSwitchCameraCompleteCallback = new SwitchCameraCompleteCallback() {
		protected void onComplete(int cameraId, int result) {
			super.onComplete(cameraId, result);
			Log.d(TAG, "WL_DEBUG mSwitchCameraCompleteCallback.onComplete cameraId = " + cameraId);
			Log.d(TAG, "WL_DEBUG mSwitchCameraCompleteCallback.onComplete result = " + result);
			mIsInSwitchCamera = false;
			boolean isFront = cameraId == FRONT_CAMERA;

			if (result == AVError.AV_OK) {
				mIsFrontCamera = isFront;
			}

			mContext.sendBroadcast(new Intent(Util.ACTION_SWITCH_CAMERA_COMPLETE).putExtra(Util.EXTRA_AV_ERROR_RESULT, result).putExtra(Util.EXTRA_IS_FRONT, isFront));
		}
	};

	public AVVideoControl(Context context) {
		mContext = context;
	}

	int enableCamera(boolean isEnable) {
		int result = AVError.AV_OK;

		if (mIsEnableCamera != isEnable) {
			QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
			AVVideoCtrl avVideoCtrl = qavsdk.getAVContext().getVideoCtrl(AVConstants.AV_VIDEO_CHANNEL_MAIN);
			mIsInOnOffCamera = true;
			result = avVideoCtrl.enableCamera(FRONT_CAMERA,isEnable, mEnableCameraCompleteCallback);
		}
		Log.d(TAG, "WL_DEBUG enableCamera isEnable = " + isEnable);
		Log.d(TAG, "WL_DEBUG enableCamera result = " + result);
		return result;
	}
	
	int enableExternalCapture(boolean isEnable) {
		int result = AVError.AV_OK;
		
		if (mIsEnableExternalCapture != isEnable) {
			QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
			AVVideoCtrl avVideoCtrl = qavsdk.getAVContext().getVideoCtrl(AVConstants.AV_VIDEO_CHANNEL_MAIN);
			mIsOnOffExternalCapture = true;
			result = avVideoCtrl.enableExternalCapture(isEnable, mEnableExternalCaptureCompleteCallback);
		}
		
		Log.d(TAG, "WL_DEBUG enableExternalCapture isEnable = " + isEnable);
		Log.d(TAG, "WL_DEBUG enableExternalCapture result = " + result);
		return result;
	}

	int switchCamera(boolean isFront) {
		int result = AVError.AV_OK;

		if (mIsFrontCamera != isFront) {
			QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
			AVVideoCtrl avVideoCtrl = qavsdk.getAVContext().getVideoCtrl(AVConstants.AV_VIDEO_CHANNEL_MAIN);
			mIsInSwitchCamera = true;
			result = avVideoCtrl.switchCamera(isFront ? FRONT_CAMERA : BACK_CAMERA, mSwitchCameraCompleteCallback);
		}
		Log.d(TAG, "WL_DEBUG switchCamera isFront = " + isFront);
		Log.d(TAG, "WL_DEBUG switchCamera result = " + result);
		return result;
	}
	
	void setRotation(int rotation) {
		QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
		AVVideoCtrl avVideoCtrl = qavsdk.getAVContext().getVideoCtrl(AVConstants.AV_VIDEO_CHANNEL_MAIN);
		avVideoCtrl.setRotation(rotation);
		Log.e(TAG, "WL_DEBUG setRotation rotation = " + rotation);

	}
	String getQualityTips() {
		QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
		AVVideoCtrl avVideoCtrl = qavsdk.getAVContext().getVideoCtrl(AVConstants.AV_VIDEO_CHANNEL_MAIN);
		return avVideoCtrl.GetQualityTips();
	}	

	int toggleEnableCamera() {
		return enableCamera(!mIsEnableCamera);
	}
	
	RemoteVideoPreviewCallback remoteVideoPreviewCallback = new RemoteVideoPreviewCallback() {
		public void onFrameReceive(VideoFrame videoFrame) {
		
			Log.d(TAG, "real RemoteVideoPreviewCallback.onFrameReceive");
			Log.d(TAG, "len: " + videoFrame.dataLen);
			Log.d(TAG, "openid: " + videoFrame.openId);
			Log.d(TAG, "videoFormat: " + videoFrame.videoFormat);
			Log.d(TAG, "width: " + videoFrame.width);
			Log.d(TAG, "height: " + videoFrame.height);
			
			String printTxtPath =   Util.outputYuvFilePath + "/" + videoFrame.height + "_" + videoFrame.width + ".yuv" ;
			Log.d("test", "printTxtPath: " + printTxtPath);
			byte[] b = videoFrame.data;
			DataOutputStream d;
			try {
				d = new DataOutputStream(new FileOutputStream(
						printTxtPath, true));
				d.write(b);
				d.flush();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    	

		};
	};
	
	public boolean StartRecordingVideo() {
		QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
		AVVideoCtrl avVideoCtrl = qavsdk.getAVContext().getVideoCtrl(AVConstants.AV_VIDEO_CHANNEL_MAIN);
		
		return avVideoCtrl.setRemoteVideoPreviewCallback(remoteVideoPreviewCallback);
		
		
	}

	int toggleSwitchCamera() {
		return switchCamera(!mIsFrontCamera);
	}

	boolean getIsInOnOffCamera() {
		return mIsInOnOffCamera;
	}

	boolean getIsInSwitchCamera() {
		return mIsInSwitchCamera;
	}
	
	public void setIsInSwitchCamera(boolean isInSwitchCamera) {
		this.mIsInSwitchCamera = isInSwitchCamera;
	}

	boolean getIsEnableCamera() {
		return mIsEnableCamera;
	}
	
	boolean getIsInOnOffExternalCapture() {
		return mIsOnOffExternalCapture;
	}
	
	boolean getIsEnableExternalCapture() {
		return mIsEnableExternalCapture;
	}
	
	public void setIsInOnOffCamera(boolean isInOnOffCamera) {
		this.mIsInOnOffCamera = isInOnOffCamera;
	}
	
	public void setIsOnOffExternalCapture(boolean isOnOffExternalCapture) {
		this.mIsOnOffExternalCapture = isOnOffExternalCapture;
	}

	boolean getIsFrontCamera() {
		return mIsFrontCamera;
	}

	void initAVVideo() {
		mIsEnableCamera = false;
		mIsFrontCamera = true;
		mIsInOnOffCamera = false;
		mIsInSwitchCamera = false;
		mIsEnableExternalCapture = false;
		mIsOnOffExternalCapture = false;
	}
	
	public void setRenderCallback() {
		QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
		AVVideoCtrl avVideoCtrl = qavsdk.getAVContext().getVideoCtrl(AVConstants.AV_VIDEO_CHANNEL_MAIN);
//		avVideoCtrl.setRemoteVideoRenderFrameCallback(new RemoteVideoRenderFrameCallback() {
//			@Override
//			public void onRenderFrame(String identifer) {
//				// TODO Auto-generated method stub
//				super.onRenderFrame(identifer);
//				Log.d("setRenderCal", "identifer: " + identifer);
//			}
//			
//		});
	}
}