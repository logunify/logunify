package com.logunify.codegen.protoc_plugin.language_generators.java;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;
import com.logunify.codegen.common.SupportingLanguage;
import com.logunify.codegen.protoc_plugin.language_generators.java.lib.inserters.JavaMessageBuilderClassExtender;
import com.logunify.codegen.protoc_plugin.language_generators.java.lib.inserters.JavaMessageClassExtender;
import com.logunify.codegen.protoc_plugin.language_generators.java.lib.inserters.JavaMessageClassImplementer;
import com.logunify.codegen.protoc_plugin.lib.BaseCodeGenerator;
import com.logunify.protobuf_schema.CanonicalField;
import lombok.Builder;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class JavaCodeGenerator extends BaseCodeGenerator {

    @Builder
    public JavaCodeGenerator(DescriptorProtos.FileDescriptorProto fileDescriptorProto) throws Descriptors.DescriptorValidationException {
        super(fileDescriptorProto);
    }

    @Override
    protected Stream<PluginProtos.CodeGeneratorResponse.File> generateCodeForMessage(
            DescriptorProtos.DescriptorProto messageDescriptor
    ) {
        var schemaName = messageDescriptor.getOptions().getExtension(extensions.getSchemaNameExt());
        var projectName = messageDescriptor.getOptions().getExtension(extensions.getProjectNameExt());
        var enabledCanonicalMap = getEnabledCanonicalMap(messageDescriptor);
        var canonicalFieldSettingCode = publishTemplate("canonical-field-setting-code.mustache", enabledCanonicalMap);

        return Stream.of(
                JavaMessageClassExtender.builder()
                        .messageDescriptor(messageDescriptor)
                        .fileDescriptor(fileDescriptorProto)
                        .build()
                        .generateFile(publishTemplate(
                                "message-class-code.mustache",
                                Map.of("schemaName", schemaName, "projectName", projectName)
                        )),
                JavaMessageClassImplementer.builder().messageDescriptor(messageDescriptor)
                        .fileDescriptor(fileDescriptorProto)
                        .build()
                        .generateFile("com.logunify.logging.event.Event,"),
                JavaMessageBuilderClassExtender.builder()
                        .messageDescriptor(messageDescriptor)
                        .fileDescriptor(fileDescriptorProto)
                        .build()
                        .generateFile(publishTemplate(
                                "builder-class-code.mustache",
                                Map.of("canonicalFieldSettingCode", canonicalFieldSettingCode))
                        )
        );
    }

    @Override
    protected SupportingLanguage getLanguage() {
        return SupportingLanguage.JAVA;
    }

    private Map<String, Object> getEnabledCanonicalMap(DescriptorProtos.DescriptorProto messageDescriptor) {
        var enabledCanonicalFieldsStr = messageDescriptor.getOptions().getExtension(extensions.getEnabledCanonicalFieldsExt());
        if (!enabledCanonicalFieldsStr.isEmpty()) {
            var enabledCanonicalFields = Arrays.stream(enabledCanonicalFieldsStr.split(":"))
                    .collect(Collectors.toSet());

            return Arrays.stream(CanonicalField.values())
                    .map(val -> {
                        if (enabledCanonicalFields.contains(val.name())) {
                            return Map.entry(val.name(), true);
                        }
                        return Map.entry(val.name(), false);
                    }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return Map.of();
    }

}
