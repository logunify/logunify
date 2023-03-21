package com.logunify.output;

import com.google.common.base.CaseFormat;
import com.logunify.entity.SchemaDefinition;
import com.logunify.output.sinks.EventSink;
import com.logunify.output.sinks.bigquery.BigquerySink;
import lombok.NoArgsConstructor;

import java.io.IOException;

@NoArgsConstructor
public class EventSinkFactory {
    enum SupportedSinkTypes {
        BIG_QUERY
    }

    public EventSink buildSink(SchemaDefinition.OutputDefinition outputDefinition, SchemaDefinition.Schema schema) throws IOException {
        try {
            SupportedSinkTypes.valueOf(CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, outputDefinition.getType()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported sink type: " + outputDefinition.getType());
        }

        // Only one supported sink type for now
        return new BigquerySink(outputDefinition.getCustomConfigs(), schema);
    }
}
