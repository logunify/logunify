package com.logunify.output.sinks.bigquery;

import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardSQLTypeName;
import com.logunify.entity.SchemaDefinition;

public class SchemaConverter {
    public static Schema fromSchema(SchemaDefinition.Schema schemaDefinition) {
        var fields = schemaDefinition.getFields().stream().map(SchemaConverter::fromSchemaDefinitionField).toArray(Field[]::new);
        return Schema.of(fields);
    }

    private static StandardSQLTypeName toBqType(String type) {
        switch (type.toLowerCase()) {
            case "string":
                return StandardSQLTypeName.STRING;
            case "double":
            case "float":
                return StandardSQLTypeName.FLOAT64;
            case "int32":
            case "int64":
                return StandardSQLTypeName.INT64;
            case "boolean":
                return StandardSQLTypeName.BOOL;
            default:
                // enum types
                return StandardSQLTypeName.STRING;
        }
    }

    private static Field fromSchemaDefinitionField(SchemaDefinition.FieldDefinition fieldDefinition) {
        switch (fieldDefinition.getType().toLowerCase()) {
            case "map":
                return Field.newBuilder(
                                fieldDefinition.getName(),
                                StandardSQLTypeName.STRUCT,
                                fromSchemaDefinitionField(new SchemaDefinition.FieldDefinition(0, "key", "", fieldDefinition.getKeyType(), null, null, null,
                                        false, false)),
                                fromSchemaDefinitionField(new SchemaDefinition.FieldDefinition(0, "value", "", fieldDefinition.getValueType(), null, null,
                                        null, false,
                                        false)))
                        .setMode(Field.Mode.REPEATED)
                        .setDescription(fieldDefinition.getDescription())
                        .build();
            case "array":
                return Field.newBuilder(
                                fieldDefinition.getName(),
                                toBqType(fieldDefinition.getArrayType()))
                        .setMode(Field.Mode.REPEATED)
                        .setDescription(fieldDefinition.getDescription())
                        .build();
            default:
                // enum types
                return Field.newBuilder(fieldDefinition.getName(), toBqType(fieldDefinition.getType()))
                        .setDescription(fieldDefinition.getDescription())
                        .build();
        }
    }
}
