package com.tencent.avsdk.control;

import com.tencent.av.sdk.AVEndpoint;
import com.tencent.av.sdk.AVRoomMulti;
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

class AVEndpointControl {
	private static final String TAG = "AVEndpointControl";
	private boolean mRemoteHasVideo = false;// 对方是否有视频画面
	private Context mContext;
	private String mRemoteVideoIdentifier = "";
	
	//多路画面相关
	private static final int MAX_REQUEST_VIEW_COUNT = 4;//当前最大支持请求画面个数
	private String mRequestIdentifierList[] = null;
	private AVEndpoint.View mRequestViewList[] = null;
	private int mRequestCount = 0;
	private boolean mIsSupportMultiView = false;
	
	private MultiVideoMembersClickListener mMembersOnClickListener = new MultiVideoMembersClickListener() {
		@Override
		public void onMembersClick(final String identifier, final int videoSrcType, final boolean needRequest) 
		{
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
				if(mRemoteHasVideo && mRemoteVideoIdentifier.endsWith(identifier))
				{
					endpoint.cancelView(videoSrcType, mCancelViewCompleteCallback);
					mContext.sendBroadcast(new Intent(
							Util.ACTION_VIDEO_CLOSE).putExtra(
							Util.EXTRA_IDENTIFIER, identifier).putExtra(
							Util.EXTRA_VIDEO_SRC_TYPE, videoSrcType));
					mRemoteHasVideo = false;
					mRemoteVideoIdentifier = "";
				}
				else
				{
					AVEndpoint.View view = new AVEndpoint.View();
					view.videoSrcType = AVEndpoint.View.VIDEO_SRC_TYPE_CAMERA;//SDK1.2版本只支持摄像头视频源，所以当前只能设置为VIDEO_SRC_TYPE_CAMERA。
					view.viewSizeType = AVEndpoint.View.VIEW_SIZE_TYPE_BIG;
					
					endpoint.requestView(view, mRequestViewCompleteCallback);
					mContext.sendBroadcast(new Intent(
							Util.ACTION_VIDEO_SHOW).putExtra(
							Util.EXTRA_IDENTIFIER, identifier).putExtra(
							Util.EXTRA_VIDEO_SRC_TYPE, view.videoSrcType));
					mRemoteHasVideo = true;	
					mRemoteVideoIdentifier = identifier;
				}
			}
			else
			{
				boolean hasRequest = false;
				for(int i = 0; i < mRequestCount; i++)
				{
					if(mRequestIdentifierList[i].equals(identifier))
					{
						hasRequest = true;
						break;
					}
				}
				
				if(hasRequest)//注意：已经请求过这个人了，再点击这个人的头像，这边为了方便测试，就先当作要去取消画面，而且是取消所有人的画面
				{
					AVEndpoint.cancelAllView(mCancelAllViewCompleteCallback);
					for(int i = 0; i < mRequestCount; i++)
					{
						mContext.sendBroadcast(new Intent(
								Util.ACTION_VIDEO_CLOSE).putExtra(
								Util.EXTRA_IDENTIFIER, mRequestIdentifierList[i]).putExtra(
								Util.EXTRA_VIDEO_SRC_TYPE, mRequestViewList[i].videoSrcType));
					}
					mRequestCount = 0;
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
					
					for(int i = 0; i < mRequestCount; i++)
					{
						mRequestViewList[i].viewSizeType = AVEndpoint.View.VIEW_SIZE_TYPE_SMALL;
					}
					
					AVEndpoint.View view = new AVEndpoint.View();
					view.videoSrcType = AVEndpoint.View.VIDEO_SRC_TYPE_CAMERA;//SDK1.2版本只支持摄像头视频源，所以当前只能设置为VIDEO_SRC_TYPE_CAMERA。
					view.viewSizeType = AVEndpoint.View.VIEW_SIZE_TYPE_BIG;
					
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

	private AVEndpoint.CancelViewCompleteCallback mCancelViewCompleteCallback = new AVEndpoint.CancelViewCompleteCallback() {
		protected void OnComplete(String identifier, int result) {
			// TODO
			Log.d(TAG, "CancelViewCompleteCallback.OnComplete");
		}
	};

	private AVEndpoint.RequestViewCompleteCallback mRequestViewCompleteCallback = new AVEndpoint.RequestViewCompleteCallback() {
		protected void OnComplete(String identifier, int result) {
			// TODO
			Log.d(TAG, "RequestViewCompleteCallback.OnComplete");
		}
	};
	
	private AVEndpoint.CancelAllViewCompleteCallback mCancelAllViewCompleteCallback = new AVEndpoint.CancelAllViewCompleteCallback() {
		protected void OnComplete(int result) {
			// TODO
			Log.d(TAG, "CancelAllViewCompleteCallback.OnComplete");
		}
	};

	private AVEndpoint.RequestViewListCompleteCallback mRequestViewListCompleteCallback = new AVEndpoint.RequestViewListCompleteCallback() {
		protected void OnComplete(String identifierList[], int count, int result) {
			// TODO
			Log.d(TAG, "RequestViewListCompleteCallback.OnComplete");
		}
	};

	AVEndpointControl(Context context) {
		mContext = context;
		mRequestIdentifierList = new String[MAX_REQUEST_VIEW_COUNT];
		mRequestViewList = new AVEndpoint.View[MAX_REQUEST_VIEW_COUNT];
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
	void closeRemoteVideo() {
		QavsdkControl qavsdkControl = ((QavsdkApplication) mContext)
				.getQavsdkControl();
		AVRoomMulti avRoomMulti = ((AVRoomMulti) qavsdkControl.getRoom());
		if (avRoomMulti != null) {
			if(!mIsSupportMultiView)
			{
				if(!mRemoteVideoIdentifier.equals(""))
				{
					AVEndpoint endpoint = avRoomMulti
							.getEndpointById(mRemoteVideoIdentifier);
		
					if (endpoint != null) {
						endpoint.cancelView(AVEndpoint.View.VIDEO_SRC_TYPE_CAMERA, mCancelViewCompleteCallback);
					}
				}				
			}
			else
			{
				if(mRequestCount > 0)AVEndpoint.cancelAllView(mCancelAllViewCompleteCallback);
			}
		}

		mContext.sendBroadcast(new Intent(Util.ACTION_VIDEO_CLOSE));
	}
}