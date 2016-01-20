package com.tencent.avsdk.control;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.tencent.av.sdk.AVContext;
import com.tencent.av.sdk.AVRoom;
import com.tencent.avsdk.DemoConstants;
import com.tencent.avsdk.MemberInfo;
import com.tencent.avsdk.MultiVideoMembersControlUI;
import com.tencent.avsdk.R;

public class QavsdkControl {
	private static final String TAG = "QavsdkControl";
	private AVContextControl mAVContextControl = null;
	private AVRoomControl mAVRoomControl = null;
	private AVEndpointControl mAVEndpointControl = null;
	private AVUIControl mAVUIControl = null;
	private AVVideoControl mAVVideoControl = null;
	private AVAudioControl mAVAudioControl = null;

	
	
	public QavsdkControl(Context context) {
		mAVContextControl = new AVContextControl(context);
		mAVRoomControl = new AVRoomControl(context);
		mAVEndpointControl = new AVEndpointControl(context);
		mAVVideoControl = new AVVideoControl(context);
		mAVAudioControl = new AVAudioControl(context);
		
		Log.d(TAG, "WL_DEBUG QavsdkControl");
	}
	
	public void setIsSupportMultiView(boolean isSupport) {
		if (null != mAVEndpointControl) {
			mAVEndpointControl.setIsSupportMultiView(isSupport);
		}
	}
	
	public boolean getIsSupportMultiView() {
		if (null != mAVEndpointControl) {
			return mAVEndpointControl.getIsSupportMultiView();
		}
		
		return false;
	}
	
	public void deleteRequestView(String identifier, int videoSrcType) {
		if (null != mAVEndpointControl) {
			mAVEndpointControl.deleteRequestView(identifier, videoSrcType);
		}
	}

	/**
	 * 启动SDK系统
	 * 
	 * @param identifier
	 *            用户身份的唯一标识
	 * @param usersig
	 *            用户身份的校验信息
	 */
	public int startContext(String identifier, String usersig) {
		if (mAVContextControl == null)
			return DemoConstants.DEMO_ERROR_NULL_POINTER;
		return mAVContextControl.startContext(identifier, usersig);
	}

	/**
	 * 关闭SDK系统
	 */
	public void stopContext() {
		if (mAVContextControl != null) {
			mAVContextControl.stopContext();	
		}
	}

	public boolean hasAVContext() {
		if (mAVContextControl == null)
			return false;
		return mAVContextControl.hasAVContext();
	}

	public String getSelfIdentifier() {
		if (mAVContextControl == null)
			return null;
		return mAVContextControl.getSelfIdentifier();
	}

	/**
	 * 创建房间
	 * 
	 * @param relationId
	 *            讨论组号
	 */
	public void enterRoom(int relationId, String roomRole) {
		if (mAVRoomControl != null) {
			mAVRoomControl.enterRoom(relationId, roomRole);		
		}
	}
	
	public void setAudioCat(int audioCat) {
		if (mAVRoomControl != null) {
			mAVRoomControl.setAudioCat(audioCat);		
		}
	}

	/** 关闭房间 */
	public int exitRoom() {
		if (mAVRoomControl == null)
			return DemoConstants.DEMO_ERROR_NULL_POINTER;
		return mAVRoomControl.exitRoom();
	}

	/**
	 * 获取成员列表
	 * 
	 * @return 成员列表
	 */
	public ArrayList<MemberInfo> getMemberList() {
		if (mAVRoomControl == null) {
			return null;
		} 
		return mAVRoomControl.getMemberList();	
	}

	public ArrayList<MemberInfo> getAudioAndCameraMemberList() {		
		if (mAVRoomControl == null) {
			return null;
		} 
		return mAVRoomControl.getAudioAndCameraMemberList();	
	}

	public ArrayList<MemberInfo> getScreenMemberList() {		
		if (mAVRoomControl == null) {
			return null;
		} 
		return mAVRoomControl.getScreenMemberList();	
	}

	public AVRoom getRoom() {
		AVContext avContext = getAVContext();

		return avContext != null ? avContext.getRoom() : null;
	}

	public boolean getIsInStartContext() {
		if (mAVContextControl == null)
			return false;
		
		return mAVContextControl.getIsInStartContext();
	}

	public boolean getIsInStopContext() {
		if (mAVContextControl == null)
			return false;
		
		return mAVContextControl.getIsInStopContext();
	}
	
	public void setTestEnvStatus(boolean status) {
		if (mAVContextControl != null)
			mAVContextControl.setTestEnvStatus(status);
	}
	
	
	
	
	public boolean setIsInStopContext(boolean isInStopContext) {
		if (mAVContextControl == null)
			return false;
		
		return mAVContextControl.setIsInStopContext(isInStopContext);
	}

	public boolean getIsInEnterRoom() {
		if (mAVRoomControl == null)
			return false;
		return mAVRoomControl.getIsInEnterRoom();
	}

	public boolean getIsInCloseRoom() {
		if (mAVRoomControl == null)
			return false;
		return mAVRoomControl.getIsInCloseRoom();
	}

	public AVContext getAVContext() {
		if (mAVContextControl == null)
			return null;		
		return mAVContextControl.getAVContext();
	}
	
	public boolean isInRequestList(String identifier, int videoSrcType) {
		if (null != mAVEndpointControl) {
			return mAVEndpointControl.isInRequestList(identifier, videoSrcType);
		}
		
		return false;
	}


