package com.logunify.codegen.package_builder.language_builders;

import com.google.protobuf.Descriptors;
import com.logunify.codegen.common.SupportingLanguage;
import com.logunify.entity.ProjectDefinition;
import com.logunify.entity.SchemaDefinition;
import com.logunify.protobuf_schema.ProtobufSchemaBuilder;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

@SuperBuilder
@Slf4j
abstract public class LanguageSpecificPackageBuilder {

    private static final ProtobufSchemaBuilder schemaGenerator;

    static {
        try {
            schemaGenerator = new ProtobufSchemaBuilder();
        } catch (Descriptors.DescriptorValidationException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    protected ProjectDefinition projectDefinition;
    @NonNull
    protected String protocPath;
    @NonNull
    protected File baseBuildDir;
    @NonNull
    protected String logUnifyPluginPath;
    protected String orgName;
    protected String languagePluginPath;

    protected String version = "0.0.1";

    public Set<File> buildSourceFiles() {
        return projectDefinition.getSchemaDefinitions().stream().map(
                schemaDefinition -> {
                    try {
                        return buildSourceFileForSchema(schemaDefinition.getSchema().getName());
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
        ).collect(Collectors.toSet());
    }

    public File buildSourceFilesTarball() throws IOException {
        var sourceFiles = buildSourceFiles();

        File tarFile = new File(baseBuildDir + "/sources.tar.gz");
        var tos = new TarArchiveOutputStream(
                new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(tarFile)))
        );
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        sourceFiles.forEach(sourceFile -> {
            var tarEntry = new TarArchiveEntry(sourceFile);
            tarEntry.setName(sourceFile.getName());
            try {
                tos.putArchiveEntry(tarEntry);
                FileUtils.copyFile(sourceFile, tos);
                tos.closeArchiveEntry();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        tos.close();
        return tarFile;
    }

    public File buildSourceFileForSchema(String schemaName) throws IOException, IllegalArgumentException, InterruptedException {
        new File(getBuildPath()).mkdirs();

        var maybeSchemaDefinition = projectDefinition.getSchemaDefinitionByName(schemaName);
        if (maybeSchemaDefinition.isEmpty()) {
            throw new IllegalArgumentException(String.format("Given schema %s does not exist under project %s", schemaName,
                    getProjectName()));
        }
        // Create the proto file
        var schemaDefinition = maybeSchemaDefinition.get();
        var schema = schemaGenerator.buildSchema(orgName, schemaDefinition, shouldIncludePackageInProto());
        var pbOutputFile = new FileOutputStream(String.format("%s/%s.pb", getBuildPath(), schemaName));
        schema.getFileDescriptorSet().writeTo(pbOutputFile);

        // Run protoc
        runProtoc(new File(String.format("%s/src", getBuildPath())), schemaDefinition);
        return new File(String.format("%s/src/%s", getBuildPath(), getSourceFilePath(schemaName)));
    }

    protected String runCommand(File dir, String... command) throws IOException, InterruptedException {
        var processBuilder = new ProcessBuilder(command);
        var process = processBuilder.directory(dir)
                .redirectErrorStream(true)
                .start();

        if (process.waitFor() > 0) {
            var result = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            throw new RuntimeException(String.format("Failed to run command: %s, result: %s",
                    String.join(" ", command),
                    result
            ));
        }


        return new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
    }

    protected String getProjectName() {
        return projectDefinition.getProjectName();
    }

    protected abstract SupportingLanguage getLanguage();

    protected String getLanguagePluginName() {
        return getLanguage().name().toLowerCase();
    }

    protected abstract String getSourceFilePath(String schemaName);

    protected String getAdditionalSourceOutPath() {
        return "";
    }

    protected abstract boolean shouldIncludePackageInProto();

    private String getBuildPath() {
        return String.format("%s/%s", baseBuildDir, getLanguage().name().toLowerCase());
    }

    private void runProtoc(File srcDir, SchemaDefinition schemaDefinition) throws IOException, InterruptedException {
        var sourceOutputPath = String.format("%s%s", srcDir, getAdditionalSourceOutPath());
        new File(sourceOutputPath).mkdirs();

        var schemaName = schemaDefinition.getSchema().getName();
        var commandArgs = new ArrayList<String>(List.of(
                protocPath,
                String.format("--descriptor_set_in=%s.pb", schemaName),
                String.format("--%s_out=%s", getLanguagePluginName(), sourceOutputPath),
                "--proto_path=" + getBuildPath()
        ));
        // If the language has a specific plugin, also call the plugin
        Optional.ofNullable(languagePluginPath)
                .filter(path -> !path.isEmpty())
                .ifPresent(path -> commandArgs.add(
                        String.format(
                                "--plugin=protoc-gen-%s=%s",
                                getLanguagePluginName(),
                                languagePluginPath
                        )
                ));
        commandArgs.addAll(List.of(
                String.format(
                        "--plugin=protoc-gen-logunify=%s",
                        logUnifyPluginPath
                ),
                String.format("--logunify_out=%s", sourceOutputPath),
                String.format("--logunify_opt=generating_language=%s", getLanguage()),
                schemaName
        ));

        log.debug("Running protoc command: {}", String.join(" ", commandArgs));
        try {
            var result = runCommand(new File(getBuildPath()), commandArgs.toArray(String[]::new));
            log.debug("Protoc result: {}", result);
        } catch (Exception e) {
            log.error("Failed to run the protoc for schema: {}, error: {}",
                    schemaDefinition.getKey(),
                    e.getMessage()
            );
            throw e;
        }
    }
}
