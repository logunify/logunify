package com.logunify.codegen.protoc_plugin.language_generators.type_script.inserters;

import com.logunify.codegen.protoc_plugin.language_generators.type_script.TypeScriptCodeInserter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class TypeScriptAdditionalImportsInserter extends TypeScriptCodeInserter {
    @Override
    protected String getInsertionPoint() {
        return "additional_imports";
    }
}
