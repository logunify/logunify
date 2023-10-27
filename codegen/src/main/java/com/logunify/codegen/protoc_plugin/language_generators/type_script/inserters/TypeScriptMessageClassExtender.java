package com.logunify.codegen.protoc_plugin.language_generators.type_script.inserters;

import com.logunify.codegen.protoc_plugin.language_generators.type_script.TypeScriptCodeInserter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class TypeScriptMessageClassExtender extends TypeScriptCodeInserter {
    @Override
    protected String getInsertionPoint() {
        return String.format("class_scope:%s", messageDescriptor.getName());
    }
}
