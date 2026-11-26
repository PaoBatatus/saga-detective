package com.example.demo.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("oauth-security")
@Component
@ConfigurationProperties(prefix = "app.oauth")
public class ClienteOAuthProperties {

    public static class Cliente {

        private String id;
        private String secret;
        private List<String> scopes = new ArrayList<>();

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public List<String> getScopes() {
            return scopes;
        }

        public void setScopes(List<String> scopes) {
            this.scopes = scopes;
        }
    }

    private List<Cliente> clients = new ArrayList<>();

    public List<Cliente> getClients() {
        return clients;
    }

    public void setClients(List<Cliente> clients) {
        this.clients = clients;
    }
}