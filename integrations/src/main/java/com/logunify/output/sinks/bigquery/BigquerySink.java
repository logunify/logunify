package com.logunify.output.sinks.bigquery;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.*;
import com.google.cloud.bigquery.storage.v1.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.logunify.entity.SchemaDefinition;
import com.logunify.output.sinks.BaseEventSink;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class BigquerySink extends BaseEventSink {
    public static final String CONFIG_KEY_PROJECT = "gcpProject";
    public static final String CONFIG_KEY_DATASET = "dataset";
    public static final String CONFIG_KEY_TABLE = "table";
    public static final String CONFIG_KEY_KEY = "key";

    private final BigQuery bigQuery;

    private final String project;
    private final String dataset;
    private final String table;
    private final SchemaDefinition.Schema schema;
    private final BigQueryWriteSettings bigQueryWriteSettings;
    private final CredentialsProvider credentialsProvider;

    public BigquerySink(Map<String, String> configs, SchemaDefinition.Schema schema) throws IOException {
        super(String.format("BigQuery/%s.%s.%s",
                configs.get(CONFIG_KEY_PROJECT),
                configs.get(CONFIG_KEY_DATASET),
                configs.get(CONFIG_KEY_TABLE))
        );

        var project = configs.get(CONFIG_KEY_PROJECT);
        var dataset = configs.get(CONFIG_KEY_DATASET);
        var table = configs.get(CONFIG_KEY_TABLE);
        var key = configs.getOrDefault(CONFIG_KEY_KEY, null);

        if (key == null) {
            key = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        }

        var f = new File(key);
        ServiceAccountCredentials bqCredential;
        if (f.exists()) {
            bqCredential = ServiceAccountCredentials.fromStream(new FileInputStream(key));
        } else {
            bqCredential = ServiceAccountCredentials.fromStream(new ByteArrayInputStream(key.getBytes()));
        }

        credentialsProvider = FixedCredentialsProvider.create(bqCredential);
        bigQueryWriteSettings = BigQueryWriteSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(bqCredential))
                .build();

        bigQuery = BigQueryOptions.newBuilder().setCredentials(bqCredential)
                .build()
                .getService();

        this.project = project;
        this.dataset = dataset;
        this.table = table;
        this.schema = schema;

        var bqTable = bigQuery.getTable(TableId.of(project, dataset, table));
        if (bqTable == null || !bqTable.exists()) {
            log.info("Table {}.{}.{} does not exist, create table", project, dataset, table);
            this.createTable();
        } else {
            this.checkSchema();
        }

        log.info("BigQuerySink initialized with project: " + project + ", dataset: " + dataset + ", table: " + table);
    }

    @Override
    public void write(Descriptors.Descriptor descriptor, List<ByteString> messages) throws IOException {
        WriteStream s = WriteStream.newBuilder().setType(WriteStream.Type.COMMITTED).build();
        TableName parent = TableName.of(project, dataset, table);
        CreateWriteStreamRequest request = CreateWriteStreamRequest.newBuilder()
                .setParent(parent.toString())
                .setWriteStream(s)
                .build();
        try (var client = BigQueryWriteClient.create(bigQueryWriteSettings)) {
            WriteStream writeStream = client.createWriteStream(request);

            var protobufSchema = ProtoSchemaConverter.convert(descriptor);
            try (StreamWriter writer =
                         StreamWriter.newBuilder(writeStream.getName()).setCredentialsProvider(credentialsProvider).setWriterSchema(protobufSchema).build()) {
                ProtoRows.Builder rowsBuilder = ProtoRows.newBuilder();
                rowsBuilder.addAllSerializedRows(messages);
                writer.append(rowsBuilder.build());
            }
        }
    }

    private void createTable() {
        var partitioning = TimePartitioning.newBuilder(TimePartitioning.Type.DAY).build();
        var tableDefinition = StandardTableDefinition.newBuilder()
                .setSchema(SchemaConverter.fromSchema(schema))
                .setTimePartitioning(partitioning).build();
        var tableInfo = TableInfo.newBuilder(TableId.of(project, dataset, table), tableDefinition)
                .setDescription(schema.getDescription())
                .build();
        bigQuery.create(tableInfo);
        log.info("Table {}.{}.{} created", project, dataset, table);
    }

    @Override
    protected boolean isSchemaCompatible() {
        var bqTable = bigQuery.getTable(TableId.of(project, dataset, table));
        var remoteSchema = bqTable.getDefinition().getSchema();
        if (remoteSchema == null) {
            log.error("Table {}.{}.{} does not exist or does not have a schema", project, dataset, bqTable);
            return false;
        }
        var localSchema = SchemaConverter.fromSchema(schema);
        if (remoteSchema.getFields().size() != localSchema.getFields().size()) {
            log.error(
                    "Table {}.{}.{} has a different number of fields, remote schema has {} fields while local schema has {} fields",
                    project, dataset, bqTable, remoteSchema.getFields().size(), localSchema.getFields().size());
            log.error("Remote fields: {}", remoteSchema.getFields().stream().map(Field::getName).collect(Collectors.joining(", ")));
            log.error("Local fields: {}", localSchema.getFields().stream().map(Field::getName).collect(Collectors.joining(", ")));
            return false;
        }
        return fieldsCompatible(remoteSchema, localSchema);
    }

    @Override
    protected boolean isSchemaUpgradeable() {
        var bigQueryTable = bigQuery.getTable(TableId.of(project, dataset, table));
        var remoteSchema = bigQueryTable.getDefinition().getSchema();
        if (remoteSchema == null) {
            log.error("Table {}.{}.{} does not exist or does not have a schema", project, dataset, table);
            return false;
        }
        var localSchema = SchemaConverter.fromSchema(schema);
        if (remoteSchema.getFields().size() > localSchema.getFields().size()) {
            log.error("Remote schema has more fields than local schema, remote schema has {} fields while local schema has {} fields",
                    remoteSchema.getFields().size(), localSchema.getFields().size());
            log.error("Remote fields: {}", remoteSchema.getFields().stream().map(Field::getName).collect(Collectors.joining(", ")));
            log.error("Local fields: {}", localSchema.getFields().stream().map(Field::getName).collect(Collectors.joining(", ")));
            log.error("Removing fields from remote schema is not supported, please manually update your remote schema or mark the local field as " +
                    "deprecated instead of deleting the field");
            return false;
        }

        // check if the first n fields are the same
        if (!fieldsCompatible(remoteSchema, localSchema)) {
            log.error("First n fields not compatible");
            log.error("only appending fields is supported, please manually update your remote schema or mark the local field as deprecated instead of " +
                    "deleting the field");
            return false;
        }

        return true;
    }

    @Override
    protected void upgradeSchema() {
        var bigQueryTable = bigQuery.getTable(TableId.of(project, dataset, table));
        var remoteSchema = bigQueryTable.getDefinition().getSchema();
        if (remoteSchema == null) {
            log.error("Table {}.{}.{} does not exist or does not have a schema", project, dataset, table);
            throw new RuntimeException("Table does not exist or does not have a schema");
        }
        var newSchema = SchemaConverter.fromSchema(schema);
        var updatedTable = bigQueryTable.toBuilder().setDefinition(bigQueryTable.getDefinition().toBuilder().setSchema(newSchema).build()).build();
        bigQuery.update(updatedTable);
    }

    private boolean fieldsCompatible(Schema remoteSchema, Schema localSchema) {
        for (int i = 0; i < remoteSchema.getFields().size(); i++) {
            var remoteField = remoteSchema.getFields().get(i);
            var localField = localSchema.getFields().get(i);
            if (!remoteField.getName().equals(localField.getName())) {
                log.error("Table {}.{}.{} has a different field name, remote field name is {} while local field name is {}",
                        project, dataset, table, remoteField.getName(), localField.getName());
                return false;
            }
            if (!remoteField.getType().equals(localField.getType())) {
                log.error("Table {}.{}.{} has a different field type, remote field type is {} while local field type is {}",
                        project, dataset, table, remoteField.getType(), localField.getType());
                return false;
            }
            if (remoteField.getMode() != localField.getMode()) {
                log.error("Table {}.{}.{} has a different field mode, remote field mode is {} while local field mode is {}",
                        project, dataset, table, remoteField.getMode(), localField.getMode());
                return false;
            }
        }

        return true;
    }
}
