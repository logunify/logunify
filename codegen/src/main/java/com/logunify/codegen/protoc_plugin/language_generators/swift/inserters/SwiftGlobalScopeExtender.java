package com.logunify.codegen.protoc_plugin.language_generators.swift.inserters;

import com.logunify.codegen.protoc_plugin.language_generators.swift.SwiftCodeInserter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class SwiftGlobalScopeExtender extends SwiftCodeInserter {
    @Override
    protected String getInsertionPoint() {
        return "global_scope";
    }
}
