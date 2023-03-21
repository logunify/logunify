package com.logunify.controller;

import com.logunify.output.SinkRegistry;
import com.logunify.schema_loader.SchemaRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class RegistryInfoController {
    private final SchemaRegistry schemaRegistry;
    private final SinkRegistry sinkRegistry;

    @Autowired
    public RegistryInfoController(SchemaRegistry schemaRegistry, SinkRegistry sinkRegistry) {
        this.schemaRegistry = schemaRegistry;
        this.sinkRegistry = sinkRegistry;
    }

    @GetMapping("/api/info/schemas")
    public String getSchemas() {
        var schemas = schemaRegistry.getSchemaDefinitions();
        return schemas.keySet().toString();
    }

    @GetMapping("/api/info/sinks")
    public Map<String, List<String>> getSinks() {
        var sinks = sinkRegistry.getAllSinks();
        return sinks;
    }
}
