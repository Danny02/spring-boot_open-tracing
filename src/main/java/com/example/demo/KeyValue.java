package com.example.demo;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.cassandra.core.mapping.Table;

@Table
public class KeyValue {
    @Id
    private final String key;

    private final String value;

    @PersistenceConstructor
    public KeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