	public void setRemoteHasVideo(String identifier, int videoSrcType, boolean isRemoteHasVideo) {
		if (null != mAVUIControl) {		
			mAVUIControl.setRemoteHasVideo(identifier, videoSrcType, isRemoteHasVideo, false, false);
		}
	}


	public void onCreate(Context context, View contentView) {
		mAVUIControl = new AVUIControl(context, contentView.findViewById(R.id.av_video_layer_ui));
		mAVVideoControl.initAVVideo();
		mAVAudioControl.initAVAudio();
		mAVEndpointControl.initMembersUI((MultiVideoMembersControlUI) contentView.findViewById(R.id.qav_gaudio_gridlayout));
	}

	public void onResume() {
		mAVContextControl.getAVContext().onResume();
		if (mAVUIControl != null) {
			mAVUIControl.onResume();		
		}
	}

	public void onPause() {
		mAVContextControl.getAVContext().onPause();
		if (null != mAVUIControl) {	
			mAVUIControl.onPause();
		}
	}

	public void onDestroy() {
		if (null != mAVAudioControl) {
			mAVAudioControl.resetAudio();
		}
		closeRemoteVideo();
		if (null != mAVUIControl) {
			mAVUIControl.onDestroy();
			mAVUIControl = null;
		}
		
		if (null != mAVEndpointControl) {
			mAVEndpointControl.clearRequestList();
		}
	}
	
	public void closeRemoteVideo() {
		mAVEndpointControl.closeRemoteVideo();
	}


	public void setLocalHasVideo(boolean isLocalHasVideo, String selfIdentifier) {
		if (null != mAVUIControl) {
			mAVUIControl.setLocalHasVideo(isLocalHasVideo, false, selfIdentifier);
		}
	}
	public void setRemoteHasVideo(boolean isRemoteHasVideo, String identifier, int videoSrcType) {
		if (null != mAVUIControl) {
			mAVUIControl.setSmallVideoViewLayout(isRemoteHasVideo, identifier, videoSrcType);
		}
	}
	public void setSelfId(String key) {
		if (null != mAVUIControl) {		
			mAVUIControl.setSelfId(key);
		}
	}	

	public int toggleEnableCamera() {
		return mAVVideoControl.toggleEnableCamera();
	}

	public int toggleSwitchCamera() {
		return mAVVideoControl.toggleSwitchCamera();
	}
	
	public boolean enableUserRender(boolean isEnable) {
		if (isEnable) {
			if (mAVVideoControl == null) {
				return false;
			}
			return mAVVideoControl.StartRecordingVideo();
		} else {
			if (null == mAVUIControl) {
				return false;
			}
			mAVUIControl.enableDefaultRender();
		}
		
		return true;
	}
	
	public void setIsOpenBackCameraFirst(boolean _isOpenBackCameraFirst) {
		mAVVideoControl.setIsOpenBackCameraFirst(_isOpenBackCameraFirst);
	}

	public boolean getIsInOnOffCamera() {
		return mAVVideoControl.getIsInOnOffCamera();
	}
	
	public boolean getIsInOnOffExternalCapture() {
		return mAVVideoControl.getIsInOnOffExternalCapture();
	}
	

	public boolean getIsInSwitchCamera() {
		return mAVVideoControl.getIsInSwitchCamera();
	}
	
	public void setIsInSwitchCamera(boolean isInSwitchCamera) {
		mAVVideoControl.setIsInSwitchCamera(isInSwitchCamera);
	}

	public boolean getIsEnableCamera() {
		return mAVVideoControl.getIsEnableCamera();
	}
	
	public void setIsInOnOffCamera(boolean isInOnOffCamera) {
		mAVVideoControl.setIsInOnOffCamera(isInOnOffCamera);
	}

	public void setIsOnOffExternalCapture(boolean isOnOffExternalCapture) {
		mAVVideoControl.setIsOnOffExternalCapture(isOnOffExternalCapture);
	}

	public boolean getIsFrontCamera() {
		return mAVVideoControl.getIsFrontCamera();
	}
	
	public boolean getIsEnableExternalCapture() {
		return mAVVideoControl.getIsEnableExternalCapture();
	}

	public void onMemberChange() {
		if (mAVUIControl != null) {
			mAVUIControl.onMemberChange();		
		}
	}

	public boolean getHandfreeChecked() {
		return mAVAudioControl.getHandfreeChecked();
	}
	
	
	public AVVideoControl getAVVideoControl() {
		return mAVVideoControl;
	}
	public AVAudioControl getAVAudioControl() {
		return mAVAudioControl;
	}
	public void setRotation(int rotation) {
		if (mAVUIControl != null) {
			mAVUIControl.setRotation(rotation);	
		}
	}
	
	public String getQualityTips( ) {
		if (null != mAVUIControl) {
			return mAVUIControl.getQualityTips();
		} else {
			return null;
		}
	}
	public void setCreateRoomStatus(boolean status) {
		if (mAVRoomControl != null) {
			mAVRoomControl.setCreateRoomStatus(status);
		}
	}
	public void setCloseRoomStatus(boolean status) {
		if (mAVRoomControl != null) {
			mAVRoomControl.setCloseRoomStatus(status);
		}
	}	
		
	public int enableExternalCapture(boolean isEnable) {
		return mAVVideoControl.enableExternalCapture(isEnable);
	}
	
	public void setNetType(int netType) {
		if (mAVRoomControl == null)return ;
		mAVRoomControl.setNetType(netType);
	}
	
	public boolean changeAuthority(byte[] auth_buffer) {
		return mAVRoomControl.changeAuthority(auth_buffer);
	}
	

}