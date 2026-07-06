package com.theblind.todo.e2e;

import java.util.HashMap;
import java.util.Map;

// Holds shared data between step definition classes within a single scenario.
// A fresh instance is created for each scenario, so values never carry over between tests.
// Useful keys: "jwt", "lastCreatedTaskId", "registeredUsername", "registeredPassword"
public class ScenarioContext {

    private final Map<String, Object> context = new HashMap<>();

    // Store a value
    public void set(String key, Object value) {
        context.put(key, value);
    }

    // Retrieve a value cast to the expected type
    public <T> T get(String key, Class<T> type) {
        return type.cast(context.get(key));
    }

    // Check whether a value exists for this key
    public boolean has(String key) {
        return context.containsKey(key) && context.get(key) != null;
    }

    // Clear all stored values
    public void clear() {
        context.clear();
    }
}
