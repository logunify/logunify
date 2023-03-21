package com.logunify.lib.package_builder;

import com.logunify.codegen.common.SupportingLanguage;
import com.logunify.codegen.package_builder.PackageBuilder;
import com.logunify.codegen.package_builder.PackageBuilderConfig;
import com.logunify.configuration.ConfigProvider;
import com.logunify.entity.ProjectDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Map;
import java.util.Set;

@Service
public class PackageBuildingService {
    private final ConfigProvider.PackageBuilderConfig packageBuilderConfig;

    private final ConfigProvider.SchemaConfig schemaConfig;

    @Autowired
    public PackageBuildingService(ConfigProvider.PackageBuilderConfig config, ConfigProvider.SchemaConfig schemaConfig) {
        this.packageBuilderConfig = config;
        this.schemaConfig = schemaConfig;
    }

    public Map<SupportingLanguage, Set<File>> buildSourceFiles(
            ProjectDefinition projectDefinition,
            Set<SupportingLanguage> languages
    ) {
        var builder = buildPackageBuilder(projectDefinition);
        return builder.buildSourceFiles(languages);
    }

    public Map<SupportingLanguage, File> buildSourceFilesTarball(
            ProjectDefinition projectDefinition,
            Set<SupportingLanguage> languages
    ) {
        var builder = buildPackageBuilder(projectDefinition);
        return builder.buildSourceFilesTarball(languages);
    }

    public Map<SupportingLanguage, File> buildSourceFileForSchema(
            ProjectDefinition projectDefinition,
            Set<SupportingLanguage> languages,
            String schemaName
    ) {
        var builder = buildPackageBuilder(projectDefinition);
        return builder.buildSourceFilesForSchema(languages, schemaName);
    }

    private PackageBuilder buildPackageBuilder(ProjectDefinition projectDefinition) {
        return new PackageBuilder(
                schemaConfig.getOrgName(),
                new PackageBuilderConfig(
                        packageBuilderConfig.getProtocPluginPath(),
                        packageBuilderConfig.getProtocPath(),
                        packageBuilderConfig.getSwiftPluginPath(),
                        packageBuilderConfig.getTypescriptPluginPath()
                ),
                projectDefinition
        );
    }
}
