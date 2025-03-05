package com.foilen.simple_file_vault.controllers;

import com.foilen.simple_file_vault.services.EntitlementService;
import com.foilen.simple_file_vault.services.FileService;
import com.foilen.smalltools.tools.AbstractBasics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

@RestController
public class FileController extends AbstractBasics {

    @Autowired
    private EntitlementService entitlementService;
    @Autowired
    private FileService fileService;

    @PostMapping("/{namespace}/{version}/{filename:.+}")
    public ResponseEntity<String> write(
            Authentication authentication,
            @PathVariable String namespace,
            @PathVariable String version,
            @PathVariable String filename,
            InputStream fileInputStream
    ) {

        // Check entitlement
        if (!entitlementService.canWrite(authentication == null ? null : authentication.getName(), namespace)) {
            return ResponseEntity.status(403).body("You are not allowed to write in this namespace");
        }

        // Validate filename is alphanumeric
        if (!filename.matches("[a-zA-Z0-9_\\-\\.]+")) {
            return ResponseEntity.badRequest().body("Filename must be alphanumeric");
        }

        fileService.write(namespace, version, filename, fileInputStream);

        return ResponseEntity.ok("File written successfully");
    }

}
