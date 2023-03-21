package com.logunify.schema_loader;

import com.logunify.entity.ProjectDefinition;
import com.logunify.entity.SchemaDefinition;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@NoArgsConstructor
public class SchemaRegistry {
    @Getter
    private final Map<String, SchemaDefinition> schemaDefinitions = new HashMap<>();

    @Getter
    private final Map<String, ProjectDefinition> projectDefinitions = new HashMap<>();

    public Optional<SchemaDefinition> getSchemaDefinition(String schemaKey) {
        return Optional.ofNullable(schemaDefinitions.getOrDefault(schemaKey, null));
    }

    public Optional<ProjectDefinition> getProjectDefinition(String projectName) {
        return Optional.ofNullable(projectDefinitions.getOrDefault(projectName, null));
    }

    public void registerSchema(SchemaDefinition schemaToRegister) {
        if (schemaDefinitions.containsKey(schemaToRegister.getKey())) {
            log.error("Schema already exists for {}", schemaToRegister.getKey());
            throw new RuntimeException("Schema already exists for " + schemaToRegister.getKey());
        }

        log.info("Schema added for {}", schemaToRegister.getKey());
        schemaDefinitions.put(schemaToRegister.getKey(), schemaToRegister);

        if (projectDefinitions.containsKey(schemaToRegister.getProjectName())) {
            projectDefinitions.get(schemaToRegister.getProjectName()).getSchemaDefinitions().add(schemaToRegister);
        } else {
            projectDefinitions.put(schemaToRegister.getProjectName(), new ProjectDefinition(
                    schemaToRegister.getProjectName(),
                    new HashSet<>(Set.of(schemaToRegister))
            ));
        }
    }
}
