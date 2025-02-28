package com.foilen.simple_file_vault.services;

import com.foilen.simple_file_vault.config.Config;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.FileTools;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.SystemTools;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class ConfigService extends AbstractBasics {

    private Config config;

    @PostConstruct
    public void reloadConfig() {
        logger.info("Reloading config");

        String configFile = SystemTools.getPropertyOrEnvironment("CONFIG_FILE");
        if (configFile == null) {
            logger.warn("No config file provided. Default: All public access for reading and writing");
            config = new Config();
            config.getPublicConfig().getReadNamespaces().add("*");
            config.getPublicConfig().getWriteNamespaces().add("*");
            return;
        }

        // Load the file
        if (!FileTools.exists(configFile)) {
            logger.error("The config file does not exist: {}", configFile);
            throw new RuntimeException("The config file does not exist: " + configFile);
        }

        logger.info("Using config file: {}", configFile);
        config = JsonTools.readFromFile(configFile, Config.class);
    }

}
