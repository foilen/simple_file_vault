package com.foilen.simple_file_vault.services;

import com.foilen.smalltools.tools.AbstractBasics;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ExecutionException;

@Service
public class EntitlementService extends AbstractBasics {

    @Autowired
    private ConfigService configService;

    private final Cache<String, Boolean> canReadByUsernameAndNamespaceCache = CacheBuilder.newBuilder().maximumSize(1000).build();
    private final Cache<String, Boolean> canWriteByUsernameAndNamespaceCache = CacheBuilder.newBuilder().maximumSize(1000).build();

    public boolean canRead(String username, String namespace) {
        try {
            return canReadByUsernameAndNamespaceCache.get(username + "|" + namespace, () -> {
                if (username == null) {
                    return namespaceIn(namespace, configService.getConfig().getPublicConfig().getReadNamespaces());
                }
                var userConfig = configService.getConfig().getUsers().get(username);
                if (userConfig == null) {
                    return false;
                }
                return namespaceIn(namespace, userConfig.getReadNamespaces());
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean canWrite(String username, String namespace) {
        try {
            return canWriteByUsernameAndNamespaceCache.get(username + "|" + namespace, () -> {
                if (username == null) {
                    return namespaceIn(namespace, configService.getConfig().getPublicConfig().getWriteNamespaces());
                }
                var userConfig = configService.getConfig().getUsers().get(username);
                if (userConfig == null) {
                    return false;
                }
                return namespaceIn(namespace, userConfig.getWriteNamespaces());
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected static Boolean namespaceIn(String toCheck, Set<String> namespaces) {
        if (Strings.isNullOrEmpty(toCheck)) {
            return false;
        }
        if (toCheck.contains("*")) {
            return false;
        }
        if (namespaces.contains(toCheck)) {
            return true;
        }
        // Check wildcards
        for (String namespace : namespaces) {
            int wildCardIndex = namespace.indexOf("*");
            if (wildCardIndex == -1) {
                continue;
            }

            // Begin
            if (wildCardIndex == 0
                    && toCheck.endsWith(namespace.substring(1))) {
                return true;
            }

            // End
            if (wildCardIndex == namespace.length() - 1
                    && toCheck.startsWith(namespace.substring(0, namespace.length() - 1))) {
                return true;
            }

            // Middle
            if (wildCardIndex != 0 && wildCardIndex != namespace.length() - 1
                    && toCheck.startsWith(namespace.substring(0, wildCardIndex))
                    && toCheck.endsWith(namespace.substring(wildCardIndex + 1))) {
                return true;
            }
        }
        return false;
    }

}
