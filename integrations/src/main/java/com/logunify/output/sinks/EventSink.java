package com.logunify.output.sinks;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;

import java.io.IOException;
import java.util.List;

public interface EventSink {
    String getId();
    void write(Descriptors.Descriptor descriptor, List<ByteString> messages) throws IOException;
}
