package com.logunify.output.sinks;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
public abstract class BaseEventSink implements EventSink {
    protected final String outputId;

    public BaseEventSink(String outputId) {
        this.outputId = outputId;
    }

    @Override
    public String getId() {
        return outputId;
    }

    protected void checkSchema() throws IOException {
        if (!this.isSchemaCompatible()) {
            if (isSchemaUpgradeable()) {
                log.info("{} schema is not compatible but upgradable, upgrading schema", outputId);
                upgradeSchema();
            } else {
                throw new IOException("Schema is not compatible and cannot be upgraded");
            }
        }
        log.info("{} passed schema check", outputId);
    }

    public abstract void write(Descriptors.Descriptor descriptor, List<ByteString> messages) throws IOException;

    protected abstract boolean isSchemaCompatible() throws IOException;

    protected abstract boolean isSchemaUpgradeable() throws IOException;

    protected abstract void upgradeSchema() throws IOException;
}
