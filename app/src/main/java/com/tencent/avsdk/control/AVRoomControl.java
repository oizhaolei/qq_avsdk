package com.tencent.avsdk.control;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tencent.av.sdk.AVContext;
import com.tencent.av.sdk.AVEndpoint;
import com.tencent.av.sdk.AVRoom;
import com.tencent.av.sdk.AVRoomMulti;
import com.tencent.avsdk.MemberInfo;
import com.tencent.avsdk.QavsdkApplication;
import com.tencent.avsdk.Util;
import com.tencent.av.sdk.AVConstants;
import com.tencent.av.sdk.AVView;

class AVRoomControl {
	private static final int TYPE_MEMBER_CHANGE_IN = 1;//进入房间事件。
	private static final int TYPE_MEMBER_CHANGE_OUT = 2;//退出房间事件。
	private static final int TYPE_MEMBER_CHANGE_HAS_CAMERA_VIDEO = 3;//有发摄像头视频事件。
	private static final int TYPE_MEMBER_CHANGE_NO_CAMERA_VIDEO = 4;//无发摄像头视频事件。
	private static final int TYPE_MEMBER_CHANGE_HAS_AUDIO = 5;//有发语音事件。
	private static final int TYPE_MEMBER_CHANGE_NO_AUDIO = 6;//无发语音事件。
	private static final int TYPE_MEMBER_CHANGE_HAS_SCREEN_VIDEO = 7;//有发屏幕视频事件。
	private static final int TYPE_MEMBER_CHANGE_NO_SCREEN_VIDEO = 8;//无发屏幕视频事件。

	

	private static final String TAG = "AVRoomControl";
	private boolean mIsInCreateRoom = false;
	private boolean mIsInCloseRoom = false;
	private Context mContext;
	private ArrayList<MemberInfo> mAudioAndCameraMemberList = new ArrayList<MemberInfo>();
	private ArrayList<MemberInfo> mScreenMemberList = new ArrayList<MemberInfo>();
	
	private int audioCat = 0;
	public void setAudioCat(int audioCat) {
		this.audioCat = audioCat;
	}

	private AVRoomMulti.Delegate mRoomDelegate = new AVRoomMulti.Delegate() {
		// 创建房间成功回调
		public void onEnterRoomComplete(int result) {
			Log.d(TAG, "WL_DEBUG mRoomDelegate.onEnterRoomComplete result = " + result);
			mIsInCreateRoom = false;
			mContext.sendBroadcast(new Intent(Util.ACTION_ROOM_CREATE_COMPLETE).putExtra(Util.EXTRA_AV_ERROR_RESULT, result));
		}
		
		// 离开房间成功回调
		public void onExitRoomComplete(int result) {
			Log.d(TAG, "WL_DEBUG mRoomDelegate.onExitRoomComplete result = " + result);
			mIsInCloseRoom = false;
			mAudioAndCameraMemberList.clear();
			mScreenMemberList.clear();
			mContext.sendBroadcast(new Intent(Util.ACTION_CLOSE_ROOM_COMPLETE));			
		}
/*
		protected void onEndpointsEnterRoom(int endpointCount, AVEndpoint endpointList[]) {
			Log.d(TAG, "WL_DEBUG onEndpointsEnterRoom. endpointCount = " + endpointCount);
			onMemberChange(TYPE_MEMBER_CHANGE_IN, endpointList, endpointCount);
		}

		protected void onEndpointsExitRoom(int endpointCount, AVEndpoint endpointList[]) {
			Log.d(TAG, "WL_DEBUG onEndpointsExitRoom. endpointCount = " + endpointCount);
			onMemberChange(TYPE_MEMBER_CHANGE_OUT, endpointList, endpointCount);
		}
*/
		public void onEndpointsUpdateInfo(int eventid, String[] updateList) {
			Log.d(TAG, "WL_DEBUG onEndpointsUpdateInfo. eventid = " + eventid);
			onMemberChange(eventid, updateList);
		}
				
		public void OnPrivilegeDiffNotify(int privilege) {
			Log.d(TAG, "OnPrivilegeDiffNotify. privilege = " + privilege);
		}
		
		public void onChangeAuthority(int retCode) {
			Log.d(TAG, "onChangeAuthority. retCode = " + retCode);
			mContext.sendBroadcast(new Intent(Util.ACTION_CHANGE_AUTHRITY).putExtra(Util.EXTRA_AV_ERROR_RESULT, retCode));
		}
	};


