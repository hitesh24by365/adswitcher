package in.adswitcher.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdConfiguration {
    private final static long DEFAULT_REFRESH_INTERVAL = 30000;

    private final long interval;
    private final List<Class<? extends Ad>> ads;
    private final Map<Class<? extends Ad>, List<Parameter>> params;

    private AdConfiguration(long interval, List<Class<? extends Ad>> ads, Map<Class<? extends Ad>, List<Parameter>> params) {
        this.interval = interval;
        this.ads = ads;
        this.params = params;
    }

    public List<Class<? extends Ad>> getAds() {
        return ads;
    }

    public long getInterval() {
        return interval;
    }

    public List<Parameter> getParams(Class<? extends Ad> clazz) {
        return params.get(clazz);
    }

    public static class Builder {
        private long interval = DEFAULT_REFRESH_INTERVAL;
        private List<Class<? extends Ad>> ads = new ArrayList<Class<? extends Ad>>();
        private Map<Class<? extends Ad>, List<Parameter>> params = new HashMap<Class<? extends Ad>, List<Parameter>>();

        public Builder setInterval(int interval) {
            this.interval = interval;
            return this;
        }

        public Builder addAd(Class<? extends Ad> clazz) {
            if (ads.contains(clazz)) {
                return this;
            }
            ads.add(clazz);
            return this;
        }

        public Builder addAd(Class<? extends Ad> clazz, List<Parameter> params) {
            this.params.put(clazz, params);
            if (ads.contains(clazz)) {
                return this;
            }
            ads.add(clazz);
            return this;
        }

        public AdConfiguration build() {
            if (ads == null || ads.size() == 0) {
                throw new RuntimeException("You must set at least one ad");
            }
            return new AdConfiguration(interval, ads, params);
        }
    }
}
