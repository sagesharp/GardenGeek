package com.gardengeek.coldsnap;

import com.gardengeek.coldsnap.ColdSnapApp;
import com.gardengeek.coldsnap.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;

public class ColdSnap extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final EditText zipCodeInput = (EditText) findViewById(R.id.zipcodeinput);
        final EditText minTempInput = (EditText) findViewById(R.id.mintempinput);
        final Button button = (Button) findViewById(R.id.okbutton);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	setZipcode(zipCodeInput.getText().toString());
            	setMinTemp(minTempInput.getText().toString());
            	Intent i = new Intent(getBaseContext(), CurrentTemp.class);
            	startActivity(i);
            }
        });
    }
    
    private void setZipcode(String zipcode)
    {
    	ColdSnapApp appState = ((ColdSnapApp)getApplication());
    	appState.alert.setZipcode(Integer.parseInt(zipcode));
    }
    private void setMinTemp(String minTemp)
    {
    	ColdSnapApp appState = ((ColdSnapApp)getApplication());
    	appState.alert.setColdTemperature(Integer.parseInt(minTemp));
    }
}