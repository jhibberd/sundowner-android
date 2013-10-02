package com.example.geotag;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/* Abstracts logic for obtaining current location; based on:
 * http://developer.android.com/guide/topics/location/strategies.html
 */
public class LocationAgent implements LocationListener {

    public interface Delegate {
        public void onObtainedCurrentLocation(Location location);
    }

    private LocationManager locationManager;
    private Delegate delegate;

    public void getCurrentLocation(
            LocationManager locationManager, Delegate delegate) {

        this.locationManager = locationManager;
        this.delegate = delegate;

        // begin listening for location updates
        // TODO it might be worth immediately getting the last known location from the cache
        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {

        // stop listening to location updates to preserve battery
        locationManager.removeUpdates(this);

        // notify the delegate that a location has been obtained
        delegate.onObtainedCurrentLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}
