package com.logunify.codegen;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;
import com.logunify.codegen.common.SupportingLanguage;
import com.logunify.codegen.protoc_plugin.language_generators.java.JavaCodeGenerator;
import com.logunify.codegen.protoc_plugin.language_generators.swift.SwiftCodeGenerator;
import com.logunify.codegen.protoc_plugin.language_generators.type_script.TypeScriptCodeGenerator;
import com.logunify.codegen.protoc_plugin.lib.BaseCodeGenerator;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProtocPluginMain {
    private static final String GENERATING_LANGUAGES = "generating_language";

    public static void main(String[] args) {
        try {
            var request = PluginProtos.CodeGeneratorRequest.parseFrom(System.in);
            var generatingLanguage = getGeneratingLanguageIfSupported(request);
            if (generatingLanguage.isEmpty()) {
                // Do nothing is the generating language is empty or the generating language is not supported
                return;
            }

            var generatedFiles = request.getProtoFileList().stream().flatMap(fileDescriptor -> {
                try {
                    return buildCodeGenerator(fileDescriptor, generatingLanguage.get()).generateCode();
                } catch (Descriptors.DescriptorValidationException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());

            var response = PluginProtos.CodeGeneratorResponse.newBuilder();
            response.addAllFile(generatedFiles);
            response.build().writeTo(System.out);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, String> getOptions(PluginProtos.CodeGeneratorRequest request) {
        if (!request.hasParameter()) {
            return Map.of();
        }
        return Arrays.stream(request.getParameter().split(","))
                .filter(optStr -> optStr.contains("="))
                .collect(Collectors.toMap(
                        param -> param.split("=")[0],
                        param -> param.split("=")[1]
                ));
    }

    private static Optional<SupportingLanguage> getGeneratingLanguageIfSupported(PluginProtos.CodeGeneratorRequest request) {
        var options = getOptions(request);
        if (!options.containsKey(GENERATING_LANGUAGES)) {
            return Optional.empty();
        }

        try {
            return Optional.of(
                    SupportingLanguage.valueOf(options.get(GENERATING_LANGUAGES).toUpperCase())
            );
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static BaseCodeGenerator buildCodeGenerator(DescriptorProtos.FileDescriptorProto fileDescriptor, SupportingLanguage generatingLanguage)
            throws Descriptors.DescriptorValidationException {
        switch (generatingLanguage) {
            case JAVA:
                return JavaCodeGenerator.builder().fileDescriptorProto(fileDescriptor).build();
            case SWIFT:
                return SwiftCodeGenerator.builder().fileDescriptorProto(fileDescriptor).build();
            case TYPE_SCRIPT:
                return TypeScriptCodeGenerator.builder().fileDescriptorProto(fileDescriptor).build();
            default:
                throw new IllegalArgumentException();
        }
    }
}
