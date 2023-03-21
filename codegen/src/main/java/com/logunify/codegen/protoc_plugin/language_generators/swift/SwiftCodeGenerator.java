package com.logunify.codegen.protoc_plugin.language_generators.swift;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;
import com.logunify.codegen.common.SupportingLanguage;
import com.logunify.codegen.protoc_plugin.language_generators.swift.inserters.SwiftAdditionalImportsInserter;
import com.logunify.codegen.protoc_plugin.language_generators.swift.inserters.SwiftGlobalScopeExtender;
import com.logunify.codegen.protoc_plugin.lib.BaseCodeGenerator;
import lombok.Builder;

import java.util.Map;
import java.util.stream.Stream;

public final class SwiftCodeGenerator extends BaseCodeGenerator {
    @Builder
    public SwiftCodeGenerator(DescriptorProtos.FileDescriptorProto fileDescriptorProto) throws Descriptors.DescriptorValidationException {
        super(fileDescriptorProto);
    }

    @Override
    protected Stream<PluginProtos.CodeGeneratorResponse.File> generateCodeForMessage(
            DescriptorProtos.DescriptorProto messageDescriptor
    ) {
        var schemaName = messageDescriptor.getOptions().getExtension(extensions.getSchemaNameExt());
        var projectName = messageDescriptor.getOptions().getExtension(extensions.getProjectNameExt());

        return Stream.of(
                SwiftAdditionalImportsInserter.builder()
                        .messageDescriptor(messageDescriptor)
                        .fileDescriptor(fileDescriptorProto)
                        .build()
                        .generateFile("import LogUnify"),
                SwiftGlobalScopeExtender.builder()
                        .messageDescriptor(messageDescriptor)
                        .fileDescriptor(fileDescriptorProto)
                        .build()
                        .generateFile(publishTemplate("extension.mustache", Map.of("className", schemaName, "projectName", projectName)))
        );
    }

    @Override
    protected SupportingLanguage getLanguage() {
        return SupportingLanguage.SWIFT;
    }
}
