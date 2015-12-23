package com.tencent.avsdk.activity;

import java.util.ArrayList;
import java.util.List;

import com.tencent.avsdk.R;
import com.tencent.avsdk.Util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class StreamSetActivity extends Activity implements OnClickListener,
		TextWatcher {
	private Spinner mySpinner;  
	private List<String> list = new ArrayList<String>(); 
	private ArrayAdapter<String> adapter; 
	
	private EditText input_file_path;
    private EditText yuv_wide;
    private EditText yuv_high;
    private EditText output_file_path;
    private int YUVFormat = 0;
      
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stream_set_activity);
		Intent intent = getIntent();
		boolean isInputSet = intent.getBooleanExtra("isInputSet", false);
		
		
		list.add("I420");    
        list.add("RGB");    
    
        input_file_path = (EditText) findViewById(R.id.input_file_path);
        input_file_path.setText(Util.inputYuvFilePath);
        yuv_wide = (EditText) findViewById(R.id.yuv_wide);
        yuv_wide.setText(Util.yuvWide + "");
        yuv_high = (EditText) findViewById(R.id.yuv_high);
        yuv_high.setText(Util.yuvHigh + "");
        output_file_path = (EditText) findViewById(R.id.output_file_path);
        output_file_path.setText(Util.outputYuvFilePath);
        mySpinner = (Spinner)findViewById(R.id.yuv_format);
        mySpinner.setSelection(0);

		if (isInputSet)
		{
			findViewById(R.id.output_set_linearlayout).setVisibility(View.GONE);
		}
		else
		{
			findViewById(R.id.input_set_linearlayout).setVisibility(View.GONE);
		}
		
		findViewById(R.id.set_ok).setOnClickListener(this);
		findViewById(R.id.set_cancel).setOnClickListener(this);
		
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, list);    
        //第三步：为适配器设置下拉列表下拉时的菜单样式。    
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);    
        //第四步：将适配器添加到下拉列表上    
        mySpinner.setAdapter(adapter);    
        //第五步：为下拉列表设置各种事件的响应，这个事响应菜单被选中    
        mySpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){    
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (0 == position) {
					YUVFormat = position;
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				YUVFormat = 0;
			}    
        });    

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.set_ok:
			save();
			finish();
			break;
		case R.id.set_cancel:
			finish();
			break;
		default:
			break;
		}
	}

	private void save() {
		Util.inputYuvFilePath = input_file_path.getText().toString();
		Util.yuvWide = Integer.parseInt(yuv_wide.getText().toString());
		Util.yuvHigh = Integer.parseInt(yuv_high.getText().toString());
		Util.outputYuvFilePath = output_file_path.getText().toString();
		Util.yuvFormat = YUVFormat;
	}

	@Override
	public void afterTextChanged(Editable s) {

		/*EditText editTextIdentifier = (EditText) findViewById(R.id.edit_text_identifier);
		EditText editTextUsersig = (EditText) findViewById(R.id.edit_text_usersig);

		String stringIdentifier = editTextIdentifier.getText().toString();
		String stringUsersig = editTextUsersig.getText().toString();
		findViewById(R.id.ok).setEnabled(
						stringIdentifier != null && stringIdentifier.length() > 0
						&& stringUsersig != null && stringUsersig.length() > 0);*/
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub

	}
}