package net.clearfix.adswitcher;

import android.view.View;

public abstract class Ad {
    private boolean available = false;
    private OnAdAvailabilityChanged onAdAvailabilityChanged;

    /**
     * Must return the ad banner
     * @return
     */
    public abstract View getView();

    /**
     * This is called by the ad switcher and you must
     * make sure that you are refreshing your ads here
     */
    public abstract void refresh();

    /**
     * Implement this if the ad has the ability to pause itself.
     * This is called when the view is not being showed and
     * it's recommended to implement it to improve performance
     */
    protected void pause() {
    }

    protected final void setAvailable(boolean available) {
        this.available = available;
        if (onAdAvailabilityChanged != null) {
            onAdAvailabilityChanged.onAdAvailabilityChanged(this);
        }
    }

    public final boolean isAvailable() {
        return available;
    }

    public void setOnAdAvailabilityChanged(OnAdAvailabilityChanged onAdAvailabilityChanged) {
        this.onAdAvailabilityChanged = onAdAvailabilityChanged;
    }
}
