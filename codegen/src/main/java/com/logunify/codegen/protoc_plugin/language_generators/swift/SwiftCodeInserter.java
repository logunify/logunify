package com.logunify.codegen.protoc_plugin.language_generators.swift;

import com.google.protobuf.DescriptorProtos;
import com.logunify.codegen.protoc_plugin.lib.BaseCodeInserter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
abstract public class SwiftCodeInserter extends BaseCodeInserter {
    private DescriptorProtos.FileDescriptorProto fileDescriptor;
    private DescriptorProtos.DescriptorProto messageDescriptor;

    @Override
    protected String getFileName() {
        return String.format("%s.pb.swift", messageDescriptor.getName());
    }
}
