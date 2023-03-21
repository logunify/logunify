package com.logunify.codegen.package_builder;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PackageBuilderConfig {
    private String logunifyPluginPath;
    private String protocPath;
    private String swiftPluginPath;
    private String typescriptPluginPath;
}
