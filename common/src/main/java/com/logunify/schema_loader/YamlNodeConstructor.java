package com.logunify.schema_loader;

import com.logunify.entity.SchemaDefinition;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;

import java.util.ArrayList;
import java.util.HashMap;

import static com.logunify.entity.SchemaDefinition.TYPES;

public class YamlNodeConstructor extends Constructor {
    public YamlNodeConstructor(Class<SchemaDefinition> schemaConfigClass) {
        super(schemaConfigClass);
    }

    private int fieldNumber = 1;

    @Override
    public Object getSingleData(Class<?> type) {
        resetFieldNumber();
        return super.getSingleData(type);
    }

    @Override
    protected Object constructObject(Node node) {
        // Custom logic for Output config
        if (node.getType() == SchemaDefinition.OutputDefinition.class) {
            return constructOutput((MappingNode) node);
        } else if (node.getType() == SchemaDefinition.EnumDefinition.class) {
            return constructEnum((MappingNode) node);
        } else if (node.getType() == SchemaDefinition.FieldDefinition.class) {
            return constructField((MappingNode) node);
        }
        return super.constructObject(node);
    }

    private SchemaDefinition.OutputDefinition constructOutput(MappingNode node) {
        var customConfigs = new HashMap<String, String>();
        var outputConfig = new SchemaDefinition.OutputDefinition();
        node.getValue().forEach(
                nodeTuple -> {
                    var keyNode = (ScalarNode) nodeTuple.getKeyNode();
                    var valueNode = (ScalarNode) nodeTuple.getValueNode();

                    switch (keyNode.getValue()) {
                        case "name":
                            outputConfig.setName(valueNode.getValue());
                            break;
                        case "type":
                            outputConfig.setType(valueNode.getValue());
                            break;
                        default:
                            customConfigs.put(keyNode.getValue(), valueNode.getValue());
                            break;
                    }
                }
        );
        outputConfig.setCustomConfigs(customConfigs);
        return outputConfig;
    }

    private SchemaDefinition.EnumDefinition constructEnum(MappingNode node) {
        var enumDefinition = new SchemaDefinition.EnumDefinition();
        node.getValue().forEach(
                nodeTuple -> {
                    var keyNode = (ScalarNode) nodeTuple.getKeyNode();

                    switch (keyNode.getValue()) {
                        case "name":
                            enumDefinition.setName(((ScalarNode) nodeTuple.getValueNode()).getValue());
                            break;
                        case "values":
                            int enumFiledNumber = 1;
                            var values = (SequenceNode) nodeTuple.getValueNode();

                            var enumValues = new ArrayList<SchemaDefinition.EnumValueDefinition>();
                            for (Node enumValueNode : values.getValue()) {
                                var enumValue = ((ScalarNode) enumValueNode).getValue();
                                enumValues.add(new SchemaDefinition.EnumValueDefinition(enumValue, enumFiledNumber++, false));
                            }
                            enumDefinition.setValues(enumValues);
                            break;
                        default:
                            break;
                    }
                });
        return enumDefinition;
    }

    private SchemaDefinition.FieldDefinition constructField(MappingNode node) {
        var fieldDefinition = new SchemaDefinition.FieldDefinition();
        fieldDefinition.setFieldNumber(fieldNumber++);
        node.getValue().forEach(
                nodeTuple -> {
                    var keyNode = (ScalarNode) nodeTuple.getKeyNode();
                    var valueNode = (ScalarNode) nodeTuple.getValueNode();

                    switch (keyNode.getValue()) {
                        case "name":
                            fieldDefinition.setName(valueNode.getValue());
                            break;
                        case "description":
                            fieldDefinition.setDescription(valueNode.getValue());
                            break;
                        case "deprecated":
                            fieldDefinition.setDeprecated(Boolean.parseBoolean(valueNode.getValue()));
                            break;
                        case "required":
                            fieldDefinition.setRequired(Boolean.parseBoolean(valueNode.getValue()));
                            break;
                        case "type":
                            var type = valueNode.getValue();
                            if (TYPES.contains(type)) {
                                fieldDefinition.setType(type);
                            } else if (type.matches("array<(.*)>")) {
                                fieldDefinition.setType("array");
                                fieldDefinition.setArrayType(type.replace("array<", "").replace(">", "").strip());
                            } else if (type.matches("map<(.*)>")) {
                                fieldDefinition.setType("map");

                                var mapTypes = type.replace("map<", "").replace(">", "").split(",");
                                fieldDefinition.setKeyType(mapTypes[0].strip());
                                fieldDefinition.setValueType(mapTypes[1].strip());
                            } else if (type.matches("enum<(.*)>")) {
                                fieldDefinition.setType(type.replace("enum<", "").replace(">", "").strip());
                            } else {
                                throw new RuntimeException("Unknown type: " + type);
                            }
                        default:
                            break;
                    }
                }
        );
        return fieldDefinition;
    }

    private void resetFieldNumber() {
        fieldNumber = 1;
    }
}
