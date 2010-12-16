package com.gardengeek.frostalert;

import com.gardengeek.frostalert.R;
import com.gardengeek.frostalert.FrostAlertApp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.content.Intent;

public class FrostAlert extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final EditText edittext = (EditText) findViewById(R.id.edittext);
        edittext.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                /* If the event is a key-down event on the "enter" button */
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                	setZipcode(edittext.getText().toString());
                	Intent i = new Intent(getBaseContext(), CurrentTemp.class);
                	startActivity(i);
                  return true;
                }
                return false;
            }
        });
    }
    
    private void setZipcode(String zipcode)
    {
    	FrostAlertApp appState = ((FrostAlertApp)getApplication());
    	appState.alert.setZipcode(Integer.parseInt(zipcode));
    }
}