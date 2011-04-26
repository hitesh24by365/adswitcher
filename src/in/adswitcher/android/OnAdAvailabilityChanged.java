package in.adswitcher.android;

interface OnAdAvailabilityChanged {
    /**
     * Called when the availability of one of the ad holders has changed
     * @param adHolder
     */
    void onAdAvailabilityChanged(AdHolder adHolder);
}
