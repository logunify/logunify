package com.logunify.codegen.protoc_plugin.language_generators.java.lib.inserters;

import com.logunify.codegen.protoc_plugin.language_generators.java.lib.JavaCodeInserter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public final class JavaOuterClassExtender extends JavaCodeInserter {

    @Override
    protected String getInsertionPoint() {
        return INSERTION_POINT_OUTER_CLASS_SCOPE;
    }
}
