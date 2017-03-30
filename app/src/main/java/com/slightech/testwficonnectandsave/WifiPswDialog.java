package com.slightech.testwficonnectandsave;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class WifiPswDialog extends Dialog {
	private Button cancelButton;
	private Button okButton;
	private EditText pswEdit;
	private OnCustomDialogListener customDialogListener;
	public WifiPswDialog(Context context, OnCustomDialogListener customListener) {
		super(context);
		customDialogListener = customListener;
		
	}
	public interface OnCustomDialogListener{
		void back(String str);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_config_dialog);
		setTitle("请输入密码");
		pswEdit = (EditText)findViewById(R.id.wifiDialogPsw);
		cancelButton = (Button)findViewById(R.id.wifiDialogCancel);
		okButton = (Button)findViewById(R.id.wifiDialogCertain);
		cancelButton.setOnClickListener(buttonDialogListener);
		okButton.setOnClickListener(buttonDialogListener);
		
	}

	private View.OnClickListener buttonDialogListener = new View.OnClickListener() {

		@Override
		public void onClick(View view) {
			if(view.getId() == R.id.wifiDialogCancel){
				pswEdit = null;
				customDialogListener.back(null);
				cancel();
			}
			else{
				customDialogListener.back(pswEdit.getText().toString());
				dismiss();
			}
		}
	};
	
}
