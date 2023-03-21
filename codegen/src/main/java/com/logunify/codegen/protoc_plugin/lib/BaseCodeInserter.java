package com.logunify.codegen.protoc_plugin.lib;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import lombok.experimental.SuperBuilder;

@SuperBuilder
abstract public class BaseCodeInserter {

    protected DescriptorProtos.FileDescriptorProto fileDescriptor;

    abstract protected String getInsertionPoint();

    abstract protected String getFileName();

    public PluginProtos.CodeGeneratorResponse.File generateFile(
            String content
    ) {
        return PluginProtos.CodeGeneratorResponse.File.newBuilder()
                .setName(getFileName())
                .setContent(content)
                .setInsertionPoint(getInsertionPoint())
                .build();
    }
}
