package com.logunify.codegen;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.logunify.codegen.common.SupportingLanguage;
import com.logunify.codegen.package_builder.PackageBuilder;
import com.logunify.codegen.package_builder.PackageBuilderConfig;
import com.logunify.schema_loader.SchemaLoader;
import com.logunify.schema_loader.SchemaRegistry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class PackageBuilderMain {
    static SchemaRegistry schemaRegistry = new SchemaRegistry();
    static SchemaLoader schemaLoader = new SchemaLoader(schemaRegistry);
    static CommandLineParser parser = new DefaultParser();

    static Options options = new Options();

    static {
        options.addOption(
                Option.builder("s")
                        .longOpt("schema_path")
                        .required(true)
                        .hasArg(true)
                        .desc("Path to schemas.")
                        .build()
        ).addOption(
                Option.builder()
                        .longOpt("languages")
                        .required(true)
                        .hasArg(true)
                        .desc("List of comma separated targeting language: java, swift, type_script")
                        .build()
        ).addOption(
                Option.builder("d")
                        .longOpt("destination_path")
                        .hasArg(true)
                        .desc("Path to generated files")
                        .build()
        ).addOption(
                Option.builder()
                        .longOpt("protoc_path")
                        .hasArg(true)
                        .desc("Override path of the protoc")
                        .build()
        ).addOption(
                Option.builder()
                        .longOpt("logunify_plugin_path")
                        .hasArg(true)
                        .desc("Override path of the logunify protoc plugin")
                        .build()
        ).addOption(
                Option.builder()
                        .longOpt("swift_plugin_path")
                        .hasArg(true)
                        .desc("Override path of the swift protoc plugin")
                        .build()
        ).addOption(
                Option.builder()
                        .longOpt("type_script_plugin_path")
                        .hasArg(true)
                        .desc("Override path of the type script protoc plugin")
                        .build()
        ).addOption(
                Option.builder("o")
                        .longOpt("org_name")
                        .hasArg(true)
                        .desc("Name of the organization")
                        .build()
        );
    }

    @Data
    @AllArgsConstructor
    private static class CommandLineArguments {
        private String schemaPath;
        private Set<SupportingLanguage> generatingLanguages;
        private String destinationPath;
        private String protocPath;
        private String logunifyPluginPath;
        private String swiftPluginPath;
        private String typeScriptPluginPath;
        private String orgName;
    }

    private static CommandLineArguments parseCommandLineArguments(String[] args) throws ParseException, IOException {
        var cmd = PackageBuilderMain.parser.parse(PackageBuilderMain.options, args);

        var schemaPath = cmd.getOptionValue("s");
        schemaLoader.loadSchema(schemaPath);
        if (schemaRegistry.getSchemaDefinitions().size() == 0) {
            throw new IllegalArgumentException(String.format("Cannot find any valid schema from provided schema-path: %s",
                    new File(schemaPath).getAbsolutePath()));
        }

        var generatingLanguages = Arrays.stream(cmd.getOptionValue("languages").split(","))
                .flatMap(language -> {
                    try {
                        return Optional.of(
                                SupportingLanguage.valueOf(language.toUpperCase())
                        ).stream();
                    } catch (IllegalArgumentException e) {
                        return Optional.<SupportingLanguage>empty().stream();
                    }
                })
                .collect(Collectors.toSet());
        if (generatingLanguages.isEmpty()) {
            throw new IllegalArgumentException("None of the language provided is supported");
        }

        var destinationDir = new File(cmd.getOptionValue("d", System.getProperty("user.dir") + "/generated"));
        destinationDir.mkdirs();
        if (!FileUtils.isDirectory(destinationDir)) {
            throw new IllegalArgumentException(String.format("Destination dir %s is not a directory", destinationDir.getAbsolutePath()));
        }

        return new CommandLineArguments(
                schemaPath,
                generatingLanguages,
                destinationDir.getAbsolutePath(),
                cmd.getOptionValue("protoc_path", "protoc"),
                cmd.getOptionValue("logunify_plugin_path", System.getProperty("user.dir") + "/codegen/build/install/codegen/bin/protoc" +
                        "-plugin"),
                cmd.getOptionValue("swift_plugin_path"),
                cmd.getOptionValue("type_script_plugin_path"),
                cmd.getOptionValue("org_name")
        );
    }

    public static void main(String[] args) throws ParseException, IOException {
        ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.INFO);
        if (args.length == 0) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("codegen", options);

            return;
        }

        var commandLineArgs = parseCommandLineArguments(args);

        // access configuration properties
        var config = new PackageBuilderConfig(
                commandLineArgs.logunifyPluginPath,
                commandLineArgs.protocPath,
                commandLineArgs.swiftPluginPath,
                commandLineArgs.typeScriptPluginPath
        );

        try {
            schemaRegistry.getProjectDefinitions().values().forEach(
                    projectDefinition -> {
                        var projectPath = String.format("/%s/%s", commandLineArgs.getDestinationPath(), projectDefinition.getProjectName());

                        var packageBuilder = new PackageBuilder(commandLineArgs.orgName, config, projectDefinition);
                        packageBuilder.buildSourceFiles(commandLineArgs.getGeneratingLanguages())
                                .forEach((language, files) -> {
                                    var languagePath = String.format("%s/%s", projectPath, language.toString().toLowerCase());

                                    files.forEach(file -> {
                                        var targetFile = new File(String.format("%s/%s", languagePath, file.getName()));
                                        try {
                                            FileUtils.copyFile(file, targetFile);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                                });
                    });
        } catch (Exception e) {
            log.error("Failed to run the codegen, error: {}", e.getMessage());
        }
    }
}
