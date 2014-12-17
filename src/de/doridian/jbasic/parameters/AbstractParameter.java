package de.doridian.jbasic.parameters;

public abstract class AbstractParameter {
    protected final Object value;

    public AbstractParameter(Object parameter) {
        value = parameter;
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

    public String getSeparator() {
        return " ";
    }

    @Override
    public String toString() {
        return getAsParameter();
    }
}
