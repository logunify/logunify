package com.logunify.schema_loader;

import com.logunify.entity.SchemaDefinition;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.logunify.entity.SchemaDefinition.TYPES;

@Slf4j
public class SchemaLoader {
    private final Yaml yaml = new Yaml(new YamlNodeConstructor(SchemaDefinition.class));

    private final SchemaRegistry schemaRegistry;

    public SchemaLoader(SchemaRegistry schemas) {
        this.schemaRegistry = schemas;
    }

    public void loadSchema(String pathStr) throws IOException {
        log.info("Loading schemas from {}", pathStr);
        var loadedSchemaKeys = loadSchemaFromPath(pathStr);
        log.info("Schemas loaded: {}", String.join(", ", loadedSchemaKeys));
    }

    private Set<String> listDir(String configsPath) throws IOException {
        try (var stream = Files.list(Paths.get(configsPath))) {
            return stream.filter(file -> !Files.isDirectory(file))
                    .map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .collect(Collectors.toSet());
        }
    }

    private List<String> loadSchemaFromPath(String pathStr) throws IOException {
        Path path = Paths.get(pathStr);
        if (Files.isDirectory(path)) {
            List<String> loadedSchemaKeys = new ArrayList<>();
            listDir(pathStr).forEach(subPathStr -> {
                try {
                    loadedSchemaKeys.addAll(loadSchemaFromPath(subPathStr));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            return loadedSchemaKeys;
        }

        log.info("Loading schema: {}", path);

        var content = new String(Files.readAllBytes(path));
        SchemaDefinition schemaDefinition = this.yaml.load(content);

        try {
            validateSchema(schemaDefinition);
        } catch (IllegalArgumentException e) {
            log.error("Skipping invalid schema {}, reason: {}", schemaDefinition.getKey(), e.getMessage());
            return List.of();
        }

        log.info("Loaded schema: {}", schemaDefinition.getKey());
        log.debug("Content of {}: {}", schemaDefinition.getKey(), schemaDefinition);
        this.schemaRegistry.registerSchema(schemaDefinition);

        return List.of(schemaDefinition.getKey());
    }

    public void validateSchema(SchemaDefinition schemaDefinition) {
        var schema = schemaDefinition.getSchema();

        // verify that all fields have unique names
        var uniqueFieldNames = new HashSet<>();
        var duplicatedFiledNames = schema.getFields().stream().map(SchemaDefinition.FieldDefinition::getName)
                .filter(name -> !uniqueFieldNames.add(name))
                .collect(Collectors.toList());
        if (!duplicatedFiledNames.isEmpty()) {
            throw new IllegalArgumentException("Duplicate field name: " + String.join(",", duplicatedFiledNames));
        }

        Function<String, Boolean> isValidPrimitiveType = TYPES::contains;
        Function<String, Boolean> isValidEnum =
                (String type) -> schema.getEnums().stream().anyMatch(enumDefinition -> enumDefinition.getName().equals(type));

        // verify that all fields have valid types
        schema.getFields().forEach(fieldDefinition -> {
            if (fieldDefinition.getType().equals("array")) {
                if (!isValidPrimitiveType.apply(fieldDefinition.getArrayType()) && !isValidEnum.apply(fieldDefinition.getArrayType())) {
                    throw new IllegalArgumentException("Invalid array type: " + fieldDefinition.getArrayType());
                }
            } else if (fieldDefinition.getType().equals("map")) {
                if (!isValidPrimitiveType.apply(fieldDefinition.getKeyType()) && !isValidEnum.apply(fieldDefinition.getKeyType())) {
                    throw new IllegalArgumentException("Invalid map key type: " + fieldDefinition.getKeyType());
                }
                if (!isValidPrimitiveType.apply(fieldDefinition.getValueType()) && !isValidEnum.apply(fieldDefinition.getValueType())) {
                    throw new IllegalArgumentException("Invalid map value type: " + fieldDefinition.getValueType());
                }
            } else if (!isValidPrimitiveType.apply(fieldDefinition.getType()) && !isValidEnum.apply(fieldDefinition.getType())) {
                throw new IllegalArgumentException("Invalid type: " + fieldDefinition.getType());
            }
        });
    }
}
