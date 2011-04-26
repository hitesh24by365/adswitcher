package net.clearfix.adswitcher;

public class Parameter {
    private final Class clazz;
    private final Object value;

    public Parameter(Class clazz, Object value) {
        this.clazz = clazz;
        this.value = value;
    }

    public Class getKey() {
        return clazz;
    }

    public Object getValue() {
        return value;
    }
}