	AVRoomControl(Context context) {
		mContext = context;
	}

	/**
	 * 成员列表变化
	 * 
	 * @param type
	 *            类型
	 * @param endpointList
	 *            成员列表
	 * @param endpointCount
	 *            成员总数
	 */
	private void onMemberChange(int eventid, String[] updateList) {
		Log.d(TAG, "WL_DEBUG onMemberChange type = " + eventid);
		Log.d(TAG, "WL_DEBUG onMemberChange endpointCount = " + updateList.length);
		int endpointCount = updateList.length;
		QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
		AVRoomMulti avRoomMulti = ((AVRoomMulti) qavsdk.getRoom());

		for (int i = 0; i < endpointCount; i++) 
		{
			AVEndpoint endpoint = avRoomMulti.getEndpointById(updateList[i]);
			if(endpoint == null)//endpoint is not exist at all
			{
				for (int j = 0; j < mAudioAndCameraMemberList.size(); j++) 
				{
					if (mAudioAndCameraMemberList.get(j).identifier.equals(updateList[i]))
					{
						qavsdk.deleteRequestView(mAudioAndCameraMemberList.get(j).identifier, AVView.VIDEO_SRC_TYPE_CAMERA);
						mAudioAndCameraMemberList.remove(j);
						break;
					}
				}

				for (int j = 0; j < mScreenMemberList.size(); j++) 
				{
					if (mScreenMemberList.get(j).identifier.equals(updateList[i]))
					{
						qavsdk.deleteRequestView(mScreenMemberList.get(j).identifier, AVView.VIDEO_SRC_TYPE_SCREEN);
						mScreenMemberList.remove(j);
						break;
					}
				}

				continue;
			}
			
			AVEndpoint.Info userInfo = endpoint.getInfo();
			String identifier = userInfo.openId;	

			//audio and camera
			boolean bAudioAndCameraMemberDelete = !endpoint.hasAudio() && !endpoint.hasCameraVideo();
			boolean bAudioAndCameraMemberExist = false;

			for (int j = 0; j < mAudioAndCameraMemberList.size(); j++) 
			{
				if (mAudioAndCameraMemberList.get(j).identifier.equals(identifier)) 
				{
					if (!endpoint.hasCameraVideo()) 
					{
						qavsdk.deleteRequestView(mAudioAndCameraMemberList.get(j).identifier, AVView.VIDEO_SRC_TYPE_CAMERA);
					}

					if(bAudioAndCameraMemberDelete)//delete
					{
						mAudioAndCameraMemberList.remove(j);
					}
					else//modify info
					{
						MemberInfo info = new MemberInfo();
						info.identifier = userInfo.openId;
						info.name = userInfo.openId;
						info.hasCameraVideo = endpoint.hasCameraVideo();
						info.hasAudio = endpoint.hasAudio();	
						info.hasScreenVideo = false;					
						mAudioAndCameraMemberList.set(j, info);
						bAudioAndCameraMemberExist = true;
					}

					break;
				}
			}
	
			if (!bAudioAndCameraMemberDelete && !bAudioAndCameraMemberExist) 
			{
				MemberInfo info = new MemberInfo();
				info.identifier = userInfo.openId;
				info.name = userInfo.openId;
				info.hasCameraVideo = endpoint.hasCameraVideo();
				info.hasAudio = endpoint.hasAudio();		
				info.hasScreenVideo = false;			
				mAudioAndCameraMemberList.add(info);	
			}
				
			//screen
			boolean bScreenMemberDelete = !endpoint.hasScreenVideo();
			boolean bScreenMemberExist = false;

			for (int j = 0; j < mScreenMemberList.size(); j++) 
			{
				if (mScreenMemberList.get(j).identifier.equals(identifier)) 
				{
					if (!endpoint.hasScreenVideo()) 
					{
						qavsdk.deleteRequestView(mScreenMemberList.get(j).identifier, AVView.VIDEO_SRC_TYPE_SCREEN);
					}

					if(bScreenMemberDelete)//delete
					{
						mScreenMemberList.remove(j);
					}
					else//modify info
					{
						MemberInfo info = new MemberInfo();
						info.identifier = userInfo.openId;
						info.name = userInfo.openId;
						info.hasCameraVideo = false;
						info.hasAudio = false;	
						info.hasScreenVideo = endpoint.hasScreenVideo();					
						mScreenMemberList.set(j, info);
						bScreenMemberExist = true;
					}

					break;
				}
			}
	
			if (!bScreenMemberDelete && !bScreenMemberExist) 
			{
				MemberInfo info = new MemberInfo();
				info.identifier = userInfo.openId;
				info.name = userInfo.openId;
				info.hasCameraVideo = false;
				info.hasAudio = false;	
				info.hasScreenVideo = endpoint.hasScreenVideo();				
				mScreenMemberList.add(info);	
			}
		}		
		
		mContext.sendBroadcast(new Intent(Util.ACTION_MEMBER_CHANGE));
	}

