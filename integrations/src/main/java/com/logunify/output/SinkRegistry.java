package com.logunify.output;

import com.logunify.output.sinks.EventSink;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SinkRegistry {
    private final Map<String, List<EventSink>> sinks = new HashMap<>();

    public List<EventSink> getSinks(String key) {
        return sinks.getOrDefault(key, List.of());
    }

    public Map<String, List<String>> getAllSinks() {
        return sinks.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().map(EventSink::getId).collect(Collectors.toList())));
    }

    public void registerSink(String key, List<EventSink> sinksToRegister) {
        sinks.put(key, sinksToRegister);
    }
}
