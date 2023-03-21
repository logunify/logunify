package com.logunify.protobuf_schema.definitions;

import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumValueDescriptorProto;

public class EnumDefinition {

    public static Builder newBuilder(String enumName) {
        return new Builder(enumName);
    }

    public String toString() {
        return mEnumType.toString();
    }

    public EnumDescriptorProto getEnumType() {
        return mEnumType;
    }

    private EnumDefinition(EnumDescriptorProto enumType) {
        mEnumType = enumType;
    }

    private final EnumDescriptorProto mEnumType;

    public static class Builder {
        public Builder addValue(String name, int num) {
            var enumValue = EnumValueDescriptorProto.newBuilder()
                    .setName(name)
                    .setNumber(num)
                    .build();
            mEnumTypeBuilder.addValue(enumValue);
            return this;
        }

        public EnumDefinition build() {
            return new EnumDefinition(mEnumTypeBuilder.build());
        }

        private Builder(String enumName) {
            mEnumTypeBuilder = EnumDescriptorProto.newBuilder();
            mEnumTypeBuilder.setName(enumName);
        }

        private final EnumDescriptorProto.Builder mEnumTypeBuilder;
    }
}