	/**
	 * 创建房间
	 * 
	 * @param relationId
	 *            讨论组号
	 */
	void enterRoom(int relationId, String roomRole) {
		Log.d(TAG, "WL_DEBUG enterRoom relationId = " + relationId);
		QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
		AVContext avContext = qavsdk.getAVContext();
		
		byte[] authBuffer = null;//权限位加密串；TODO：请业务侧填上自己的加密串�
		
		AVRoom.EnterRoomParam enterRoomParam = new AVRoomMulti.EnterRoomParam(relationId, Util.auth_bits, authBuffer, roomRole, audioCat);
		// create room
		avContext.enterRoom(AVRoom.AV_ROOM_MULTI, mRoomDelegate, enterRoomParam);
		mIsInCreateRoom = true;
	}

	/** 关闭房间 */
	int exitRoom() {
		Log.d(TAG, "WL_DEBUG exitRoom");
		QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
		AVContext avContext = qavsdk.getAVContext();
		int result = avContext.exitRoom();
		mIsInCloseRoom = true;

		return result;
	}
	
	boolean changeAuthority(byte[] auth_buffer)
	{
		Log.d(TAG, "WL_DEBUG changeAuthority");
		QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
		AVContext avContext = qavsdk.getAVContext();
		AVRoomMulti room = (AVRoomMulti)avContext.getRoom();
		return room.changeAuthority(auth_buffer, auth_buffer.length);
	}

	/**
	 * 获取成员列表
	 * 
	 * @return 成员列表
	 */
	ArrayList<MemberInfo> getMemberList() {
		ArrayList<MemberInfo> memberList = (ArrayList<MemberInfo>)mAudioAndCameraMemberList.clone();
		for (int j = 0; j < mScreenMemberList.size(); j++) 
		{
			memberList.add(mScreenMemberList.get(j));
		}
		return memberList;
	}

	ArrayList<MemberInfo> getAudioAndCameraMemberList() {		
		return mAudioAndCameraMemberList;
	}

	ArrayList<MemberInfo> getScreenMemberList() {		
		return mScreenMemberList;
	}

	boolean getIsInEnterRoom() {
		return mIsInCreateRoom;
	}

	boolean getIsInCloseRoom() {
		return mIsInCloseRoom;
	}
	
	public void setCreateRoomStatus(boolean status) {
		mIsInCreateRoom = status;
	}
	public void setCloseRoomStatus(boolean status) {
		mIsInCloseRoom = status;
	}
	
	public void setNetType(int netType) {
		QavsdkControl qavsdk = ((QavsdkApplication) mContext).getQavsdkControl();
		AVContext avContext = qavsdk.getAVContext();
		AVRoomMulti room = (AVRoomMulti)avContext.getRoom();
		if (null != room) {
			room.setNetType(netType);
		}
	}
}