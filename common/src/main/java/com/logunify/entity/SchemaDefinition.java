package com.logunify.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchemaDefinition implements Comparable<SchemaDefinition> {
    public static final Set<String> TYPES = Set.of("string", "double", "float", "int32", "int64", "boolean");

    private String projectName;
    private Schema schema;
    private List<OutputDefinition> outputs;

    public String getKey() {
        return String.format("%s/%s", getProjectName(), getSchema().getName());
    }

    @Override
    public int compareTo(SchemaDefinition schemaDefinition) {
        return CharSequence.compare(
                String.format("%s:%s", schemaDefinition.getProjectName(), schemaDefinition.getSchema().getName()),
                String.format("%s:%s", projectName, schema.getName())
        );
    }

    @Data
    @AllArgsConstructor
    public static class EnumValueDefinition {
        private String value;
        private int fieldNumber;
        private boolean deprecated;
    }

    @Data
    @NoArgsConstructor
    public static class EnumDefinition {
        private String name;
        private List<EnumValueDefinition> values;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FieldDefinition {
        private int fieldNumber;
        private String name;
        private String description;
        private String type;
        private String keyType;
        private String valueType;
        private String arrayType;
        private boolean deprecated;
        private boolean required;

        public String getName() {
            // Empty String
            StringBuilder result = new StringBuilder();

            // Append first character(in lower case)
            // to result string
            char c = name.charAt(0);
            result.append(Character.toLowerCase(c));

            // Traverse the string from
            // ist index to last index
            for (int i = 1; i < name.length(); i++) {

                char ch = name.charAt(i);

                // Check if the character is upper case
                // then append '_' and such character
                // (in lower case) to result string
                if (Character.isUpperCase(ch)) {
                    result.append('_');
                    result.append(Character.toLowerCase(ch));
                }

                // If the character is lower case then
                // add such character into result string
                else {
                    result.append(ch);
                }
            }

            return result.toString();
        }
    }

    @Data
    @NoArgsConstructor
    public static class OutputDefinition {
        private String name;
        private String type;
        private Map<String, String> customConfigs;
    }

    @Data
    @NoArgsConstructor
    public static class Schema {
        String name;
        String description;
        List<EnumDefinition> enums;
        List<FieldDefinition> fields;
    }
}
