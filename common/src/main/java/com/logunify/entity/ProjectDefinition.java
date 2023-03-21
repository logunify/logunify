package com.logunify.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;


@Data
@Builder
@AllArgsConstructor
public class ProjectDefinition {

    private String projectName;
    private Set<SchemaDefinition> schemaDefinitions;

    public Optional<SchemaDefinition> getSchemaDefinitionByName(String name) {
        return schemaDefinitions.stream()
                .filter(schemaDefinition -> Objects.equals(schemaDefinition.getSchema().getName(), name))
                .findAny();
    }
}
