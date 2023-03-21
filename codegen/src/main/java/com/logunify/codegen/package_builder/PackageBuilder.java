package com.logunify.codegen.package_builder;

import com.logunify.codegen.common.SupportingLanguage;
import com.logunify.codegen.package_builder.language_builders.JavaPackageBuilder;
import com.logunify.codegen.package_builder.language_builders.LanguageSpecificPackageBuilder;
import com.logunify.codegen.package_builder.language_builders.SwiftPackageBuilder;
import com.logunify.codegen.package_builder.language_builders.TypeScriptPackageBuilder;
import com.logunify.entity.ProjectDefinition;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class PackageBuilder {
    private String orgName;

    private PackageBuilderConfig config;
    private ProjectDefinition projectDefinition;

    public Map<SupportingLanguage, Set<File>> buildSourceFiles(Set<SupportingLanguage> generatingLanguages) {
        getRootBuildDir().mkdir();
        return generatingLanguages.stream().map(
                language -> {
                    var result = Map.entry(
                            language,
                            buildLanguageSpecificPackageBuilder(language)
                                    .buildSourceFiles());
                    log.info("Generated {} source files {} for project: {}", language, result.getValue().stream()
                                    .map(File::getName)
                                    .collect(Collectors.joining(",")),
                            projectDefinition.getProjectName());
                    return result;
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<SupportingLanguage, File> buildSourceFilesTarball(Set<SupportingLanguage> generatingLanguages) {
        getRootBuildDir().mkdir();
        return generatingLanguages.stream().map(
                language -> {
                    try {
                        var result = Map.entry(
                                language,
                                buildLanguageSpecificPackageBuilder(language)
                                        .buildSourceFilesTarball());

                        log.info("Generated {} source file tarball {} for project: {}", language, result.getValue().getName(),
                                projectDefinition.getProjectName());
                        return result;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<SupportingLanguage, File> buildSourceFilesForSchema(Set<SupportingLanguage> generatingLanguages, String schemaName) {
        getRootBuildDir().mkdir();
        return generatingLanguages.stream().map(
                language -> {
                    try {
                        var result = Map.entry(
                                language,
                                buildLanguageSpecificPackageBuilder(language)
                                        .buildSourceFileForSchema(schemaName));

                        log.info("Generated {} source file {} for schema: {}/{}", language, result.getValue().getName(), projectDefinition.getProjectName(),
                                schemaName);
                        return result;
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private String buildRootBuildDirPath() {
        return String.format(
                "/tmp/builds/%s",
                projectDefinition.getProjectName()
        );
    }

    private File getRootBuildDir() {
        return new File(buildRootBuildDirPath());
    }

    private LanguageSpecificPackageBuilder buildLanguageSpecificPackageBuilder(SupportingLanguage language) {
        switch (language) {
            case JAVA:
                return JavaPackageBuilder.builder()
                        .baseBuildDir(getRootBuildDir())
                        .logUnifyPluginPath(config.getLogunifyPluginPath())
                        .languagePluginPath(null)
                        .protocPath(config.getProtocPath())
                        .orgName(orgName)
                        .projectDefinition(projectDefinition)
                        .build();
            case SWIFT:
                return SwiftPackageBuilder.builder()
                        .baseBuildDir(getRootBuildDir())
                        .logUnifyPluginPath(config.getLogunifyPluginPath())
                        .languagePluginPath(config.getSwiftPluginPath())
                        .protocPath(config.getProtocPath())
                        .orgName(orgName)
                        .projectDefinition(projectDefinition)
                        .build();
            case TYPE_SCRIPT:
                return TypeScriptPackageBuilder.builder()
                        .baseBuildDir(getRootBuildDir())
                        .logUnifyPluginPath(config.getLogunifyPluginPath())
                        .languagePluginPath(config.getTypescriptPluginPath())
                        .protocPath(config.getProtocPath())
                        .orgName(orgName)
                        .projectDefinition(projectDefinition)
                        .build();
            default:
                return null;
        }
    }
}
