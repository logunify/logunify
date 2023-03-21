package com.logunify.protobuf_schema.definitions;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.OneofDescriptorProto;

import java.util.HashMap;
import java.util.Map;

public class MessageDefinition {
    public static Builder newBuilder(String msgTypeName) {
        return new Builder(msgTypeName);
    }

    public String toString() {
        return mMsgType.toString();
    }

    public DescriptorProto getMessageType() {
        return mMsgType;
    }

    private MessageDefinition(DescriptorProto msgType) {
        mMsgType = msgType;
    }

    private final DescriptorProto mMsgType;

    public static class Builder {
        // --- public ---

        public Builder addField(String label, String type, String name, int num) {
            return addField(label, type, name, num, null);
        }

        public Builder addField(String label, String type, String name, int num, String defaultVal) {
            var protoLabel = sLabelMap.get(label);
            if (protoLabel == null)
                throw new IllegalArgumentException("Illegal label: " + label);
            addField(protoLabel, type, name, num, defaultVal, null);
            return this;
        }

        public OneofBuilder addOneof(String oneofName) {
            mMsgTypeBuilder.addOneofDecl(OneofDescriptorProto.newBuilder().setName(oneofName).build());
            return new OneofBuilder(this, mOneofIndex++);
        }

        public Builder addMessageDefinition(MessageDefinition msgDef) {
            mMsgTypeBuilder.addNestedType(msgDef.getMessageType());
            return this;
        }

        public Builder addEnumDefinition(EnumDefinition enumDef) {
            mMsgTypeBuilder.addEnumType(enumDef.getEnumType());
            return this;
        }

        public MessageDefinition build() {
            return new MessageDefinition(mMsgTypeBuilder.build());
        }


        public Builder setOptions(DescriptorProtos.MessageOptions options) {
            mMsgTypeBuilder.setOptions(options);
            return this;
        }

        public Builder addSchemaNameExtension() {
            mMsgTypeBuilder.addExtension(
                    FieldDescriptorProto.newBuilder()
                            .setName("schema_name")
                            .setLabel(FieldDescriptorProto.Label.LABEL_OPTIONAL)
                            .setType(FieldDescriptorProto.Type.TYPE_STRING)
                            .setExtendee("google.protobuf.MessageOptions")
                            .setNumber(1001));
            return this;
        }

        public Builder addProjectNameExtension() {
            mMsgTypeBuilder.addExtension(
                    FieldDescriptorProto.newBuilder()
                            .setName("project_name")
                            .setLabel(FieldDescriptorProto.Label.LABEL_OPTIONAL)
                            .setType(FieldDescriptorProto.Type.TYPE_STRING)
                            .setExtendee("google.protobuf.MessageOptions")
                            .setNumber(1002));
            return this;
        }

        public Builder addEnabledCanonicalFieldsExtension() {
            mMsgTypeBuilder.addExtension(
                    FieldDescriptorProto.newBuilder()
                            .setName("enabled_canonical_fields")
                            .setLabel(FieldDescriptorProto.Label.LABEL_OPTIONAL)
                            .setType(FieldDescriptorProto.Type.TYPE_STRING)
                            .setExtendee("google.protobuf.MessageOptions")
                            .setNumber(1003));
            return this;
        }

        public Builder setMessageOptions(DescriptorProtos.MessageOptions options) {
            mMsgTypeBuilder.setOptions(options);
            return this;
        }

        private Builder(String msgTypeName) {
            mMsgTypeBuilder = DescriptorProto.newBuilder();
            mMsgTypeBuilder.setName(msgTypeName);
        }

        private void addField(FieldDescriptorProto.Label label, String type, String name, int num, String defaultVal,
                              OneofBuilder oneofBuilder) {
            FieldDescriptorProto.Builder fieldBuilder = FieldDescriptorProto.newBuilder();
            fieldBuilder.setLabel(label);
            FieldDescriptorProto.Type primType = sTypeMap.get(type);
            if (primType != null)
                fieldBuilder.setType(primType);
            else
                fieldBuilder.setTypeName(type);
            fieldBuilder.setName(name).setNumber(num);
            if (defaultVal != null)
                fieldBuilder.setDefaultValue(defaultVal);
            if (oneofBuilder != null)
                fieldBuilder.setOneofIndex(oneofBuilder.getIdx());
            mMsgTypeBuilder.addField(fieldBuilder.build());
        }

        private final DescriptorProto.Builder mMsgTypeBuilder;
        private int mOneofIndex = 0;
    }

    public static class OneofBuilder {
        public OneofBuilder addField(String type, String name, int num) {
            return addField(type, name, num, null);
        }

        public OneofBuilder addField(String type, String name, int num, String defaultVal) {
            mMsgBuilder.addField(FieldDescriptorProto.Label.LABEL_OPTIONAL, type, name, num, defaultVal, this);
            return this;
        }

        public Builder msgDefBuilder() {
            return mMsgBuilder;
        }

        public int getIdx() {
            return mIdx;
        }

        private OneofBuilder(Builder msgBuilder, int oneofIdx) {
            mMsgBuilder = msgBuilder;
            mIdx = oneofIdx;
        }

        private final Builder mMsgBuilder;
        private final int mIdx;
    }

    private static final Map<String, FieldDescriptorProto.Type> sTypeMap = new HashMap<>();
    private static final Map<String, FieldDescriptorProto.Label> sLabelMap = new HashMap<>();

    static {
        sTypeMap.put("double", FieldDescriptorProto.Type.TYPE_DOUBLE);
        sTypeMap.put("float", FieldDescriptorProto.Type.TYPE_FLOAT);
        sTypeMap.put("int32", FieldDescriptorProto.Type.TYPE_INT32);
        sTypeMap.put("int64", FieldDescriptorProto.Type.TYPE_INT64);
        sTypeMap.put("uint32", FieldDescriptorProto.Type.TYPE_UINT32);
        sTypeMap.put("uint64", FieldDescriptorProto.Type.TYPE_UINT64);
        sTypeMap.put("sint32", FieldDescriptorProto.Type.TYPE_SINT32);
        sTypeMap.put("sint64", FieldDescriptorProto.Type.TYPE_SINT64);
        sTypeMap.put("fixed32", FieldDescriptorProto.Type.TYPE_FIXED32);
        sTypeMap.put("fixed64", FieldDescriptorProto.Type.TYPE_FIXED64);
        sTypeMap.put("sfixed32", FieldDescriptorProto.Type.TYPE_SFIXED32);
        sTypeMap.put("sfixed64", FieldDescriptorProto.Type.TYPE_SFIXED64);
        sTypeMap.put("bool", FieldDescriptorProto.Type.TYPE_BOOL);
        sTypeMap.put("string", FieldDescriptorProto.Type.TYPE_STRING);
        sTypeMap.put("bytes", FieldDescriptorProto.Type.TYPE_BYTES);
        sTypeMap.put("enum", FieldDescriptorProto.Type.TYPE_ENUM);
        sTypeMap.put("message", FieldDescriptorProto.Type.TYPE_MESSAGE);
        sTypeMap.put("group", FieldDescriptorProto.Type.TYPE_GROUP);

        sLabelMap.put("optional", FieldDescriptorProto.Label.LABEL_OPTIONAL);
        sLabelMap.put("required", FieldDescriptorProto.Label.LABEL_REQUIRED);
        sLabelMap.put("repeated", FieldDescriptorProto.Label.LABEL_REPEATED);
    }
}