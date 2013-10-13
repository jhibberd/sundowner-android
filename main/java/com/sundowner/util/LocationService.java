package com.sundowner.util;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

// service for obtaining the device's location
// http://developer.android.com/guide/topics/location/strategies.html
public class LocationService extends Service {

    public interface Delegate {
        public void onLocationUpdate(Location location);
    }

    private static int MIN_PULSE_INTERVAL = 30; // seconds
    private final LocationServiceBinder binder = new LocationServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocationServiceBinder extends Binder implements LocationListener, Pulse.Delegate {

        private Delegate delegate;
        private LocationManager locationManager;
        private Location currentLocation;
        private Pulse<Location> locationPulse;

        public void setDelegate(Delegate delegate) {
            // NOTE: Although the bound service model allows for multiple concurrent clients this
            // implementation only allows for a single delegate. This is fine for now as only a
            // single activity will need to receive location updates at a time, but may pose a
            // problem in the future.
            locationPulse = new Pulse<Location>(MIN_PULSE_INTERVAL, this);
            this.delegate = delegate;
        }

        @Override
        public void onLocationChanged(Location location) {
            currentLocation = location;
            if (locationPulse != null) {
                locationPulse.set(location);
            }
        }

        @Override
        public void onPulse(Object value) {
            delegate.onLocationUpdate((Location)value);
        }

        public void flushLocation() {
            if (locationPulse != null) {
                locationPulse.flush();
            }
        }

        public Location getCurrentLocation() {
            return currentLocation;
        }

        private void startListening() {
            // request location updates from both the the GPS and network providers
            locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0, // min time between updates
                    0, // min distance between updates
                    this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }

        private void stopListening() {
            locationManager.removeUpdates(this);
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

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        binder.startListening();
    }

    @Override
    public void onDestroy() {
        binder.stopListening();
        super.onDestroy();
    }
}
