package com.logunify.configuration;

import com.logunify.output.SinkLoader;
import com.logunify.output.SinkRegistry;
import com.logunify.schema_loader.SchemaLoader;
import com.logunify.schema_loader.SchemaRegistry;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

@Component
public class ConfigProvider {
    @Data
    @Component
    @ConfigurationProperties(prefix = "package-builder")
    public static class PackageBuilderConfig {
        private String gradlePath;
        private String protocPluginPath;
        private String protocPath;
        private String androidSdkPath;
        private String swiftPluginPath;
        private String typescriptPluginPath;
    }

    @Data
    @Component
    @ConfigurationProperties(prefix = "schema")
    public static class SchemaConfig {
        @NotBlank
        private String location;
        private String orgName;
    }

    @Data
    @Component
    @Configuration
    @ConfigurationProperties(prefix = "security")
    @Validated
    public static class SecurityConfig {
        enum AuthType {
            BASIC,
            NONE
        }

        @Data
        public static class BasicAuthConfig {
            private Set<String> apiKeys;
        }

        @NotNull
        AuthType authType;

        BasicAuthConfig basicAuthConfig;

        public void setBasicAuth(BasicAuthConfig basicAuth) {
            basicAuthConfig = basicAuth;
        }
    }

    @Autowired
    public ConfigProvider(SchemaConfig configuration) {
        this.schemaConfig = configuration;
    }


    @Autowired
    private final SchemaConfig schemaConfig;

    @Bean
    public SchemaRegistry schemaRegistry() {
        return new SchemaRegistry();
    }

    @Bean
    public SinkRegistry sinkRegistry() {
        return new SinkRegistry();
    }

    @Bean
    public SchemaLoader loadSchemas(SchemaRegistry schemaRegistry) throws IOException {
        var schemaLoader = new SchemaLoader(schemaRegistry);
        schemaLoader.loadSchema(schemaConfig.location);

        return schemaLoader;
    }

    @Bean
    public SinkLoader loadSinks(SinkRegistry sinkRegistry, SchemaRegistry schemaRegistry) {
        var sinkLoader = new SinkLoader(sinkRegistry);
        sinkLoader.loadSinks(new ArrayList<>(schemaRegistry.getSchemaDefinitions().values()));

        return new SinkLoader(sinkRegistry);
    }
}
