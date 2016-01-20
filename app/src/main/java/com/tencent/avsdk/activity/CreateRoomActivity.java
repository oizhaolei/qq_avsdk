package com.tencent.avsdk.activity;

import com.tencent.av.sdk.AVConstants;
import com.tencent.av.sdk.AVError;
import com.tencent.av.sdk.AVVideoCtrl;
import com.tencent.av.sdk.AVRoom;
import com.tencent.avsdk.DemoConstants;
import com.tencent.avsdk.QavsdkApplication;
import com.tencent.avsdk.R;
import com.tencent.avsdk.Util;
import com.tencent.avsdk.control.QavsdkControl;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CreateRoomActivity extends Activity implements OnClickListener {
	private static final String TAG = "CreateRoomActivity";
	private static final int DIALOG_CREATE_ROOM = 0;
	private static final int DIALOG_CLOSE_ROOM = DIALOG_CREATE_ROOM + 1;
	private static final int DIALOG_CREATE_ROOM_ERROR = DIALOG_CLOSE_ROOM + 1;
	private static final int DIALOG_CLOSE_ROOM_ERROR = DIALOG_CREATE_ROOM_ERROR + 1;
	
	private int mModeListIndex = -1;
	private CheckBox isSupportMultiVideo;
	private CheckBox isAuthAudioSend;
	private CheckBox isAuthAudioRev;
	private CheckBox isAuthVideoSend;
	private CheckBox isAuthVideoRev;
	private CheckBox isOpenBackCameraFirst;
		
	private int mCreateRoomErrorCode = AVError.AV_OK;
	private int mCloseRoomErrorCode = AVError.AV_OK;
	private ProgressDialog mDialogCreateRoom = null;
	private ProgressDialog mDialogCloseRoom = null;
	private QavsdkControl mQavsdkControl;
	private String mSelfIdentifier = "";
	private Context ctx;
	public static final int MAX_TIMEOUT = 5*1000;
	public static final int MSG_CREATEROOM_TIMEOUT = 1;
	private Handler handler = new Handler(new Handler.Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MSG_CREATEROOM_TIMEOUT:
				if (mQavsdkControl != null) {
					mQavsdkControl.setCreateRoomStatus(false);
					mQavsdkControl.setCloseRoomStatus(false);
					refreshWaitingDialog();			
					Toast.makeText(getApplicationContext(), R.string.notify_network_error, Toast.LENGTH_SHORT).show();
				}
				break;

			default:
				break;
			}
			return false;
		}
	});	
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "WL_DEBUG onReceive action = " + action);
			if (action.equals(Util.ACTION_ROOM_CREATE_COMPLETE)) {
				handler.removeMessages(MSG_CREATEROOM_TIMEOUT);
				refreshWaitingDialog();

				mCreateRoomErrorCode = intent.getIntExtra(
						Util.EXTRA_AV_ERROR_RESULT, AVError.AV_OK);

				if (mCreateRoomErrorCode == AVError.AV_OK) {
					startActivityForResult(new Intent(CreateRoomActivity.this, AvActivity.class)
						.putExtra(Util.EXTRA_RELATION_ID, getRelationId())
						.putExtra(Util.EXTRA_SELF_IDENTIFIER, mSelfIdentifier), 0);
				} else {
					if ((mQavsdkControl != null) && (mQavsdkControl.getAVContext() != null) && (mQavsdkControl.getAVContext().getAudioCtrl() != null)) {
						mQavsdkControl.getAVContext().getAudioCtrl().stopTRAEService();
					}
					showDialog(DIALOG_CREATE_ROOM_ERROR);
				}
			} else if (action.equals(Util.ACTION_CLOSE_ROOM_COMPLETE)) {
				refreshWaitingDialog();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_room_activity);
		ctx = this;
		findViewById(R.id.create_room).setOnClickListener(this);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Util.ACTION_ROOM_CREATE_COMPLETE);
		intentFilter.addAction(Util.ACTION_CLOSE_ROOM_COMPLETE);
		registerReceiver(mBroadcastReceiver, intentFilter);
		
		isSupportMultiVideo = (CheckBox)findViewById(R.id.isSupportMultiVideo);
		isAuthAudioSend =  (CheckBox)findViewById(R.id.isAuthAudioSend);
		isAuthAudioRev = (CheckBox)findViewById(R.id.isAuthAudioRev);
		isAuthVideoSend = (CheckBox)findViewById(R.id.isAuthVideoSend);
		isAuthVideoRev = (CheckBox)findViewById(R.id.isAuthVideoRev);
		isOpenBackCameraFirst = (CheckBox)findViewById(R.id.isOpenBackCameraFirst);
		isOpenBackCameraFirst.setChecked(false);
		
		isAuthAudioSend.setChecked(true);
		isAuthAudioRev.setChecked(true);
		isAuthVideoSend.setChecked(true);
		isAuthVideoRev.setChecked(true);
		
		
		int IdentifierListIndex = getIntent().getIntExtra(Util.EXTRA_IDENTIFIER_LIST_INDEX, -1);
		mQavsdkControl = ((QavsdkApplication) getApplication()).getQavsdkControl();

		if (IdentifierListIndex != -1 && mQavsdkControl.getAVContext() != null) {
			TextView login = (TextView) findViewById(R.id.login);
			login.setText(Util.getIdentifierList(this).get(IdentifierListIndex));
			mSelfIdentifier = Util.getIdentifierList(this).get(IdentifierListIndex);
		} else {
			finish();
		}
		
	    isSupportMultiVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (null != mQavsdkControl) {
					mQavsdkControl.setIsSupportMultiView(isChecked);
				}
				
			}
		});
	    
	    isOpenBackCameraFirst.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (null != mQavsdkControl) {
					mQavsdkControl.setIsOpenBackCameraFirst(isChecked);
				}
				
			}
		});
	    
	    isAuthAudioSend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
			}
		});
	    
	    isAuthVideoSend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
			}
		});
	    
	    isAuthVideoRev.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
			}
		});
    
		Log.d(TAG, "WL_DEBUG onCreate");
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshWaitingDialog();
		Log.d(TAG, "WL_DEBUG onResume");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mBroadcastReceiver);
		Log.d(TAG, "WL_DEBUG onDestroy");
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;

		switch (id) {
		case DIALOG_CREATE_ROOM:
			dialog = mDialogCreateRoom = Util.newProgressDialog(this,
					R.string.at_create_room);
			break;

		case DIALOG_CLOSE_ROOM:
			dialog = mDialogCloseRoom = Util.newProgressDialog(this,
					R.string.at_close_room);
			break;

		case DIALOG_CREATE_ROOM_ERROR:
			dialog = Util.newErrorDialog(this, R.string.create_room_failed);
			break;

		case DIALOG_CLOSE_ROOM_ERROR:
			dialog = Util.newErrorDialog(this, R.string.close_room_failed);
			break;

		default:
			break;
		}
		return dialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DIALOG_CREATE_ROOM_ERROR:
			((AlertDialog) dialog)
					.setMessage(getString(R.string.error_code_prefix)
							+ mCreateRoomErrorCode);
			break;
		case DIALOG_CLOSE_ROOM_ERROR:
			((AlertDialog) dialog)
					.setMessage(getString(R.string.error_code_prefix)
							+ mCloseRoomErrorCode);
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.create_room:
			EditText roomRole = (EditText) findViewById(R.id.room_role);
			String roomRoleValue = roomRole.getText().toString();
			
			if( isAuthAudioSend.isChecked() &&
			    isAuthAudioRev.isChecked() &&
			    isAuthVideoSend.isChecked() &&
			    isAuthVideoRev.isChecked() )
			{
				Util.auth_bits = AVRoom.AUTH_BITS_DEFUALT;
			}
			else
			{
				Util.auth_bits = AVRoom.AUTH_BITS_CREATE_ROOM | AVRoom.AUTH_BITS_JOIN_ROOM | AVRoom.AUTH_BITS_RECV_SUB;
				
				if( isAuthAudioSend.isChecked() )
					Util.auth_bits |= AVRoom.AUTH_BITS_SEND_AUDIO;
				if( isAuthAudioRev.isChecked() )
					Util.auth_bits |= AVRoom.AUTH_BITS_RECV_AUDIO;
				if( isAuthVideoSend.isChecked() )
					Util.auth_bits |= AVRoom.AUTH_BITS_SEND_VIDEO;
				if( isAuthVideoRev.isChecked() )
					Util.auth_bits |= AVRoom.AUTH_BITS_RECV_VIDEO;
			}
			
			if (Util.isNetworkAvailable(getApplicationContext())) {
				int relationId = getRelationId();

				if (relationId != 0) {
					if ((mQavsdkControl != null) && (mQavsdkControl.getAVContext() != null) && (mQavsdkControl.getAVContext().getAudioCtrl() != null)) {
						mQavsdkControl.getAVContext().getAudioCtrl().startTRAEService();
					}
					mQavsdkControl.enterRoom(relationId, roomRoleValue);
					handler.sendEmptyMessageDelayed(MSG_CREATEROOM_TIMEOUT, MAX_TIMEOUT);				
					refreshWaitingDialog();
				}	
			} else {
				Toast.makeText(getApplicationContext(), getString(R.string.notify_no_network), Toast.LENGTH_SHORT).show();
			}

			break;			
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "WL_DEBUG onActivityResult" + resultCode);
		if ((mQavsdkControl != null) && (mQavsdkControl.getAVContext() != null) && (mQavsdkControl.getAVContext().getAudioCtrl() != null)) {
			mQavsdkControl.getAVContext().getAudioCtrl().stopTRAEService();
		}
		
		if (DemoConstants.AUTO_EXIT_ROOM == resultCode) {
			return;
		}
		
		mCloseRoomErrorCode = mQavsdkControl.exitRoom();

		if (mCloseRoomErrorCode != AVError.AV_OK) {
			showDialog(DIALOG_CLOSE_ROOM_ERROR);
			if (mQavsdkControl != null) {
				mQavsdkControl.setCloseRoomStatus(false);
			}
			return;
		}

		refreshWaitingDialog();
	}

	/**
	 * 获取讨论组号
	 * 
	 * @return 讨论组号
	 */
	private int getRelationId() {
		EditText room = (EditText) findViewById(R.id.room);
		String relationIdStr = room.getText().toString();
		int relationId = 0;

		try {
			relationId = Integer.parseInt(relationIdStr);
		} catch (Exception e) {
			Log.e(TAG, "WL_DEBUG getRelationId e = " + e);
		}

		return relationId;
	}

	private void refreshWaitingDialog() {
		Util.switchWaitingDialog(this, mDialogCreateRoom, DIALOG_CREATE_ROOM,
				mQavsdkControl.getIsInEnterRoom());
		Util.switchWaitingDialog(this, mDialogCloseRoom, DIALOG_CLOSE_ROOM,
				mQavsdkControl.getIsInCloseRoom());
	}
}