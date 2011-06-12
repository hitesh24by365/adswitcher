package com.egoclean.adswitcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds the configuration for an specific ad: the refresh refreshInterval,
 * the list of AdHolders to rotate and the custom parameters to
 * initialize complex AdHolder objects
 */
public class AdConfiguration {
    private final static long DEFAULT_REFRESH_INTERVAL = 30000;
    private final static long DEFAULT_ROTATE_INTERVAL = 120000;

    private final long mRefreshInterval;
    private final long rotateInterval;
    private final List<Class<? extends AdHolder>> mAdHolders;
    private final Map<Class<? extends AdHolder>, List<AdParameter>> mParams;

    private AdConfiguration(long refreshInterval, List<Class<? extends AdHolder>> ads, Map<Class<? extends AdHolder>,
            List<AdParameter>> params, long rotateInterval) {
        this.mRefreshInterval = refreshInterval;
        this.mAdHolders = ads;
        this.mParams = params;
        this.rotateInterval = rotateInterval;
    }

    /**
     * @return the list of ad holders
     */
    public List<Class<? extends AdHolder>> getAdHolders() {
        return mAdHolders;
    }

    /**
     * @return the refresh refresh interval
     */
    public long getRefreshInterval() {
        return mRefreshInterval;
    }

    /**
     * @param clazz the AdHolder class
     * @return the list of AdParameters that belongs to this specific AdHolder class
     */
    public List<AdParameter> getParams(Class<? extends AdHolder> clazz) {
        return mParams.get(clazz);
    }

    public long getRotateInterval() {
        return rotateInterval;
    }

    public static class Builder {
        private long refreshInterval = DEFAULT_REFRESH_INTERVAL;
        private long rotateInterval = DEFAULT_ROTATE_INTERVAL;
        private List<Class<? extends AdHolder>> ads = new ArrayList<Class<? extends AdHolder>>();
        private Map<Class<? extends AdHolder>, List<AdParameter>> params = new HashMap<Class<? extends AdHolder>, List<AdParameter>>();

        /**
         * @param refreshInterval the interval of time between each refreshing call to the ad holders (milliseconds).
         * Use -1 to avoid refreshing ads (don't recommended). By default this is set to 30 secs
         * @return the builder
         */
        public Builder setRefreshInterval(int refreshInterval) {
            this.refreshInterval = refreshInterval;
            return this;
        }

        /**
         * @param rotateInterval the interval of time between each ad rotation (milliseconds). Use -1 to avoid rotating
         * ads. This is set to 120secs by default.
         * @return the builder
         */
        public Builder setRotateInterval(int rotateInterval) {
            this.rotateInterval = rotateInterval;
            return this;
        }

        /**
         * @param clazz the AdHolder class to add
         * @return the builder
         */
        public Builder addAd(Class<? extends AdHolder> clazz) {
            if (ads.contains(clazz)) {
                return this;
            }
            ads.add(clazz);
            return this;
        }

        /**
         * @param clazz the AdHolder class to add
         * @param params the custom params needed to instantiate the AdHolder class. This most include the
         * extra parameters that the constructor has, excluding the Context object.
         * <p><b>Important:</b> all AdHolder objects MUST have a constructor whose first parameter
         * is a Context object.</p>
         * @return the builder
         * @see AdHolder
         */
        public Builder addAd(Class<? extends AdHolder> clazz, List<AdParameter> params) {
            this.params.put(clazz, params);
            if (ads.contains(clazz)) {
                return this;
            }
            ads.add(clazz);
            return this;
        }

        /**
         * @return the AdConfiguration object
         */
        public AdConfiguration build() {
            if (ads == null || ads.size() == 0) {
                throw new RuntimeException("You must set at least one ad");
            }
            return new AdConfiguration(refreshInterval, ads, params, rotateInterval);
        }
    }
}
