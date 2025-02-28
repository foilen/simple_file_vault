package com.foilen.simple_file_vault.config;

import com.foilen.smalltools.restapi.model.AbstractApiBase;

import java.util.HashSet;
import java.util.Set;

public class ConfigPublic extends AbstractApiBase {

    private Set<String> writeNamespaces = new HashSet<>();
    private Set<String> readNamespaces = new HashSet<>();

    public Set<String> getReadNamespaces() {
        return readNamespaces;
    }

    public void setReadNamespaces(Set<String> readNamespaces) {
        this.readNamespaces = readNamespaces;
    }

    public Set<String> getWriteNamespaces() {
        return writeNamespaces;
    }

    public void setWriteNamespaces(Set<String> writeNamespaces) {
        this.writeNamespaces = writeNamespaces;
    }

}
