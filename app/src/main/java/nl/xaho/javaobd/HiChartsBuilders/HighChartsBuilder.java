package nl.xaho.javaobd.HiChartsBuilders;

import java.lang.reflect.ParameterizedType;

public abstract class HighChartsBuilder<T> {
    protected T object;

    public HighChartsBuilder() {
        ParameterizedType superClass = (ParameterizedType) getClass().getGenericSuperclass();
        Class<T> type = (Class<T>) superClass.getActualTypeArguments()[0];
        try {
            object = type.newInstance();
        } catch (Exception e) {
            // Oops, no default constructor
            throw new RuntimeException(e);
        }
    }

    public T build() {
        return object;
    }
}
