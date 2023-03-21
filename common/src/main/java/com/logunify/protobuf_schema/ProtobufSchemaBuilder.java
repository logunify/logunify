package com.logunify.protobuf_schema;

import com.google.common.base.CaseFormat;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.logunify.entity.SchemaDefinition;
import com.logunify.protobuf_schema.definitions.EnumDefinition;
import com.logunify.protobuf_schema.definitions.MessageDefinition;

import java.util.Optional;

public class ProtobufSchemaBuilder {
    private static final String dependencyDescriptorProto = "google/protobuf/descriptor.proto";

    private final ProtobufSchemaExtensions extensions = new ProtobufSchemaExtensions();

    public ProtobufSchemaBuilder() throws Descriptors.DescriptorValidationException {
    }

    private String getProjectName(SchemaDefinition schemaDefinition) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, schemaDefinition.getProjectName());
    }

    private String getPackageName(SchemaDefinition schemaDefinition, String orgName) {
        if (Optional.ofNullable(orgName).map(name -> !name.isEmpty()).orElse(false)) {
            return String.format("com.%s.%s",
                    CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, orgName),
                    getProjectName(schemaDefinition)
            );
        }
        return String.format("com.%s", getProjectName(schemaDefinition));
    }

    private String getSchemaName(SchemaDefinition schemaDefinition) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, schemaDefinition.getSchema().getName());
    }

    private EnumDefinition buildEnumDefinition(SchemaDefinition.EnumDefinition enumDefinition) {
        var builder = EnumDefinition.newBuilder(enumDefinition.getName());
        enumDefinition.getValues().forEach(enumValue -> {
            builder.addValue(enumValue.getValue(), enumValue.getFieldNumber());
        });
        return builder.build();
    }

    public ProtobufSchema buildSchema(String orgName, SchemaDefinition schemaDefinition, boolean includePackage) {

        try {
            var schema = schemaDefinition.getSchema();
            var schemaBuilder = ProtobufSchema.newBuilder();

            schema.getEnums().forEach(enumDefinition ->
                    schemaBuilder.addEnumDefinition(buildEnumDefinition(enumDefinition)));

            schemaBuilder.addMessageDefinition(buildMessageDefinition(schemaDefinition));

            var fileOptions = DescriptorProtos.FileOptions
                    .newBuilder()
                    .setJavaOuterClassname(schemaDefinition.getSchema().getName() + "Schema")
                    .build();

            var builder = schemaBuilder.setName(schemaDefinition.getSchema().getName())
                    .setProjectName(getProjectName(schemaDefinition))
                    .setOrgName(orgName)
                    .addDependency(dependencyDescriptorProto)
                    .setFileOption(fileOptions);
            if (includePackage) {
                builder.setPackage(getPackageName(schemaDefinition, orgName));
            }
            return builder.build();
        } catch (Descriptors.DescriptorValidationException e) {
            throw new RuntimeException(e);
        }
    }

    private MessageDefinition buildMessageDefinition(SchemaDefinition schemaDefinition) {
        var builder = MessageDefinition.newBuilder(schemaDefinition.getSchema().getName());
        schemaDefinition.getSchema().getFields().forEach(schemaField -> {
            switch (schemaField.getType().toUpperCase()) {
                case "ARRAY":
                    builder.addField(
                            "repeated",
                            schemaField.getArrayType(),
                            schemaField.getName(),
                            schemaField.getFieldNumber()
                    );
                    break;
                case "MAP":
                    var mapTypeName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, schemaField.getName()) + "Entry";
                    var mapMessageDef = MessageDefinition.newBuilder(mapTypeName)
                            .addField("optional", schemaField.getKeyType(), "key", 1)
                            .addField("optional", schemaField.getValueType(), "value", 2)
                            .setOptions(DescriptorProtos.MessageOptions.newBuilder().setMapEntry(true).build())
                            .build();
                    builder.addMessageDefinition(mapMessageDef)
                            .addField(
                                    "repeated",
                                    mapTypeName,
                                    schemaField.getName(),
                                    schemaField.getFieldNumber()
                            );
                    break;
                // enum type.
                default:
                    builder.addField(
                            "optional",
                            schemaField.getType(),
                            schemaField.getName(),
                            schemaField.getFieldNumber()
                    );
                    break;
            }
        });

        return builder.addSchemaNameExtension()
                .addProjectNameExtension()
                .addEnabledCanonicalFieldsExtension()
                .setMessageOptions(
                        DescriptorProtos.MessageOptions.newBuilder()
                                .setExtension(extensions.getSchemaNameExt(), schemaDefinition.getSchema().getName())
                                .setExtension(extensions.getProjectNameExt(), schemaDefinition.getProjectName())
                                .build()
                ).build();
    }
}
