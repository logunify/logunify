package com.logunify.codegen.protoc_plugin.language_generators.java.lib.inserters;

import com.google.protobuf.DescriptorProtos;
import com.logunify.codegen.protoc_plugin.language_generators.java.lib.JavaCodeInserter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
abstract public class JavaMessageLevelInserter extends JavaCodeInserter {
    protected DescriptorProtos.DescriptorProto messageDescriptor;

    abstract protected String getInsertionPointTag();

    @Override
    protected String getInsertionPoint() {
        return String.format("%s:%s", getInsertionPointTag(), getFullCanonicalClassName(messageDescriptor.getName()));
    }
}
