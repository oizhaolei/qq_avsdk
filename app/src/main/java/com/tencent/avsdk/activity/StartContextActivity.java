package com.tencent.avsdk.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.tencent.av.sdk.AVError;
import com.tencent.avsdk.QavsdkApplication;
import com.tencent.avsdk.R;
import com.tencent.avsdk.Util;
import com.tencent.avsdk.control.QavsdkControl;

import java.util.ArrayList;

public class StartContextActivity extends ListActivity {
    private static final String TAG = "StartContextActivity";
    private static final int DIALOG_LOGIN = 0;
    private static final int DIALOG_LOGOUT = DIALOG_LOGIN + 1;
    private static final int DIALOG_LOGIN_ERROR = DIALOG_LOGOUT + 1;
    private static final int REQUEST_CODE_CREATE_ACTIVITY = 0;
    private static final int REQUEST_CODE_ADD = REQUEST_CODE_CREATE_ACTIVITY + 1;
    private static final int REQUEST_CODE_ADD_USER = REQUEST_CODE_ADD + 1;
    private int mPosition;
    private int mLoginErrorCode = AVError.AV_OK;
    private ArrayAdapter<String> mAdapter = null;
    private ProgressDialog mDialogLogin = null;
    private ProgressDialog mDialogLogout = null;
    private QavsdkControl mQavsdkControl;
    private ArrayList<String> mArrayList = new ArrayList<String>();
    private Context ctx = null;
//    private String loginInfoUrl = "http://bbs.qcloud.com/forum.php?mod=viewthread&tid=8287&extra=page%3D1%26filter%3Dsortid%26sortid%3D6%26sortid%3D6";
//    private TLSService tlsService;

    private boolean testEnvStatus = false;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, "WL_DEBUG onReceive action = " + action);
            Log.e(TAG, "WL_DEBUG ANR StartContextActivity onReceive action = " + action + " In");
            if (action.equals(Util.ACTION_START_CONTEXT_COMPLETE)) {
                mLoginErrorCode = intent.getIntExtra(
                        Util.EXTRA_AV_ERROR_RESULT, AVError.AV_OK);

                if (mLoginErrorCode == AVError.AV_OK) {
                    refreshWaitingDialog();
                    startActivityForResult(
                            new Intent(StartContextActivity.this,
                                    CreateRoomActivity.class).putExtra(
                                    Util.EXTRA_IDENTIFIER_LIST_INDEX, mPosition),
                            REQUEST_CODE_CREATE_ACTIVITY);
                } else {
                    showDialog(DIALOG_LOGIN_ERROR);
                }
            } else if (action.equals(Util.ACTION_CLOSE_CONTEXT_COMPLETE)) {
                mQavsdkControl.setIsInStopContext(false);
                refreshWaitingDialog();
            }
            Log.e(TAG, "WL_DEBUG ANR StartContextActivity onReceive action = " + action + " Out");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
//		LogcatHelper.getInstance(this).start();
        Log.e(TAG, "WL_DEBUG onCreate");
        setTitle(R.string.login);
        mArrayList.clear();

        ArrayList<String> identifierList = Util.getIdentifierList(this);

        if (null != identifierList) {
            mArrayList.addAll(identifierList);
        }
        mAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mArrayList);
        setListAdapter(mAdapter);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Util.ACTION_START_CONTEXT_COMPLETE);
        intentFilter.addAction(Util.ACTION_CLOSE_CONTEXT_COMPLETE);
        registerReceiver(mBroadcastReceiver, intentFilter);
        mQavsdkControl = ((QavsdkApplication) getApplication())
                .getQavsdkControl();
//        // 设置使用TLS SDK所需的配置信息
//        TLSConfiguration.setSdkAppid(1400001862); // 必须项, sdkAppid, 1400000955,
//        // 1400001285, 101122465
//        TLSConfiguration.setAccountType(1019); // 必须项, accountType, 373, 117, 107
//        TLSConfiguration.setAppVersion("1.0"); // 可选项, 表示app当前版本, 默认为1.0
//        TLSConfiguration.setTimeout(3000); // 可选项, 表示网络操作超时时间, 默认为8s
//
//        // 设置QQ APP_ID和APP_KEY
//        TLSConfiguration.setQqAppIdAndAppKey("1104701569", "CXtj4p63eTEB2gSu");
//
//        // 设置微信APP_ID和APP_SECRET
//        TLSConfiguration.setWxAppIdAndAppSecret("wxc05322d5f11ea2b0", "3ace67c5982c6ed8daa36f8911f609d7");
//
//        tlsService = TLSService.getInstance();
//        tlsService.initTlsSdk(StartContextActivity.this); // 需要使用关于短信或字符串账号密码登录注册服务时调用
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshWaitingDialog();
        Log.e(TAG, "WL_DEBUG onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        Log.e(TAG, "WL_DEBUG onDestroy");
//		LogcatHelper.getInstance(this).stop();
    }

    @Override
    protected void onListItemClick(ListView l, View v, final int position, long id) {
        if (!mQavsdkControl.hasAVContext()) {
            login(position);
        }

        Log.e(TAG, "WL_DEBUG onListItemClick");
    }

    private void login(int position) {
        mLoginErrorCode = mQavsdkControl.startContext(
                Util.getIdentifierList(StartContextActivity.this).get(position), Util
                        .getUserSigList(StartContextActivity.this).get(position));
        if (mLoginErrorCode != AVError.AV_OK) {
            showDialog(DIALOG_LOGIN_ERROR);
        }

        mPosition = position;
        refreshWaitingDialog();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;

        switch (id) {
            case DIALOG_LOGIN:
                dialog = mDialogLogin = Util.newProgressDialog(this,
                        R.string.at_login);
                break;

            case DIALOG_LOGOUT:
                dialog = mDialogLogout = Util.newProgressDialog(this,
                        R.string.at_logout);
                break;

            case DIALOG_LOGIN_ERROR:
                dialog = Util.newErrorDialog(this, R.string.login_failed);
                break;

            default:
                break;
        }
        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_LOGIN_ERROR:
                ((AlertDialog) dialog)
                        .setMessage(getString(R.string.error_code_prefix)
                                + mLoginErrorCode);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "WL_DEBUG onActivityResult requestCode = " + requestCode);
        Log.e(TAG, "WL_DEBUG onActivityResult resultCode = " + resultCode);
        switch (requestCode) {
            case REQUEST_CODE_CREATE_ACTIVITY:
                mQavsdkControl.stopContext();
                refreshWaitingDialog();
                break;
            default:
                break;
        }
    }

    private void refreshWaitingDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                Util.switchWaitingDialog(ctx, mDialogLogin, DIALOG_LOGIN,
                        mQavsdkControl.getIsInStartContext());
                Util.switchWaitingDialog(ctx, mDialogLogout, DIALOG_LOGOUT,
                        mQavsdkControl.getIsInStopContext());
            }
        });

    }


}
