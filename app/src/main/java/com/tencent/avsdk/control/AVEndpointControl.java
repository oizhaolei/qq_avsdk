package com.tencent.avsdk.control;

import java.util.ArrayList;

import com.tencent.av.sdk.AVEndpoint;
import com.tencent.av.sdk.AVRoomMulti;
import com.tencent.av.sdk.AVView;
import com.tencent.avsdk.MultiVideoMembersControlUI;
import com.tencent.avsdk.MultiVideoMembersControlUI.MultiVideoMembersClickListener;
import com.tencent.avsdk.QavsdkApplication;
import com.tencent.avsdk.R;
import com.tencent.avsdk.Util;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import com.tencent.av.sdk.AVConstants;

class AVEndpointControl {
	private static final String TAG = "AVEndpointControl";
	private boolean mRemoteHasVideo = false;// 对方是否有视频画面
	private Context mContext;
	private String mRemoteVideoIdentifier = "";
	
	//多路画面相关
	
	private String mRequestIdentifierList[] = null;
	private AVView mRequestViewList[] = null;
	private int mRequestCount = 0;
	private boolean mIsSupportMultiView = true;
	
	public void setIsSupportMultiView(boolean isSupport) {
		mIsSupportMultiView = true;
	}
	
	public boolean getIsSupportMultiView() {
		return mIsSupportMultiView;
	}
	
	private MultiVideoMembersClickListener mMembersOnClickListener = new MultiVideoMembersClickListener() {
		@Override
		public void onMembersClick(final String identifier, final int videoSrcType) 
		{
			Log.d("onMembersClick", "mIsSupportMultiView: " + mIsSupportMultiView);
			QavsdkControl qavsdkControl = ((QavsdkApplication) mContext).getQavsdkControl();
			AVEndpoint endpoint = ((AVRoomMulti) qavsdkControl.getRoom()).getEndpointById(identifier);
			if (endpoint == null)
			{
				Toast.makeText(mContext, R.string.avendpoint_is_null, Toast.LENGTH_LONG).show();
				return;	
			}	
			
			if (identifier.equals(qavsdkControl.getSelfIdentifier())) 
			{
				//对自己不能操作
				return;
			}
			
			if(!mIsSupportMultiView)
			{
				
			}
			else
			{
				boolean hasRequest = false;
				int index = 0;
				for(index = 0; index < mRequestCount; index++)
				{
					if(mRequestIdentifierList[index].equals(identifier) && mRequestViewList[index].videoSrcType == videoSrcType)
					{
						hasRequest = true;
						break;
					}
				}
				
				if(hasRequest)
				{
					mContext.sendBroadcast(new Intent(
							Util.ACTION_VIDEO_CLOSE).putExtra(
							Util.EXTRA_IDENTIFIER, mRequestIdentifierList[index]).putExtra(
							Util.EXTRA_VIDEO_SRC_TYPE, mRequestViewList[index].videoSrcType));
					
					if (!deleteRequestView(index)) {
						return;
					}
					
					if (0 != mRequestCount) {
						AVEndpoint.requestViewList(mRequestIdentifierList, mRequestViewList, mRequestCount, mRequestViewListCompleteCallback);
					} else {
						AVEndpoint.cancelAllView(mCancelAllViewCompleteCallback);
					}
				}
				else
				{
					/*
					同时请求多个成员的视频画面。
					注意：
					. 画面大小可以根据业务层实际需要及硬件能力决定。
					. 如果是手机，建议只有其中一路是大画面，其他都是小画面，这样硬件更容易扛得住，同时减少流量。
					. 这边把320×240及以上大小的画面认为是大画面；反之，认为是小画面。
					. 实际上请求到的画面大小，由发送方决定。如A传的画面是小画面，即使这边即使想请求它的大画面，也只能请求到的小画面。
					. 发送方传的画面大小，是否同时有大画面和小画面，由其所设置的编解码参数、场景、硬件、网络等因素决定。
					. RequestViewList和CancelAllView不能并发执行，即同一时间点只能进行一种操作。
					. RequestViewList与CancelAllView配对使用，不能与RequestView和CancelView交叉使用。
					*/
					
					
					
					AVView view = new AVView();
					view.videoSrcType = videoSrcType;
					view.viewSizeType = AVView.VIEW_SIZE_TYPE_BIG;
					
					mRequestViewList[mRequestCount] = view;
					mRequestIdentifierList[mRequestCount] = identifier;
					mRequestCount++;
					
					AVEndpoint.requestViewList(mRequestIdentifierList, mRequestViewList, mRequestCount, mRequestViewListCompleteCallback);
					mContext.sendBroadcast(new Intent(
							Util.ACTION_VIDEO_SHOW).putExtra(
							Util.EXTRA_IDENTIFIER, identifier).putExtra(
							Util.EXTRA_VIDEO_SRC_TYPE, view.videoSrcType));	
				}
			}						
					
		}

		@Override
		public void onMembersHolderTouch() {
		}
	};
	
