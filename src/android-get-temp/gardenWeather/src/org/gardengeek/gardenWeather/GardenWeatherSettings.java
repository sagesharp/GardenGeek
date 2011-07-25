package org.gardengeek.gardenWeather;

import org.gardengeek.gardenWeather.GardenWeatherApp;
import org.gardengeek.gardenWeather.R;


import android.app.Activity;
import android.content.SharedPreferences;
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
    	Integer minTemp;
    	Integer zipcode;
        
        final EditText zipCodeInput = (EditText) findViewById(R.id.zipcodeinput);
        final EditText minTempInput = (EditText) findViewById(R.id.mintempinput);
        final Button button = (Button) findViewById(R.id.okbutton);
        
        minTemp = getMinTemp();
        if (minTemp != Integer.MIN_VALUE)
        	minTempInput.setText(minTemp.toString());
        zipcode = getZipcode();
        if (zipcode != Integer.MIN_VALUE) {
        	zipCodeInput.setText(getStringZipcode());
        }
        
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
    
    private String getStringZipcode() {
    	String zip;
    	
    	zip = getZipcode().toString();    	
    	while (zip.length() < 5)
    		zip = "0".concat(zip);
    	return zip;
    }
    
    private Integer getZipcode()
    {
    	GardenWeatherApp appState;
    	SharedPreferences settings;
    	
    	appState = ((GardenWeatherApp)getApplication());
    	settings = getSharedPreferences(appState.PREFS_NAME, 0);
    	return settings.getInt("zipcode", Integer.MIN_VALUE);
    }
    
    private void setZipcode(String zipcode)
    {
    	GardenWeatherApp appState;
    	SharedPreferences settings;
    	SharedPreferences.Editor editor;
    	
    	appState = ((GardenWeatherApp)getApplication());
    	settings = getSharedPreferences(appState.PREFS_NAME, 0);
    	editor = settings.edit();
    	editor.putInt("zipcode", Integer.parseInt(zipcode));
    	editor.commit();
    	
    	appState.alert.setZipcode(Integer.parseInt(zipcode));
    	
    	try {
    		appState.alert.fetchLatLong();
    	} catch (Exception e) {
    		/* Internet's probably down, we'll fetch it later. */
    	}
    }
    
    private Integer getMinTemp()
    {
    	GardenWeatherApp appState;
    	SharedPreferences settings;
    	
    	appState = ((GardenWeatherApp)getApplication());
    	settings = getSharedPreferences(appState.PREFS_NAME, 0);
    	return settings.getInt("minTemp", Integer.MIN_VALUE);
    }
    
    private void setMinTemp(String minTemp)
    {
    	GardenWeatherApp appState;
    	SharedPreferences settings;
    	SharedPreferences.Editor editor;
    	
    	appState = ((GardenWeatherApp)getApplication());
    	settings = getSharedPreferences(appState.PREFS_NAME, 0);
    	editor = settings.edit();
    	editor.putInt("minTemp", Integer.parseInt(minTemp));
    	editor.commit();
    	
    	appState.alert.setColdTemperature(Integer.parseInt(minTemp));
    }
}