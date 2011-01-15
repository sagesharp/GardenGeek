package com.gardengeek.coldsnap;

import java.util.List;

import com.gardengeek.coldsnap.ColdSnapApp;
import com.gardengeek.coldsnap.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.GridView;

public class CurrentTemp extends Activity {
    
    public void onCreate(Bundle savedInstanceState) {
    	TextView tempText;
    	GridView gridview;
    	List<ColdSnapService.DateTemp> dateTemps;
    	ColdSnapApp appState;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
    	Button button = (Button) findViewById(R.id.okbutton);
    	button.setText("OK");
    	
        setContentView(R.layout.temp);
        
        appState = ((ColdSnapApp)getApplication());
        setZipInView(appState);
        setLatLongInView(appState);

        dateTemps = getDateTemp(appState.alert.getLatLong());
        while (dateTemps.size() < 3)
        	dateTemps.add(new ColdSnapService.DateTemp("Never", "-400"));
        
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
    
    private void setZipInView(ColdSnapApp appState)
    {
    	TextView tempText;

    	if (appState.debug == false)
    		return;
    	String foo = appState.alert.getZipcode();
    	tempText = (TextView) findViewById(R.id.zipcode);
    	tempText.setText(getString(R.string.forzip).concat(" ").concat(foo).concat(getString(R.string.forzipending)));
    }
    private void setLatLongInView(ColdSnapApp appState)
    {
    	TextView tempText;
    	String latlong;

    	if (appState.debug == false)
    		return;
    	latlong = appState.alert.getLatLong();
    	tempText = (TextView) findViewById(R.id.latlong);
    	tempText.setText(getString(R.string.forlatlong).concat(" ").concat(latlong).concat(getString(R.string.forlatlongending)));
    }
    private List<ColdSnapService.DateTemp> getDateTemp(String latlong)
    {
    	ColdSnapApp appState = ((ColdSnapApp)getApplication());
    	return appState.alert.getMinimumTemperatures(latlong);
    }
}