package com.logunify.codegen.protoc_plugin.language_generators.type_script.inserters;

import com.logunify.codegen.protoc_plugin.language_generators.type_script.TypeScriptCodeInserter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class TypeScriptMessageClassImplementer extends TypeScriptCodeInserter {
    @Override
    protected String getInsertionPoint() {
        return String.format("class_implements:%s", messageDescriptor.getName());
    }
}
