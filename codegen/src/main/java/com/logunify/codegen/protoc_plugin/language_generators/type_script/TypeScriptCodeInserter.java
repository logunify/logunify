package com.logunify.codegen.protoc_plugin.language_generators.type_script;

import com.google.protobuf.DescriptorProtos;
import com.logunify.codegen.protoc_plugin.lib.BaseCodeInserter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
abstract public class TypeScriptCodeInserter extends BaseCodeInserter {
    protected DescriptorProtos.FileDescriptorProto fileDescriptor;
    protected DescriptorProtos.DescriptorProto messageDescriptor;

    @Override
    protected String getFileName() {
        return String.format("%s.ts", messageDescriptor.getName());
    }
}
