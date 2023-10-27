package com.logunify.protobuf_schema;

import com.google.protobuf.*;
import lombok.Getter;

public class ProtobufSchemaExtensions {
    @Getter
    private final ExtensionRegistry extensionRegistry;
    @Getter
    private final GeneratedMessage.GeneratedExtension<DescriptorProtos.MessageOptions, String> schemaNameExt;
    @Getter
    private final GeneratedMessage.GeneratedExtension<DescriptorProtos.MessageOptions, String> projectNameExt;
    @Getter
    private final GeneratedMessage.GeneratedExtension<DescriptorProtos.MessageOptions, String> enabledCanonicalFieldsExt;

    public ProtobufSchemaExtensions() throws Descriptors.DescriptorValidationException {

        var extMessage = DescriptorProtos.DescriptorProto.newBuilder()
                .setName("ext")
                .addExtension(
                        DescriptorProtos.FieldDescriptorProto.newBuilder()
                                .setName("schema_name")
                                .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL)
                                .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING)
                                .setExtendee("google.protobuf.MessageOptions")
                                .setNumber(1001)
                                .build()
                )
                .addExtension(
                        DescriptorProtos.FieldDescriptorProto.newBuilder()
                                .setName("project_name")
                                .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL)
                                .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING)
                                .setExtendee("google.protobuf.MessageOptions")
                                .setNumber(1002)
                                .build()
                )
                .addExtension(
                        DescriptorProtos.FieldDescriptorProto.newBuilder()
                                .setName("enabled_canonical_fields")
                                .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL)
                                .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING)
                                .setExtendee("google.protobuf.MessageOptions")
                                .setNumber(1003)
                                .build()
                )
                .build();

        var extFile = DescriptorProtos.FileDescriptorProto.newBuilder()
                .addMessageType(extMessage)
                .addDependency("google/protobuf/descriptor.proto")
                .addExtension(
                        DescriptorProtos.FieldDescriptorProto.newBuilder()
                                .setName("api_key")
                                .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL)
                                .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING)
                                .setExtendee("google.protobuf.FileOptions")
                                .setNumber(1001)
                                .build()
                )
                .build();

        var fd = Descriptors.FileDescriptor.buildFrom(
                extFile,
                new Descriptors.FileDescriptor[]{
                        DescriptorProtos.getDescriptor(),
                }
        );


        schemaNameExt = GeneratedMessage.<DescriptorProtos.MessageOptions, String>newMessageScopedGeneratedExtension(
                DynamicMessage.newBuilder(fd.getMessageTypes().get(0)).getDefaultInstanceForType(),
                0,
                String.class,
                null
        );
        projectNameExt = GeneratedMessage.<DescriptorProtos.MessageOptions, String>newMessageScopedGeneratedExtension(
                DynamicMessage.newBuilder(fd.getMessageTypes().get(0)).getDefaultInstanceForType(),
                1,
                String.class,
                null
        );
        enabledCanonicalFieldsExt = GeneratedMessage.<DescriptorProtos.MessageOptions, String>newMessageScopedGeneratedExtension(
                DynamicMessage.newBuilder(fd.getMessageTypes().get(0)).getDefaultInstanceForType(),
                2,
                String.class,
                null
        );

        extensionRegistry =
                ExtensionRegistry.newInstance();
        extensionRegistry.add(schemaNameExt);
        extensionRegistry.add(projectNameExt);
        extensionRegistry.add(enabledCanonicalFieldsExt);
    }
}
