package de.doridian.crtdemo.basic.parameters;

public abstract class AbstractParameter {
    protected final Object value;
    public final String preChars;

    public AbstractParameter(String preChars, Object parameter) {
        value = parameter;
        this.preChars = preChars;
    }

    public Object getValue() {
        return value;
    }

    public String getStringValue() {
        return getValue().toString();
    }

    public boolean valueEquals(Object value) {
        return this.value.equals(value);
    }

    public String getAsParameter() {
        return getStringValue();
    }

    @Override
    public String toString() {
        return getAsParameter();
    }
}
