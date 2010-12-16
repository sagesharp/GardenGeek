package com.gardengeek.frostalert;

import com.gardengeek.frostalert.R;
import com.gardengeek.frostalert.FrostAlertApp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.GridView;

public class CurrentTemp extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temp);
        TextView tempText = (TextView) findViewById(R.id.zipcode);
        tempText.setText(getZip());
        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this));

    }
    
    private CharSequence getZip()
    {
    	FrostAlertApp appState = ((FrostAlertApp)getApplication());
    	String foo = appState.alert.getZipcode();
    	return getString(R.string.forzip).concat(" ").concat(foo).concat(getString(R.string.forzipending));
    }
}
