package com.foilen.simple_file_vault.services;

import com.foilen.smalltools.tools.*;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class FileService extends AbstractBasics {

    private String dataFolder;

    @PostConstruct
    public void init() {
        dataFolder = SystemTools.getPropertyOrEnvironment("DATA_FOLDER", "/tmp/simple-file-vault");
        logger.info("Data folder: {}", dataFolder);
        DirectoryTools.createPath(dataFolder);
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
