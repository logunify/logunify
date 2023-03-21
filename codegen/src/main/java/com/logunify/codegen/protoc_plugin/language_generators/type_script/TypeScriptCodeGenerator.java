package com.logunify.codegen.protoc_plugin.language_generators.type_script;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;
import com.logunify.codegen.common.SupportingLanguage;
import com.logunify.codegen.protoc_plugin.language_generators.type_script.inserters.TypeScriptAdditionalImportsInserter;
import com.logunify.codegen.protoc_plugin.language_generators.type_script.inserters.TypeScriptMessageClassExtender;
import com.logunify.codegen.protoc_plugin.language_generators.type_script.inserters.TypeScriptMessageClassImplementer;
import com.logunify.codegen.protoc_plugin.lib.BaseCodeGenerator;
import lombok.Builder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Stream;

public final class TypeScriptCodeGenerator extends BaseCodeGenerator {

    private static final MustacheFactory templateFactory = new DefaultMustacheFactory();

    @Builder
    public TypeScriptCodeGenerator(DescriptorProtos.FileDescriptorProto fileDescriptorProto) throws Descriptors.DescriptorValidationException {
        super(fileDescriptorProto);
    }

    @Override
    protected Stream<PluginProtos.CodeGeneratorResponse.File> generateCodeForMessage(
            DescriptorProtos.DescriptorProto messageDescriptor
    ) {
        var schemaName = messageDescriptor.getOptions().getExtension(extensions.getSchemaNameExt());
        var projectName = messageDescriptor.getOptions().getExtension(extensions.getProjectNameExt());

        return Stream.of(
                TypeScriptAdditionalImportsInserter.builder()
                        .messageDescriptor(messageDescriptor)
                        .fileDescriptor(fileDescriptorProto)
                        .build()
                        .generateFile("import LogUnifyLogger, { LogUnifyEvent } from \"@logunify/node-sdk\";\n"),
                TypeScriptMessageClassImplementer.builder()
                        .messageDescriptor(messageDescriptor)
                        .fileDescriptor(fileDescriptorProto)
                        .build()
                        .generateFile("LogUnifyEvent"),
                TypeScriptMessageClassExtender.builder()
                        .messageDescriptor(messageDescriptor)
                        .fileDescriptor(fileDescriptorProto)
                        .build()
                        .generateFile(publishTemplate("message-class-code.mustache", Map.of(
                                "schemaName", schemaName, "projectName", projectName
                        )))
        );
    }

    @Override
    protected SupportingLanguage getLanguage() {
        return SupportingLanguage.TYPE_SCRIPT;
    }

    protected String publishTemplate(String templateName, Map<String, Object> values) {
        var resource = this.getClass().getResourceAsStream("/type_script/" + templateName);
        var template = templateFactory.compile(
                new InputStreamReader(resource, StandardCharsets.UTF_8),
                templateName
        );

        var writer = new StringWriter();
        try {
            template.execute(writer, values).flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return writer.toString();
    }
}