	private AVEndpoint.CancelAllViewCompleteCallback mCancelAllViewCompleteCallback = new AVEndpoint.CancelAllViewCompleteCallback() {
		protected void OnComplete(int result) {
			// TODO
			Log.d(TAG, "CancelAllViewCompleteCallback.OnComplete");
		}
	};

	private AVEndpoint.RequestViewListCompleteCallback mRequestViewListCompleteCallback = new AVEndpoint.RequestViewListCompleteCallback() {
		protected void OnComplete(String identifierList[], AVView viewList[], int count, int result) {
			// TODO
			Log.d(TAG, "RequestViewListCompleteCallback.OnComplete");
		}
	};

	AVEndpointControl(Context context) {
		mContext = context;
		mRequestIdentifierList = new String[AVView.MAX_VIEW_COUNT];
		mRequestViewList = new AVView[AVView.MAX_VIEW_COUNT];
		mRequestCount = 0;
	}

	/**
	 * 初始化成员列表界面
	 * 
	 * @param membersUI
	 *            成员列表界面
	 * */
	void initMembersUI(MultiVideoMembersControlUI membersUI) {
		mRemoteHasVideo = false;// 对方是否有视频画面
		mRemoteVideoIdentifier = "";
		Resources resources = mContext.getResources();
		membersUI.setOnMemberClickListener(mMembersOnClickListener);
		LayoutParams membersHolderParams = (LayoutParams) membersUI
				.getLayoutParams();
		int height = 0;

		if (membersUI.getMode() == MultiVideoMembersControlUI.MODE_ONE_LINE) {
			height = resources
					.getDimensionPixelSize(R.dimen.qav_gaudio_members_holder_height_one_line);
		} else if (membersUI.getMode() == MultiVideoMembersControlUI.MODE_TWO_LINE) {
			height = resources
					.getDimensionPixelSize(R.dimen.qav_gaudio_members_holder_height_two_line);
		}
		membersHolderParams.height = height;
		membersUI.setLayoutParams(membersHolderParams);

		QavsdkControl qavsdkControl = ((QavsdkApplication) mContext)
				.getQavsdkControl();
		membersUI.notifyDataSetChanged(qavsdkControl.getMemberList());
	}

	/**
	 * 关闭远程视频
	 */
	public void closeRemoteVideo() {
		QavsdkControl qavsdkControl = ((QavsdkApplication) mContext)
				.getQavsdkControl();
		AVRoomMulti avRoomMulti = ((AVRoomMulti) qavsdkControl.getRoom());
		if (avRoomMulti != null) {
			if(!mIsSupportMultiView)
			{				
				mRemoteHasVideo = false;
				mRemoteVideoIdentifier = "";
			}
			else
			{
				if(mRequestCount > 0)AVEndpoint.cancelAllView(mCancelAllViewCompleteCallback);
			}
		}

		mContext.sendBroadcast(new Intent(Util.ACTION_VIDEO_CLOSE));
	}
	
	private boolean deleteRequestView(int index){
		ArrayList<String> requestIdentifierArrayList = new ArrayList<String>();
		ArrayList<AVView> requestViewArrayList = new ArrayList<AVView>();
		
		if (index < 0 || index >= mRequestCount) {
			return false;
		}
		for(int i = 0; i < mRequestCount; i++){
			if(i != index){
				requestIdentifierArrayList.add(mRequestIdentifierList[i]);
				requestViewArrayList.add(mRequestViewList[i]);
			}
		}
		
		mRequestIdentifierList = requestIdentifierArrayList.toArray(new String[AVView.MAX_VIEW_COUNT]);
		mRequestViewList = requestViewArrayList.toArray(new AVView[AVView.MAX_VIEW_COUNT]);
		mRequestCount--;
		
		return true;
	}
	
	public void deleteRequestView(String identifier, int videoSrcType){
		if (TextUtils.isEmpty(identifier)) {
			return;
		}
		
		boolean hasRequest = false;
		
		int index = 0;
		for(index = 0; index < mRequestCount; index++)
		{
			if(mRequestIdentifierList[index].equals(identifier) && mRequestViewList[index].videoSrcType == videoSrcType)
			{
				hasRequest = true;
				break;
			}
		}
		
		if (hasRequest)
		{
			deleteRequestView(index);
		}
	}
	
	public void clearRequestList() {
		mRequestIdentifierList = new String[AVView.MAX_VIEW_COUNT];
		mRequestViewList = new AVView[AVView.MAX_VIEW_COUNT];
		mRequestCount = 0;
	}
	
	public boolean isInRequestList(String identifier, int videoSrcType) {
		if (TextUtils.isEmpty(identifier) || videoSrcType == AVView.VIDEO_SRC_TYPE_NONE) {
			return false;
		}
		
		for (int i = 0; i < mRequestCount; i++) {
			if (mRequestIdentifierList[i].equals(identifier) && mRequestViewList[i].videoSrcType == videoSrcType) {
				return true;
			}
		}
		
		return false;
	}
}