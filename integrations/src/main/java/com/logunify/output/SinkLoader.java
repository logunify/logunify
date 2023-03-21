package com.logunify.output;

import com.logunify.entity.SchemaDefinition;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class SinkLoader {
    private final SinkRegistry sinkRegistry;

    public SinkLoader(SinkRegistry sinkRegistry) {
        this.sinkRegistry = sinkRegistry;
    }

    private final EventSinkFactory factory = new EventSinkFactory();

    public void loadSinks(List<SchemaDefinition> schemaDefinitions) {
        log.info("Loading sinks for {}", schemaDefinitions.stream().map(SchemaDefinition::getKey).collect(Collectors.joining(",")));
        schemaDefinitions.forEach(schemaDefinition -> {
            var sinks = schemaDefinition.getOutputs().stream().map(outputDefinition -> {
                try {
                    var sink = factory.buildSink(outputDefinition, schemaDefinition.getSchema());
                    log.info("Loaded {} sink for {}", sink.getClass(), schemaDefinition.getKey());
                    return sink;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());

            sinkRegistry.registerSink(
                    schemaDefinition.getKey(),
                    sinks
            );
        });
    }
}
