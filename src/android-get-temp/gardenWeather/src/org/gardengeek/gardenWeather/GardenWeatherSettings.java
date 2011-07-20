package org.gardengeek.gardenWeather;

import org.gardengeek.gardenWeather.GardenWeatherApp;
import org.gardengeek.gardenWeather.R;


import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GardenWeatherSettings extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    	GardenWeatherApp appState;
    	Integer minTemp;
    	String zipcode;
        
        final EditText zipCodeInput = (EditText) findViewById(R.id.zipcodeinput);
        final EditText minTempInput = (EditText) findViewById(R.id.mintempinput);
        final Button button = (Button) findViewById(R.id.okbutton);
        
        appState = ((GardenWeatherApp)getApplication());
        minTemp = appState.alert.getColdTemperature();
        if (minTemp != Integer.MIN_VALUE)
        	minTempInput.setText(minTemp.toString());
        zipcode = appState.alert.getZipcode();
        if (zipcode != null)
        	zipCodeInput.setText(zipcode);
        
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	GardenWeatherApp appState = ((GardenWeatherApp)getApplication());
            	
            	if (appState.alert.getZipcode() != zipCodeInput.getText().toString())
            		setZipcode(zipCodeInput.getText().toString());
            	setMinTemp(minTempInput.getText().toString());
            	finish();
            }
        });
    }
    
    private void setZipcode(String zipcode)
    {
    	GardenWeatherApp appState = ((GardenWeatherApp)getApplication());
    	appState.alert.setZipcode(Integer.parseInt(zipcode));
    	try {
    		appState.alert.fetchLatLong();
    	} catch (Exception e) {
    		/* Internet's probably down, we'll fetch it later. */
    	}
    }
    private void setMinTemp(String minTemp)
    {
    	GardenWeatherApp appState = ((GardenWeatherApp)getApplication());
    	appState.alert.setColdTemperature(Integer.parseInt(minTemp));
    }
}