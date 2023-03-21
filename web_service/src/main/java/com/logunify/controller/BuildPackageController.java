package com.logunify.controller;

import com.logunify.codegen.common.SupportingLanguage;
import com.logunify.lib.package_builder.PackageBuildingService;
import com.logunify.schema_loader.SchemaRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

@Controller
@Slf4j
public class BuildPackageController {
    private final PackageBuildingService packageBuildingService;
    private final SchemaRegistry schemaRegistry;

    @Autowired
    public BuildPackageController(PackageBuildingService packageBuildingService, SchemaRegistry schemaRegistry) {
        this.packageBuildingService = packageBuildingService;
        this.schemaRegistry = schemaRegistry;
    }

    @GetMapping("/download-source-file/{projectName}/{schemaName}")
    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ResponseEntity<InputStreamResource> downloadSourceFileForSchema(
            @PathVariable String projectName,
            @PathVariable String schemaName,
            @RequestParam String language
    ) throws IOException {
        var maybeProjectDefinition = schemaRegistry.getProjectDefinition(projectName);
        if (maybeProjectDefinition.isEmpty()) {
            throw new IllegalArgumentException(String.format("Project %s does not exist.", projectName));
        }
        SupportingLanguage targetLanguage;
        try {
            targetLanguage = SupportingLanguage.valueOf(language.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            var sourceFile = packageBuildingService
                    .buildSourceFileForSchema(
                            maybeProjectDefinition.get(),
                            Set.of(targetLanguage),
                            schemaName
                    )
                    .get(targetLanguage);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", sourceFile.getName()))
                    .body(new InputStreamResource(new FileInputStream(sourceFile)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/download-source-file/{projectName}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> downloadSourceFileForSchema(
            @PathVariable String projectName,
            @RequestParam String language
    ) throws IOException {
        var maybeProjectDefinition = schemaRegistry.getProjectDefinition(projectName);
        if (maybeProjectDefinition.isEmpty()) {
            throw new IllegalArgumentException(String.format("Project %s does not exist.", projectName));
        }
        SupportingLanguage targetLanguage;
        try {
            targetLanguage = SupportingLanguage.valueOf(language.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            var tarFile = packageBuildingService
                    .buildSourceFilesTarball(
                            maybeProjectDefinition.get(),
                            Set.of(targetLanguage)
                    ).get(targetLanguage);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", tarFile.getName()))
                    .body(new InputStreamResource(new FileInputStream(tarFile)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
