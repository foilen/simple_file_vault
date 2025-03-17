package com.foilen.simple_file_vault.services;

import com.foilen.smalltools.tools.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class FileService extends AbstractBasics {

    private static final String HTML_HEADER = "<html><head><title>Simple File Vault</title></head><body>";
    private static final String HTML_FOOTER = "</body></html>";

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
        content.append("<h1>Namespaces you can read</h1>\n");
        content.append("<ul>\n");
        Arrays.stream(new File(dataFolder).listFiles())
                .filter(File::isDirectory)
                .map(File::getName)
                .filter(namespace -> entitlementService.canRead(username, namespace))
                .sorted()
                .forEach(file ->
                        content.append("<li><a href=\"").append(file).append("/\">").append(file).append("</a></li>\n")
                );
        content.append("</ul>\n");
        content.append(HTML_FOOTER);
        return content.toString();
    }

    public String listVersions(String namespace) {
        String folder = dataFolder + "/" + namespace;
        if (!FileTools.exists(folder)) {
            return null;
        }
        StringBuilder content = new StringBuilder(HTML_HEADER);

        List<String> versions = new ArrayList<>();
        List<String> tags = new ArrayList<>();
        Arrays.stream(new File(folder).listFiles())
                .forEach(file -> {
                    if (file.isDirectory()) {
                        versions.add(file.getName());
                    } else {
                        tags.add(file.getName());
                    }
                });
        Collections.sort(versions);
        Collections.sort(tags);

        content.append("<h1>Versions</h1>\n");
        content.append("<ul>\n");
        versions.forEach(file ->
                content.append("<li><a href=\"").append(file).append("/\">").append(file).append("</a></li>\n")
        );
        content.append("</ul>\n");

        content.append("<h1>Tags</h1>\n");
        content.append("<ul>\n");
        tags.forEach(file ->
                content.append("<li><a href=\"").append(file).append("/\">").append(file).append("</a>")
                        .append(" (<a href=\"tags/").append(file).append("\">resolve</a>)</li>\n")
        );
        content.append("</ul>\n");

        content.append(HTML_FOOTER);
        return content.toString();
    }

    public String listFiles(String namespace, String versionOrTag) {
        versionOrTag = resolveVersion(namespace, versionOrTag);

        String folder = dataFolder + "/" + namespace + "/" + versionOrTag;
        if (!FileTools.exists(folder)) {
            return null;
        }
        StringBuilder content = new StringBuilder(HTML_HEADER);
        content.append("<h1>Files</h1>\n");
        content.append("<ul>\n");
        Arrays.stream(new File(folder).listFiles())
                .map(File::getName)
                .sorted()
                .forEach(file ->
                        content.append("<li><a href=\"").append(file).append("\">").append(file).append("</a></li>\n")
                );
        content.append("</ul>\n");
        content.append(HTML_FOOTER);
        return content.toString();
    }

    public Resource read(String namespace, String versionOrTag, String filename) {
        versionOrTag = resolveVersion(namespace, versionOrTag);

        String filePath = dataFolder + "/" + namespace + "/" + versionOrTag + "/" + filename;
        logger.info("Reading file: {}", filePath);
        if (!FileTools.exists(filePath)) {
            logger.info("File does not exist: {}", filePath);
            return null;
        }
        return new FileSystemResource(filePath);
    }

    public void write(String namespace, String version, String filename, InputStream fileInputStream) {
        // Version must not be a tag
        String tagFolder = dataFolder + "/" + namespace + "/" + version;
        if (new File(tagFolder).isFile()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This version is a tag");
        }

        String folder = dataFolder + "/" + namespace + "/" + version;
        DirectoryTools.createPath(folder);
        String filePath = folder + "/" + filename;
        logger.info("Writing file: {}", filePath);
        try (var fileOutputStream = new FileOutputStream(filePath)) {
            StreamsTools.flowStream(fileInputStream, fileOutputStream);
        } catch (IOException e) {
            throw new ResponseStatusException(500, "Problem writing the file", e);
        }
    }

    private String resolveVersion(String namespace, String versionOrTag) {
        String tagFile = dataFolder + "/" + namespace + "/" + versionOrTag;
        if (new File(tagFile).isFile()) {
            versionOrTag = FileTools.getFileAsString(tagFile);
        }
        return versionOrTag;
    }

    public String tagGetVersion(String namespace, String tag) {
        String namespaceFolder = dataFolder + "/" + namespace;
        if (!FileTools.exists(namespaceFolder)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Namespace does not exist: " + namespace);
        }
        String tagFile = namespaceFolder + "/" + tag;
        if (!FileTools.exists(tagFile)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag does not exist");
        }
        if (!new File(tagFile).isFile()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag does not exist");
        }
        return FileTools.getFileAsString(tagFile);
    }

    public void tagSetVersion(String namespace, String tag, String version) {
        String namespaceFolder = dataFolder + "/" + namespace;
        if (!FileTools.exists(namespaceFolder)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Namespace does not exist");
        }
        String versionFolder = namespaceFolder + "/" + version;
        if (!FileTools.exists(versionFolder)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Version does not exist");
        }
        String tagFile = namespaceFolder + "/" + tag;
        FileTools.writeFile(version, tagFile);
    }

}
