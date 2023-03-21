package com.logunify.codegen.protoc_plugin.lib;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;
import com.logunify.codegen.common.SupportingLanguage;
import com.logunify.protobuf_schema.ProtobufSchemaExtensions;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Stream;

abstract public class BaseCodeGenerator {
    protected final DescriptorProtos.FileDescriptorProto fileDescriptorProto;

    protected static final ProtobufSchemaExtensions extensions;


    static {
        try {
            extensions = new ProtobufSchemaExtensions();
        } catch (Descriptors.DescriptorValidationException e) {
            throw new RuntimeException(e);
        }
    }

    protected static final MustacheFactory templateFactory = new DefaultMustacheFactory();

    public BaseCodeGenerator(DescriptorProtos.FileDescriptorProto fileDescriptorProto) throws Descriptors.DescriptorValidationException {
        var fileDescriptor = Descriptors.FileDescriptor.buildFrom(
                fileDescriptorProto,
                new com.google.protobuf.Descriptors.FileDescriptor[]{
                        com.google.protobuf.DescriptorProtos.getDescriptor(),
                }
        );

        // Setup and prepare the extensions
        Descriptors.FileDescriptor.internalUpdateFileDescriptor(
                fileDescriptor,
                extensions.getExtensionRegistry()
        );
        this.fileDescriptorProto = fileDescriptor.toProto();
    }

    public Stream<PluginProtos.CodeGeneratorResponse.File> generateCode() {
        // Skip internal proto
        if (fileDescriptorProto.getName().equals("google/protobuf/descriptor.proto")) {
            return Stream.empty();
        }

        try {
            return fileDescriptorProto.getMessageTypeList()
                    .parallelStream()
                    .flatMap(this::generateCodeForMessage);
        } catch (Exception ignore) {
            return Stream.empty();
        }
    }

    protected String publishTemplate(String templateName, Map<String, Object> values) {
        var resource = this.getClass()
                .getResourceAsStream(String.format("/%s/%s", getLanguage().name().toLowerCase(), templateName));
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

    abstract protected Stream<PluginProtos.CodeGeneratorResponse.File> generateCodeForMessage(
            DescriptorProtos.DescriptorProto messageDescriptor
    );

    abstract protected SupportingLanguage getLanguage();
}
