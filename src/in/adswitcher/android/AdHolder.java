package in.adswitcher.android;

import android.content.Context;
import android.view.View;

/**
 * Holds an Ad and its state. Important: you can create custom constructors, but the first parameter of them
 * have to be a Context instance, which must be used to create the ad view.
 */
public abstract class AdHolder {
    private boolean available = false;
    private OnAdAvailabilityChanged onAdAvailabilityChanged;
    private final Context context;

    public AdHolder(Context context) {
        this.context = context;
    }

    /**
     * Must return the ad banner
     * @return
     */
    public abstract View getView();

    /**
     * This is called by the ad switcher and you must
     * make sure that you are refreshing your ads here.
     * <p>Examples:</p>
     * <p><code>mobclixAd.getAd(); // for Mobclix</code><br/>
     * <code>adView.loadAd(new AdRequest()); // for AdMob</code></p>
     */
    public abstract void refresh();

    /**
     * Implement this if the ad has the ability to pause itself.
     * This is called when the view is not being showed and
     * it's recommended to implement it to improve performance
     */
    protected void pause() {
    }

    /**
     * Use this to change the availability of the ad. Most ad SDKs
     * provide listeners for their ads that will allow you to know
     * whether the ad was served or not.
     * @param available
     */
    protected final void setAvailable(boolean available) {
        this.available = available;
        if (onAdAvailabilityChanged != null) {
            onAdAvailabilityChanged.onAdAvailabilityChanged(this);
        }
    }

    /**
     * @return true if the ad was served and can be shown
     */
    public final boolean isAvailable() {
        return available;
    }

    void setOnAdAvailabilityChanged(OnAdAvailabilityChanged onAdAvailabilityChanged) {
        this.onAdAvailabilityChanged = onAdAvailabilityChanged;
    }

    public Context getContext() {
        return context;
    }
}
