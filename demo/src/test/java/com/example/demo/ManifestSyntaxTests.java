package com.example.demo;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ManifestSyntaxTests {
    @Test
    void deploymentAndPipelineYamlAreValid() throws Exception {
        for (String name : new String[]{"docker-compose.yml", ".gitlab-ci.yml",
                "k8s/mysql-deployment.yml", "k8s/app-deployment.yml", "k8s/service-monitor.yml"}) {
            int documents = 0;
            try (var input = Files.newInputStream(Path.of(name))) {
                for (Object document : new Yaml().loadAll(input)) {
                    assertTrue(document instanceof java.util.Map, name + " must contain mapping documents");
                    documents++;
                }
            }
            assertTrue(documents > 0, name + " must contain at least one document");
        }
    }
}
