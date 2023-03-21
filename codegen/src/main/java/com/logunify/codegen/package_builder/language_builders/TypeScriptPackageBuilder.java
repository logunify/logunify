package com.logunify.codegen.package_builder.language_builders;

import com.logunify.codegen.common.SupportingLanguage;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class TypeScriptPackageBuilder extends LanguageSpecificPackageBuilder {

    @Override
    protected SupportingLanguage getLanguage() {
        return SupportingLanguage.TYPE_SCRIPT;
    }

    @Override
    protected String getSourceFilePath(String schemaName) {
        return String.format("%s.ts", schemaName);
    }

    @Override
    protected boolean shouldIncludePackageInProto() {
        return false;
    }

    @Override
    protected String getLanguagePluginName() {
        return "ts";
    }
}
