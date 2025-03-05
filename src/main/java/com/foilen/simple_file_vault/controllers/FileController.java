package com.foilen.simple_file_vault.controllers;

import com.foilen.simple_file_vault.services.EntitlementService;
import com.foilen.simple_file_vault.services.FileService;
import com.foilen.smalltools.tools.AbstractBasics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.util.regex.Pattern;

@RestController
public class FileController extends AbstractBasics {

    private static final Pattern ALPHANUMERIC = Pattern.compile("[a-zA-Z0-9_\\-\\.]+");

    @Autowired
    private EntitlementService entitlementService;
    @Autowired
    private FileService fileService;

    @GetMapping(value = "/{namespace}/{version}/{filename:.+}", produces = "application/octet-stream")
    public Resource read(
            Authentication authentication,
            @PathVariable String namespace,
            @PathVariable String version,
            @PathVariable String filename
    ) {

        // Check entitlement
        if (!entitlementService.canRead(authentication == null ? null : authentication.getName(), namespace)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not allowed to read in this namespace");
        }

        // Read the file
        var content = fileService.read(namespace, version, filename);
        if (content == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }

        return content;
    }

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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not allowed to write in this namespace");
        }

        // Validate values are alphanumeric
        if (!ALPHANUMERIC.matcher(namespace).matches()) {
            return ResponseEntity.badRequest().body("Namespace must be alphanumeric");
        }
        if (!ALPHANUMERIC.matcher(version).matches()) {
            return ResponseEntity.badRequest().body("Version must be alphanumeric");
        }
        if (!ALPHANUMERIC.matcher(filename).matches()) {
            return ResponseEntity.badRequest().body("Filename must be alphanumeric");
        }

        // Write the file
        fileService.write(namespace, version, filename, fileInputStream);

        return ResponseEntity.ok("File written successfully");
    }

}
