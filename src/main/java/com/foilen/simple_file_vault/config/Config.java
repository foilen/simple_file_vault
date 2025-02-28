package com.foilen.simple_file_vault.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.foilen.smalltools.restapi.model.AbstractApiBase;

import java.util.HashMap;
import java.util.Map;

public class Config extends AbstractApiBase {

    @JsonProperty("public")
    private ConfigPublic publicConfig = new ConfigPublic();
    private Map<String, UserConfigPublic> users = new HashMap<>();

    public ConfigPublic getPublicConfig() {
        return publicConfig;
    }

    public void setPublicConfig(ConfigPublic publicConfig) {
        this.publicConfig = publicConfig;
    }

    public Map<String, UserConfigPublic> getUsers() {
        return users;
    }

    public void setUsers(Map<String, UserConfigPublic> users) {
        this.users = users;
    }

}
