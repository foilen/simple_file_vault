package com.foilen.simple_file_vault.services;

import com.foilen.simple_file_vault.config.Config;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.FileTools;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.SystemTools;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ConfigService extends AbstractBasics {

    private Config config;

    @PostConstruct
    public void init() {
        logger.info("Load config");

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

        // Validate all namespaces have maximum 1 *
        validateWildcards(config.getPublicConfig().getReadNamespaces());
        validateWildcards(config.getPublicConfig().getWriteNamespaces());
        config.getUsers().values().forEach(userConfig -> {
            validateWildcards(userConfig.getReadNamespaces());
            validateWildcards(userConfig.getWriteNamespaces());
        });
        
    }

    private void validateWildcards(Set<String> readNamespaces) {
        readNamespaces.forEach(namespace -> {
            if (namespace.chars().filter(ch -> ch == '*').count() > 1) {
                logger.error("The namespace '{}' has more than 1 *", namespace);
                throw new RuntimeException("The namespace '" + namespace + "' has more than 1 *");
            }
        });
    }

    public Config getConfig() {
        return config;
    }

}
