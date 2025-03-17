package com.foilen.simple_file_vault.services;

import com.foilen.smalltools.tools.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

@Service
public class FileService extends AbstractBasics {

    private static final String HTML_HEADER = "<html><head><title>Simple File Vault</title></head><body><ul>";
    private static final String HTML_FOOTER = "</ul></body></html>";

    @Autowired
    private EntitlementService entitlementService;

    private String dataFolder;

    @PostConstruct
    public void init() {
        dataFolder = SystemTools.getPropertyOrEnvironment("DATA_FOLDER", "/tmp/simple-file-vault");
        logger.info("Data folder: {}", dataFolder);
        DirectoryTools.createPath(dataFolder);
    }

    public String listNamespaces(String username) {
        StringBuilder content = new StringBuilder(HTML_HEADER);
        Arrays.stream(new File(dataFolder).listFiles())
                .filter(File::isDirectory)
                .map(File::getName)
                .filter(namespace -> entitlementService.canRead(username, namespace))
                .sorted()
                .forEach(file ->
                        content.append("<li><a href=\"").append(file).append("/\">").append(file).append("</a></li>\n")
                );
        content.append(HTML_FOOTER);
        return content.toString();
    }

    public String listVersions(String namespace) {
        String folder = dataFolder + "/" + namespace;
        if (!FileTools.exists(folder)) {
            return null;
        }
        StringBuilder content = new StringBuilder(HTML_HEADER);
        Arrays.stream(new File(folder).listFiles())
                .filter(File::isDirectory)
                .map(File::getName)
                .sorted()
                .forEach(file ->
                        content.append("<li><a href=\"").append(file).append("/\">").append(file).append("</a></li>\n")
                );
        content.append(HTML_FOOTER);
        return content.toString();
    }

    public String listFiles(String namespace, String version) {
        String folder = dataFolder + "/" + namespace + "/" + version;
        if (!FileTools.exists(folder)) {
            return null;
        }
        StringBuilder content = new StringBuilder(HTML_HEADER);
        Arrays.stream(new File(folder).listFiles())
                .map(File::getName)
                .sorted()
                .forEach(file ->
                        content.append("<li><a href=\"").append(file).append("\">").append(file).append("</a></li>\n")
                );
        content.append(HTML_FOOTER);
        return content.toString();
    }

    public Resource read(String namespace, String version, String filename) {
        String filePath = dataFolder + "/" + namespace + "/" + version + "/" + filename;
        logger.info("Reading file: {}", filePath);
        if (!FileTools.exists(filePath)) {
            logger.info("File does not exist: {}", filePath);
            return null;
        }
        return new FileSystemResource(filePath);
    }

    public void write(String namespace, String version, String filename, InputStream fileInputStream) {
        String folder = dataFolder + "/" + namespace + "/" + version;
        DirectoryTools.createPath(folder);
        String filePath = folder + "/" + filename;
        logger.info("Writing file: {}", filePath);
        try (var fileOutputStream = new FileOutputStream(filePath)) {
            StreamsTools.flowStream(fileInputStream, fileOutputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
