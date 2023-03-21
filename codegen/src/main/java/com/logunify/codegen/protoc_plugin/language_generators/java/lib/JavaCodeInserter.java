package com.logunify.codegen.protoc_plugin.language_generators.java.lib;

import com.google.protobuf.DescriptorProtos;
import com.logunify.codegen.protoc_plugin.lib.BaseCodeInserter;
import lombok.experimental.SuperBuilder;

import java.util.Optional;

@SuperBuilder
abstract public class JavaCodeInserter extends BaseCodeInserter {
    private DescriptorProtos.FileDescriptorProto fileDescriptor;

    protected static final String INSERTION_POINT_CLASS_SCOPE = "class_scope";
    protected static final String INSERTION_POINT_BUILDER_SCOPE = "builder_scope";
    protected static final String INSERTION_POINT_MESSAGE_IMPLEMENTS = "message_implements";
    protected static final String INSERTION_POINT_BUILDER_IMPLEMENTS = "builder_implements";
    protected static final String INSERTION_POINT_OUTER_CLASS_SCOPE = "outer_class_scope";

    protected String getOuterClassName() {
        return fileDescriptor.getOptions().getJavaOuterClassname();
    }

    protected String getPackageName() {
        return fileDescriptor.getPackage();
    }

    /**
     * Get the canonical class name (package + class name, e.g: com.foo.BarOuterClass) for a given class
     */
    protected String getFullCanonicalClassName(String className) {
        var hasPackageName = Optional.ofNullable(getPackageName()).map(str -> !str.isEmpty()).orElse(false);
        if (hasPackageName) {
            return String.format("%s.%s", getPackageName(), className);
        }
        return className;
    }

    @Override
    protected String getFileName() {
        return getPackageName().replace(".", "/") + "/" + getOuterClassName() + ".java";
    }
}
