package com.logunify.codegen.package_builder.language_builders;

import com.logunify.codegen.common.SupportingLanguage;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class SwiftPackageBuilder extends LanguageSpecificPackageBuilder {

    @Override
    protected SupportingLanguage getLanguage() {
        return SupportingLanguage.SWIFT;
    }

    @Override
    protected String getSourceFilePath(String schemaName) {
        return String.format("%s.pb.swift", schemaName);
    }

    @Override
    protected boolean shouldIncludePackageInProto() {
        return false;
    }
}
