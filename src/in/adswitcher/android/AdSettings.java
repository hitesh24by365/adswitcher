package in.adswitcher.android;

import java.util.HashMap;
import java.util.Map;

public class AdSettings {
    private static Map<String, AdConfiguration> ads = new HashMap<String, AdConfiguration>();

    public static void addConfiguration(String key, AdConfiguration configuration) {
        if (key == null || configuration == null) {
            throw new RuntimeException("Neither the key nor the configuration can be null");
        }
        ads.put(key, configuration);
    }

    public static void removeConfiguration(String key) {
        ads.remove(key);
    }

    public static AdConfiguration getConfiguration(String key) {
        return ads.get(key);
    }
}
