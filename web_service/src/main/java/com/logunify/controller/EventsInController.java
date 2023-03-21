package com.logunify.controller;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.logunify.entity.BulkEvents;
import com.logunify.lib.schema_builder.ProtobufSchemaBuildingService;
import com.logunify.output.SinkRegistry;
import com.logunify.protobuf_schema.ProtobufSchema;
import com.logunify.schema_loader.SchemaRegistry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class EventsInController {
    Map<String, ProtobufSchema> cachedSchemas = new HashMap<>();

    private final ProtobufSchemaBuildingService schemaGenerator;
    private final SchemaRegistry schemaRegistry;
    private final SinkRegistry sinkRegistry;

    @Autowired
    public EventsInController(
            ProtobufSchemaBuildingService schemaGenerator,
            SchemaRegistry schemas,
            SinkRegistry outputs) {
        this.schemaGenerator = schemaGenerator;
        this.schemaRegistry = schemas;
        this.sinkRegistry = outputs;
    }

    @Data
    @AllArgsConstructor
    public static class Response {
        private boolean success;
    }

    @PostMapping("/api/events/_bulk")
    public ResponseEntity<Response> receiveBulkEvents(@RequestBody BulkEvents bulk) {
        bulk.getEvents().stream().collect(Collectors.groupingBy(event -> buildSchemaKey(event.getProjectName(), event.getSchemaName())))
                .forEach((key, events) -> {
                    var maybeSchemaDefinition = schemaRegistry.getSchemaDefinition(key);
                    if (maybeSchemaDefinition.isEmpty()) {
                        log.error("Schema not found: " + key);
                        return;
                    }

                    var schemaDefinition = maybeSchemaDefinition.get();
                    var schema = schemaGenerator.buildSchema(schemaDefinition, false);
                    var descriptor = schema.getMessageDescriptor(events.get(0).getSchemaName());
                    var messages = events.stream().map(
                            event -> {
                                try {
                                    var message = DynamicMessage.newBuilder(descriptor)
                                            .mergeFrom(Base64.getDecoder().decode(event.getSerializedEvent())).build();
                                    log.debug("Received message:\n{}", message);
                                    return message.toByteString();
                                } catch (InvalidProtocolBufferException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    ).collect(Collectors.toList());

                    var sinks = sinkRegistry.getSinks(schemaDefinition.getKey());
                    sinks.parallelStream().forEach(sink -> {
                        try {
                            sink.write(descriptor, messages);
                        } catch (IOException e) {
                            log.error("Error writing to output", e);
                        }
                    });
                });
        return new ResponseEntity<>(new Response(true), HttpStatus.OK);
    }

    private String buildSchemaKey(String projectName, String schemaName) {
        return String.format(
                "%s/%s",
                projectName,
                schemaName
        );
    }
}
