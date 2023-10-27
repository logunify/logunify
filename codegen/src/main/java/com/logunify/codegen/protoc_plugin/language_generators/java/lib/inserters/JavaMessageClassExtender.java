package com.logunify.codegen.protoc_plugin.language_generators.java.lib.inserters;

import com.logunify.codegen.protoc_plugin.language_generators.java.lib.JavaCodeInserter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public final class JavaMessageClassExtender extends JavaMessageLevelInserter {

    @Override
    protected String getInsertionPointTag() {
        return JavaCodeInserter.INSERTION_POINT_CLASS_SCOPE;
    }
}
