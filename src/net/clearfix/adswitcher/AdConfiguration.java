package net.clearfix.adswitcher;

import java.util.ArrayList;
import java.util.List;

public class AdConfiguration {
    private final static long DEFAULT_REFRESH_INTERVAL = 30000;

    private final long interval;
    private final List<Ad> ads;

    private AdConfiguration(long interval, List<Ad> ads) {
        this.interval = interval;
        this.ads = ads;
    }

    public List<Ad> getAds() {
        return ads;
    }

    public long getInterval() {
        return interval;
    }

    public static class Builder {
        private long interval = DEFAULT_REFRESH_INTERVAL;
        private List<Ad> ads = new ArrayList<Ad>();

        public Builder setInterval(int interval) {
            this.interval = interval;
            return this;
        }

        public Builder addAd(Ad ad) {
            ads.add(ad);
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
