package com.gardengeek.frostalert;

import com.gardengeek.frostalert.R;
import com.gardengeek.frostalert.FrostAlertApp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;

public class FrostAlert extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final EditText edittext = (EditText) findViewById(R.id.edittext);
        final Button button = (Button) findViewById(R.id.okbutton);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	setZipcode(edittext.getText().toString());
            	Intent i = new Intent(getBaseContext(), CurrentTemp.class);
            	startActivity(i);
            }
        });
    }
    
    private void setZipcode(String zipcode)
    {
    	FrostAlertApp appState = ((FrostAlertApp)getApplication());
    	appState.alert.setZipcode(Integer.parseInt(zipcode));
    }
}