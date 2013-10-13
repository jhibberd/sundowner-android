package com.sundowner.util;

import android.os.Handler;

// used when an object wants to be notified of changes to a value, but insists on a minimum time
// interval between notifications
public class Pulse<T> {

    public interface Delegate {
        public void onPulse(Object value);
    }

    private T value;
    private Runnable pulseRunnable;
    private long lastPulse = -1;
    private final int minPulseInterval;
    private final Delegate delegate;
    private final Handler handler;

    public Pulse(int minPulseInterval, Delegate delegate) {
        this.minPulseInterval = minPulseInterval;
        this.delegate = delegate;
        this.handler = new Handler();
    }

    public void set(T value) {

        this.value = value;

        // cancel any existing waiting pulse callbacks
        if (pulseRunnable != null) {
            handler.removeCallbacks(pulseRunnable);
            pulseRunnable = null;
        }

        // calculate the number of seconds until the next pulse is due
        long now = System.currentTimeMillis() / 1000L;
        long secondsToNextPulse;
        if (lastPulse == -1) {
            secondsToNextPulse = 0;
        } else {
            secondsToNextPulse = minPulseInterval - (now - lastPulse);
        }

        if (secondsToNextPulse <= 0) {
            lastPulse = now;
            delegate.onPulse(value);

        } else {
            pulseRunnable = new Runnable() {
                @Override
                public void run() {
                    flush();
                }
            };
            handler.postDelayed(pulseRunnable, secondsToNextPulse * 1000L);
        }

    }

    public void flush() {
        // Ignore the flush request if no value has yet been set. The caller will have to wait
        // until the first value is received naturally.
        if (value == null) {
            return;
        }
        lastPulse = -1;
        set(value);
    }
}
