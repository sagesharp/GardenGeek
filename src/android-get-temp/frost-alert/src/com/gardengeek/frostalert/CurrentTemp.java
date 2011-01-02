package com.gardengeek.frostalert;

import java.util.List;

import com.gardengeek.frostalert.R;
import com.gardengeek.frostalert.FrostAlertApp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.GridView;

public class CurrentTemp extends Activity {
    private String latlong;
    
    public void onCreate(Bundle savedInstanceState) {
    	TextView tempText;
    	GridView gridview;
    	List<FrostAlertService.DateTemp> dateTemps;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
    	Button button = (Button) findViewById(R.id.okbutton);
    	button.setText("OK");
    	
        setContentView(R.layout.temp);

        tempText = (TextView) findViewById(R.id.zipcode);
        tempText.setText(getZip());

        tempText = (TextView) findViewById(R.id.latlong);
        tempText.setText(getLatLong());
        
        dateTemps = getDateTemp(latlong);
        while (dateTemps.size() < 3)
        	dateTemps.add(new FrostAlertService.DateTemp("Never", "-400"));
        
        tempText = (TextView) findViewById(R.id.today);
        tempText.setText(dateTemps.get(0).date);
        tempText = (TextView) findViewById(R.id.todays_temp);
        tempText.setText(dateTemps.get(0).temp + getString(R.string.tempunits));
        
        tempText = (TextView) findViewById(R.id.tomorrow);
        tempText.setText(dateTemps.get(1).date);
        tempText = (TextView) findViewById(R.id.tomorrows_temp);
        tempText.setText(dateTemps.get(1).temp + getString(R.string.tempunits));
        
        tempText = (TextView) findViewById(R.id.next_day);
        tempText.setText(dateTemps.get(2).date);
        tempText = (TextView) findViewById(R.id.next_days_temp);
        tempText.setText(dateTemps.get(2).temp + getString(R.string.tempunits));

        gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this));

    }
    
    private CharSequence getZip()
    {
    	FrostAlertApp appState = ((FrostAlertApp)getApplication());
    	String foo = appState.alert.getZipcode();
    	return getString(R.string.forzip).concat(" ").concat(foo).concat(getString(R.string.forzipending));
    }
    private CharSequence getLatLong()
    {
    	FrostAlertApp appState = ((FrostAlertApp)getApplication());
    	latlong = appState.alert.getLatLong();
    	return getString(R.string.forlatlong).concat(" ").concat(latlong).concat(getString(R.string.forlatlongending));
    }
    private List<FrostAlertService.DateTemp> getDateTemp(String latlong)
    {
    	FrostAlertApp appState = ((FrostAlertApp)getApplication());
    	return appState.alert.getMinimumTemperatures(latlong);
    }
}