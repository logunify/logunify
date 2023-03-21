package com.logunify.lib.schema_builder;

import com.google.protobuf.Descriptors;
import com.logunify.configuration.ConfigProvider;
import com.logunify.entity.SchemaDefinition;
import com.logunify.protobuf_schema.ProtobufSchema;
import com.logunify.protobuf_schema.ProtobufSchemaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProtobufSchemaBuildingService {
    private final ConfigProvider.SchemaConfig schemaConfig;
    private final ProtobufSchemaBuilder schemaBuilder;

    @Autowired
    public ProtobufSchemaBuildingService(ConfigProvider.SchemaConfig schemaConfig) throws Descriptors.DescriptorValidationException {
        this.schemaConfig = schemaConfig;
        this.schemaBuilder = new ProtobufSchemaBuilder();
    }

    public ProtobufSchema buildSchema(SchemaDefinition schemaDefinition, boolean includePackage) {
        return schemaBuilder.buildSchema(schemaConfig.getOrgName(), schemaDefinition, includePackage);
    }
}
