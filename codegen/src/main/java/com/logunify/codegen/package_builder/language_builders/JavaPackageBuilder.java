package com.logunify.codegen.package_builder.language_builders;

import com.google.common.base.CaseFormat;
import com.logunify.codegen.common.SupportingLanguage;
import lombok.experimental.SuperBuilder;

import java.util.Optional;

@SuperBuilder
public class JavaPackageBuilder extends LanguageSpecificPackageBuilder {

    @Override
    protected SupportingLanguage getLanguage() {
        return SupportingLanguage.JAVA;
    }

    @Override
    protected String getSourceFilePath(String schemaName) {
        if (Optional.ofNullable(orgName).map(name -> !name.isEmpty()).orElse(false)) {
            return String.format(
                    "java/main/com/%s/%s/%sSchema.java",
                    CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, orgName),
                    CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, getProjectName()),
                    schemaName
            );

        }
        return String.format(
                "java/main/com/%s/%sSchema.java",
                CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, getProjectName()),
                schemaName
        );
    }

    @Override
    protected boolean shouldIncludePackageInProto() {
        return true;
    }

    @Override
    protected String getAdditionalSourceOutPath() {
        return "/java/main";
    }
}
