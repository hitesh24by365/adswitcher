package in.adswitcher.android;

public class AdParameter {
    private final Class clazz;
    private final Object value;

    public AdParameter(Class key, Object value) {
        this.clazz = key;
        this.value = value;
    }

    public AdParameter(Object value) {
        this.clazz = Object.class;
        this.value = value;
    }

    public Class getKey() {
        return clazz;
    }

    public Object getValue() {
        return value;
    }
}
