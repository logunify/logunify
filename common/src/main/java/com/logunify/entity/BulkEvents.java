package com.logunify.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkEvents {
    private String apiKey;
    private List<Event> events;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Event {
        private String serializedEvent;
        private String schemaName;
        private String projectName;
    }
}
