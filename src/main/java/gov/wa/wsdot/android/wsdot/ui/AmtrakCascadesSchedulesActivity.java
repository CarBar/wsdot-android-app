/*
 * Copyright (c) 2015 Washington State Department of Transportation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package gov.wa.wsdot.android.wsdot.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.AmtrakCascadesStationItem;
import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class AmtrakCascadesSchedulesActivity extends ActionBarActivity
        implements ConnectionCallbacks, OnConnectionFailedListener {
	
    private List<AmtrakCascadesStationItem> amtrakStationItems = new ArrayList<AmtrakCascadesStationItem>();
    private Map<String, String> stationsMap = new HashMap<String, String>();
    private Map<String, String> daysOfWeekMap = new HashMap<String, String>();
    private Spinner daySpinner;
    private Spinner originSpinner;
    private Spinner destinationSpinner;
    
    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;

    protected double mLatitude;
    protected double mLongitude;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.amtrakcascades_schedules);		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
        getDaysOfWeek();
        getAmtrakStations();
        getToLocation();
        buildGoogleApiClient();
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
    
    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    
    /**
     * 
     */
    @SuppressLint("SimpleDateFormat")
    private void getDaysOfWeek() {
        DateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE");
        DateFormat statusDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dayOfWeekFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        statusDateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        daySpinner = (Spinner) findViewById(R.id.day_spinner);
        List<String> daysOfWeek = new ArrayList<String>();
        
        Date startDate = new Date();
        
        for (int i = 0; i < 7; i++) {
            Date nextDay = new Date(startDate.getTime() + i * 24 * 3600 * 1000);
            daysOfWeek.add(dayOfWeekFormat.format(nextDay));
            daysOfWeekMap.put(dayOfWeekFormat.format(nextDay), statusDateFormat.format(nextDay));            
        }
        
        ArrayAdapter<String> dayOfWeekArrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, daysOfWeek);
        
        dayOfWeekArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayOfWeekArrayAdapter);
    }

    /**
     * 
     */
    private void getAmtrakStations() {
        amtrakStationItems.add(new AmtrakCascadesStationItem("VAC", "Vancouver, BC", 1, 49.2737293, -123.0979175));
        amtrakStationItems.add(new AmtrakCascadesStationItem("BEL", "Bellingham, WA", 2, 48.720423, -122.5109386));
        amtrakStationItems.add(new AmtrakCascadesStationItem("MVW", "Mount Vernon, WA", 3, 48.4185923, -122.334973));
        amtrakStationItems.add(new AmtrakCascadesStationItem("STW", "Stanwood, WA", 4, 48.2417732, -122.3495322));
        amtrakStationItems.add(new AmtrakCascadesStationItem("EVR", "Everett, WA", 5, 47.975512, -122.197854));
        amtrakStationItems.add(new AmtrakCascadesStationItem("EDM", "Edmonds, WA", 6, 47.8111305, -122.3841639));
        amtrakStationItems.add(new AmtrakCascadesStationItem("SEA", "Seattle, WA", 7, 47.6001899, -122.3314322));
        amtrakStationItems.add(new AmtrakCascadesStationItem("TUK", "Tukwila, WA", 8, 47.461079, -122.242693));
        amtrakStationItems.add(new AmtrakCascadesStationItem("TAC", "Tacoma, WA", 9, 47.2419939, -122.4205623));
        amtrakStationItems.add(new AmtrakCascadesStationItem("OLW", "Olympia/Lacey, WA", 10, 46.9913576, -122.793982));
        amtrakStationItems.add(new AmtrakCascadesStationItem("CTL", "Centralia, WA", 11, 46.7177596, -122.9528291));
        amtrakStationItems.add(new AmtrakCascadesStationItem("KEL", "Kelso/Longview, WA", 12, 46.1422504, -122.9132438));
        amtrakStationItems.add(new AmtrakCascadesStationItem("VAN", "Vancouver, WA", 13, 45.6294472, -122.685568));
        amtrakStationItems.add(new AmtrakCascadesStationItem("PDX", "Portland, OR", 14, 45.528639, -122.676284));
        amtrakStationItems.add(new AmtrakCascadesStationItem("ORC", "Oregon City, OR", 15, 45.3659422, -122.5960671));
        amtrakStationItems.add(new AmtrakCascadesStationItem("SLM", "Salem, OR", 16, 44.9323665, -123.0281591));
        amtrakStationItems.add(new AmtrakCascadesStationItem("ALY", "Albany, OR", 17, 44.6300975, -123.1041787));
        amtrakStationItems.add(new AmtrakCascadesStationItem("EUG", "Eugene, OR", 18, 44.055506, -123.094523));

        stationsMap.put("Vancouver, BC", "VAC");
        stationsMap.put("Bellingham, WA", "BEL");
        stationsMap.put("Mount Vernon, WA", "MVW");
        stationsMap.put("Stanwood, WA", "STW");
        stationsMap.put("Everett, WA", "EVR");
        stationsMap.put("Edmonds, WA", "EDM");
        stationsMap.put("Seattle, WA", "SEA");
        stationsMap.put("Tukwila, WA", "TUK");
        stationsMap.put("Tacoma, WA", "TAC");
        stationsMap.put("Olympia/Lacey, WA", "OLW");
        stationsMap.put("Centralia, WA", "CTL");
        stationsMap.put("Kelso/Longview, WA", "KEL");
        stationsMap.put("Vancouver, WA", "VAN");
        stationsMap.put("Portland, OR", "PDX");
        stationsMap.put("Oregon City, OR", "ORC");
        stationsMap.put("Salem, OR", "SLM");
        stationsMap.put("Albany, OR", "ALY");
        stationsMap.put("Eugene, OR", "EUG");
        
        
        Collections.sort(amtrakStationItems, AmtrakCascadesStationItem.stationNameComparator);        
    }

    /**
     * Haversine formula
     * 
     * Provides great-circle distances between two points on a sphere from their longitudes and latitudes
     * 
     * http://en.wikipedia.org/wiki/Haversine_formula
     * 
     * @param latitude
     * @param longitude
     */
    protected void getDistanceFromStation(double latitude, double longitude) {
        for (AmtrakCascadesStationItem station: amtrakStationItems) {
            double earthRadius = 3958.75; // miles
            double dLat = Math.toRadians(station.getLatitude() - latitude);
            double dLng = Math.toRadians(station.getLongitude() - longitude);
            double sindLat = Math.sin(dLat / 2);
            double sindLng = Math.sin(dLng / 2);
            double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                    * Math.cos(Math.toRadians(latitude))
                    * Math.cos(Math.toRadians(station.getLatitude()));
            
            double c = 2 * Math.asin(Math.sqrt(a));
            int distance = (int) Math.round(earthRadius * c);

            station.setDistance(distance);
        }
        
        getFromLocation();
    }
    
    private void getFromLocation() {
        originSpinner = (Spinner) findViewById(R.id.origin_spinner);
        int stationIndex = 0;
        Collections.sort(amtrakStationItems, AmtrakCascadesStationItem.stationDistanceComparator);
        String closestStation = amtrakStationItems.get(0).getStationName();
        Collections.sort(amtrakStationItems, AmtrakCascadesStationItem.stationNameComparator);
        List<String> stations = new ArrayList<String>();
        
        for (AmtrakCascadesStationItem station: amtrakStationItems) {
            stations.add(station.getStationName());
        }
        
        stationIndex = stations.indexOf(closestStation);
        
        
        ArrayAdapter<String> stationsArrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, stations);
        
        stationsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        originSpinner.setAdapter(stationsArrayAdapter);
        originSpinner.setSelection(stationIndex);
    }
    
    private void getToLocation() {
        destinationSpinner = (Spinner) findViewById(R.id.destination_spinner);
        
        List<String> stations = new ArrayList<String>();
        
        for (AmtrakCascadesStationItem station: amtrakStationItems) {
            stations.add(station.getStationName());
        }
        
        ArrayAdapter<String> stationsArrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, stations);
        
        stationsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        destinationSpinner.setAdapter(stationsArrayAdapter);
    }

    /**
     * 
     * @param view
     */
    public void checkSchedules(View view) {
        String day = daySpinner.getSelectedItem().toString();
        String dayId = daysOfWeekMap.get(day);
        String origin = originSpinner.getSelectedItem().toString();
        String destination = destinationSpinner.getSelectedItem().toString();
        String originId = stationsMap.get(origin);
        String destinationId = stationsMap.get(destination);
        
        Toast toast = Toast.makeText(this, "Date: " + dayId + " Origin: " + originId + " Destination: " + destinationId, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void onConnectionFailed(ConnectionResult arg0) {
        // TODO Auto-generated method stub
        
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitude = mLastLocation.getLatitude();
            mLongitude = mLastLocation.getLongitude();
            getDistanceFromStation(mLatitude, mLongitude);
        } else {
            Toast.makeText(this, "Can't determine last known location.", Toast.LENGTH_LONG).show();
        }
    }

    public void onConnectionSuspended(int arg0) {
        // TODO Auto-generated method stub
    }

}
