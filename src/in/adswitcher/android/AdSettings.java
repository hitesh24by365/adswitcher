package in.adswitcher.android;

import java.util.HashMap;
import java.util.Map;

/**
 * Use this to create and attach AdConfiguration. This allows you to create different configurations and identify
 * them using a key, which can be used in the XML
 */
public class AdSettings {
    private static Map<String, AdConfiguration> ads = new HashMap<String, AdConfiguration>();

    /**
     * @param key the id for the configuration (cannot be null)
     * @param configuration the configuration object (cannot be null)
     */
    public static void addConfiguration(String key, AdConfiguration configuration) {
        if (key == null || configuration == null) {
            throw new RuntimeException("Neither the key nor the configuration can be null");
        }
        ads.put(key, configuration);
    }

    /**
     * @param key id of the configuration to remove
     */
    public static void removeConfiguration(String key) {
        ads.remove(key);
    }

    /**
     * @param key id of the configuration to retrieve
     * @return
     */
    public static AdConfiguration getConfiguration(String key) {
        return ads.get(key);
    }
}
