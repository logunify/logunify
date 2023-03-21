package com.logunify.protobuf_schema;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.Descriptors.*;
import com.google.protobuf.DynamicMessage;
import com.logunify.protobuf_schema.definitions.EnumDefinition;
import com.logunify.protobuf_schema.definitions.MessageDefinition;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class ProtobufSchema {

    @Getter
    @Setter
    private String projectName;

    @Getter
    @Setter
    private String orgName;


    public static Builder newBuilder() {
        return new Builder();
    }

    public DynamicMessage.Builder newMessageBuilder(String msgTypeName) {
        var msgType = getMessageDescriptor(msgTypeName);
        if (msgType == null)
            return null;
        return DynamicMessage.newBuilder(msgType);
    }

    public Descriptor getMessageDescriptor(String msgTypeName) {
        var msgType = mMsgDescriptorMapShort.get(msgTypeName);
        if (msgType == null)
            msgType = mMsgDescriptorMapFull.get(msgTypeName);
        return msgType;
    }

    public EnumValueDescriptor getEnumValue(String enumTypeName, String enumName) {
        var enumType = getEnumDescriptor(enumTypeName);
        if (enumType == null)
            return null;
        return enumType.findValueByName(enumName);
    }

    public EnumValueDescriptor getEnumValue(String enumTypeName, int enumNumber) {
        var enumType = getEnumDescriptor(enumTypeName);
        if (enumType == null)
            return null;
        return enumType.findValueByNumber(enumNumber);
    }

    public EnumDescriptor getEnumDescriptor(String enumTypeName) {
        var enumType = mEnumDescriptorMapShort.get(enumTypeName);
        if (enumType == null)
            enumType = mEnumDescriptorMapFull.get(enumTypeName);
        return enumType;
    }

    public Set<String> getMessageTypes() {
        return new TreeSet<String>(mMsgDescriptorMapFull.keySet());
    }

    public Set<String> getEnumTypes() {
        return new TreeSet<String>(mEnumDescriptorMapFull.keySet());
    }

    public FileDescriptorSet getFileDescriptorSet() {
        return mFileDescSet;
    }

    public String getName() {
        return mFileDescSet.getFile(0).getName();
    }

    public String getPackageName() {
        return mFileDescSet.getFile(0).getPackage();
    }

    public byte[] toByteArray() {
        return mFileDescSet.toByteArray();
    }

    public String toString() {
        var msgTypes = getMessageTypes();
        var enumTypes = getEnumTypes();
        return "types: " + msgTypes + "\nenums: " + enumTypes + "\n" + mFileDescSet;
    }

    private ProtobufSchema(FileDescriptorSet fileDescSet) throws DescriptorValidationException {
        mFileDescSet = fileDescSet;
        var fileDescMap = init(fileDescSet);

        var msgDupes = new HashSet<String>();
        var enumDupes = new HashSet<String>();
        for (FileDescriptor fileDesc : fileDescMap.values()) {
            for (Descriptor msgType : fileDesc.getMessageTypes())
                addMessageType(msgType, null, msgDupes, enumDupes);
            for (EnumDescriptor enumType : fileDesc.getEnumTypes())
                addEnumType(enumType, null, enumDupes);
        }

        for (String msgName : msgDupes)
            mMsgDescriptorMapShort.remove(msgName);
        for (String enumName : enumDupes)
            mEnumDescriptorMapShort.remove(enumName);
    }

    private Map<String, FileDescriptor> init(FileDescriptorSet fileDescSet) throws DescriptorValidationException {
        // check for dupes
        var allFdProtoNames = new HashSet<String>();
        for (FileDescriptorProto fdProto : fileDescSet.getFileList()) {
            if (allFdProtoNames.contains(fdProto.getName()))
                throw new IllegalArgumentException("duplicate name: " + fdProto.getName());
            allFdProtoNames.add(fdProto.getName());
        }

        allFdProtoNames.add("google/protobuf/descriptor.proto");

        // build FileDescriptors, resolve dependencies (imports) if any
        Map<String, FileDescriptor> resolvedFileDescMap = new HashMap<String, FileDescriptor>();
        resolvedFileDescMap.put("google/protobuf/descriptor.proto",
                DescriptorProtos.DescriptorProto.getDescriptor().getFile());

        while (resolvedFileDescMap.size() < fileDescSet.getFileCount() + 1) {
            for (FileDescriptorProto fdProto : fileDescSet.getFileList()) {
                if (resolvedFileDescMap.containsKey(fdProto.getName()))
                    continue;

                List<String> dependencyList = fdProto.getDependencyList();
                List<FileDescriptor> resolvedFdList = new ArrayList<FileDescriptor>();
                for (String depName : dependencyList) {
                    if (!allFdProtoNames.contains(depName))
                        throw new IllegalArgumentException("cannot resolve import " + depName + " in " + fdProto.getName());
                    FileDescriptor fd = resolvedFileDescMap.get(depName);
                    if (fd != null)
                        resolvedFdList.add(fd);
                }

                if (resolvedFdList.size() == dependencyList.size()) { // dependencies resolved
                    FileDescriptor[] fds = new FileDescriptor[resolvedFdList.size()];
                    FileDescriptor fd = FileDescriptor.buildFrom(fdProto, resolvedFdList.toArray(fds));
                    resolvedFileDescMap.put(fdProto.getName(), fd);
                }
            }
        }

        return resolvedFileDescMap;
    }

    private void addMessageType(Descriptor msgType, String scope, Set<String> msgDupes, Set<String> enumDupes) {
        var msgTypeNameFull = msgType.getFullName();
        var msgTypeNameShort = (scope == null ? msgType.getName() : scope + "." + msgType.getName());

        if (mMsgDescriptorMapFull.containsKey(msgTypeNameFull))
            throw new IllegalArgumentException("duplicate name: " + msgTypeNameFull);
        if (mMsgDescriptorMapShort.containsKey(msgTypeNameShort))
            msgDupes.add(msgTypeNameShort);

        mMsgDescriptorMapFull.put(msgTypeNameFull, msgType);
        mMsgDescriptorMapShort.put(msgTypeNameShort, msgType);

        for (Descriptor nestedType : msgType.getNestedTypes())
            addMessageType(nestedType, msgTypeNameShort, msgDupes, enumDupes);
        for (EnumDescriptor enumType : msgType.getEnumTypes())
            addEnumType(enumType, msgTypeNameShort, enumDupes);
    }

    private void addEnumType(EnumDescriptor enumType, String scope, Set<String> enumDupes) {
        var enumTypeNameFull = enumType.getFullName();
        var enumTypeNameShort = (scope == null ? enumType.getName() : scope + "." + enumType.getName());

        if (mEnumDescriptorMapFull.containsKey(enumTypeNameFull))
            throw new IllegalArgumentException("duplicate name: " + enumTypeNameFull);
        if (mEnumDescriptorMapShort.containsKey(enumTypeNameShort))
            enumDupes.add(enumTypeNameShort);

        mEnumDescriptorMapFull.put(enumTypeNameFull, enumType);
        mEnumDescriptorMapShort.put(enumTypeNameShort, enumType);
    }

    private final FileDescriptorSet mFileDescSet;
    private final Map<String, Descriptor> mMsgDescriptorMapFull = new HashMap<String, Descriptor>();
    private final Map<String, Descriptor> mMsgDescriptorMapShort = new HashMap<String, Descriptor>();
    private final Map<String, EnumDescriptor> mEnumDescriptorMapFull = new HashMap<String, EnumDescriptor>();
    private final Map<String, EnumDescriptor> mEnumDescriptorMapShort = new HashMap<String, EnumDescriptor>();

    public static class Builder {

        private String projectName;

        public Builder setProjectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        private String orgName;

        public Builder setOrgName(String orgName) {
            this.orgName = orgName;
            return this;
        }

        public ProtobufSchema build() throws DescriptorValidationException {
            FileDescriptorSet.Builder fileDescSetBuilder = FileDescriptorSet.newBuilder();
            fileDescSetBuilder.addFile(mFileDescProtoBuilder.build());
            fileDescSetBuilder.mergeFrom(mFileDescSetBuilder.build());
            var schema = new ProtobufSchema(fileDescSetBuilder.build());
            schema.setProjectName(projectName);
            schema.setOrgName(orgName);
            return schema;
        }

        public Builder setName(String name) {
            mFileDescProtoBuilder.setName(name);
            return this;
        }

        public Builder setPackage(String name) {
            mFileDescProtoBuilder.setPackage(name);
            return this;
        }

        public Builder addMessageDefinition(MessageDefinition msgDef) {
            mFileDescProtoBuilder.addMessageType(msgDef.getMessageType());
            return this;
        }

        public Builder addEnumDefinition(EnumDefinition enumDef) {
            mFileDescProtoBuilder.addEnumType(enumDef.getEnumType());
            return this;
        }

        public Builder addDependency(String dependency) {
            mFileDescProtoBuilder.addDependency(dependency);
            return this;
        }

        public Builder addPublicDependency(String dependency) {
            mFileDescProtoBuilder.addPublicDependency(mFileDescProtoBuilder.getDependencyCount() - 1);
            return this;
        }

        public Builder addSchema(ProtobufSchema schema) {
            mFileDescSetBuilder.mergeFrom(schema.mFileDescSet);
            return this;
        }

        private Builder() {
            mFileDescProtoBuilder = FileDescriptorProto.newBuilder();
            mFileDescSetBuilder = FileDescriptorSet.newBuilder();
        }

        private final FileDescriptorProto.Builder mFileDescProtoBuilder;
        private final FileDescriptorSet.Builder mFileDescSetBuilder;

        public Builder setFileOption(FileOptions fileOption) {
            mFileDescProtoBuilder.setOptions(fileOption);
            return this;
        }
    }
}