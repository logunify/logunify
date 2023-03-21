package com.logunify.controller;

import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.UnknownFieldSet;
import com.logunify.entity.SchemaDefinition;
import com.logunify.lib.schema_builder.ProtobufSchemaBuildingService;
import com.logunify.protobuf_schema.ProtobufSchema;
import com.logunify.protobuf_schema.definitions.EnumDefinition;
import com.logunify.protobuf_schema.definitions.MessageDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.Charset;
import java.util.Base64;

@RestController
public class GenerateSchemaController {
    private final ProtobufSchemaBuildingService schemaGenerator;

    @Autowired
    public GenerateSchemaController(ProtobufSchemaBuildingService schemaGenerator) {
        this.schemaGenerator = schemaGenerator;
    }

    @PostMapping("/generate-schema")
    public byte[] generateSchema(@RequestBody SchemaDefinition schemaDefinition) {
        var dynamicSchema = schemaGenerator.buildSchema(schemaDefinition, true);
        System.out.println(new String(Base64.getEncoder().encode(dynamicSchema.toByteArray())));
        System.out.println(dynamicSchema.toString());

        return dynamicSchema.toByteArray();
    }

    @GetMapping("/get-schema-dev")
    public byte[] getSchemaDev(
            @RequestParam(name = "project-name") String projectName,
            @RequestParam(name = "schema-name") String schemaName
    ) {
        try {
            return getProtoSchemaDev(projectName, schemaName).toByteArray();
        } catch (Descriptors.DescriptorValidationException e) {
            throw new RuntimeException(e);
        }
    }

    private static ProtobufSchema getProtoSchemaDev(String projectName, String schemaName) throws Descriptors.DescriptorValidationException {
        // create a map type
        MessageDefinition strToStrMap =
                // the msgTypeName need to be 100% match with the camel case of the map field name + "Entry"
                MessageDefinition.newBuilder("AdditionalDataEntry")
                        .addField("optional", "string", "key", 1)
                        .addField("optional", "string", "value", 2)
                        .setOptions(DescriptorProtos.MessageOptions.newBuilder().setMapEntry(true).build())
                        .build();

        // custom enum
        var enumDefinition = EnumDefinition.newBuilder("EVENT_TYPE")
                .addValue("CLICK", 1)
                .addValue("IMPRESSION", 2)
                .addValue("CONVERT", 3)
                .build();

        var eventDef =
                MessageDefinition.newBuilder(schemaName)
                        .addEnumDefinition(enumDefinition)
                        .addField("optional", "string", "session_id", 1)
                        .addField("required", "int32", "user_id", 2)
                        .addField("optional", "EVENT_TYPE", "event_type", 3)
                        .addField("repeated", "AdditionalDataEntry", "additional_data", 4)
                        .addMessageDefinition(strToStrMap)
                        .addSchemaNameExtension()
                        .addProjectNameExtension()
                        .setMessageOptions(DescriptorProtos.MessageOptions.newBuilder()
                                .setUnknownFields(
                                        UnknownFieldSet.newBuilder()
                                                .addField(1001,
                                                        UnknownFieldSet.Field.newBuilder()
                                                                .addLengthDelimited(ByteString.copyFrom("event_schema", Charset.defaultCharset()))
                                                                .build()
                                                ).build()
                                )
                                .build())
                        .build();


        // build the schema
        var schemaBuilder = ProtobufSchema.newBuilder();
        var fileOptions = DescriptorProtos.FileOptions
                .newBuilder()
                .setJavaOuterClassname(schemaName + "OuterClass")
                .setUnknownFields(UnknownFieldSet.newBuilder().addField(1001,
                        UnknownFieldSet.Field.newBuilder()
                                .addLengthDelimited(ByteString.copyFrom("test_key",
                                        Charset.defaultCharset())).build()).build())
                .build();
        return
                schemaBuilder.setName(schemaName)
                        .setPackage("com.logunify")
                        .addMessageDefinition(eventDef)
                        .setFileOption(fileOptions)
                        .setProjectName(projectName)
                        .addDependency("google/protobuf/descriptor.proto")
                        .build();
    }
}