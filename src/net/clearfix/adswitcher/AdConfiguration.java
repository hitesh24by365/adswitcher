package net.clearfix.adswitcher;

import java.util.ArrayList;
import java.util.List;

public class AdConfiguration {
    private final static long DEFAULT_REFRESH_INTERVAL = 30000;

    private final long interval;
    private final List<Class<? extends Ad>> ads;

    private AdConfiguration(long interval, List<Class<? extends Ad>> ads) {
        this.interval = interval;
        this.ads = ads;
    }

    public List<Class<? extends Ad>> getAds() {
        return ads;
    }

    public long getInterval() {
        return interval;
    }

    public static class Builder {
        private long interval = DEFAULT_REFRESH_INTERVAL;
        private List<Class<? extends Ad>> ads = new ArrayList<Class<? extends Ad>>();

        public Builder setInterval(int interval) {
            this.interval = interval;
            return this;
        }

        public Builder addAd(Class<? extends Ad> clazz) {
            ads.add(clazz);
            return this;
        }

        public AdConfiguration build() {
            if (ads == null || ads.size() == 0) {
                throw new RuntimeException("You must set at least one ad");
            }
            return new AdConfiguration(interval, ads);
        }
    }
}
