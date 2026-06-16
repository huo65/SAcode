package com.DB.DBmarket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    private String provider = "local";
    private Local local = new Local();

    @Data
    public static class Local {
        private String baseDir = "data/storage";
        private String publicUrlPrefix = "/storage";

        public Path resolveBaseDir() {
            Path configuredPath = Paths.get(baseDir);
            if (configuredPath.isAbsolute()) {
                return configuredPath.normalize();
            }

            Path moduleRoot = findBackendModuleRoot();
            if (moduleRoot != null) {
                return moduleRoot.resolve(configuredPath).normalize();
            }

            return configuredPath.toAbsolutePath().normalize();
        }

        private Path findBackendModuleRoot() {
            Path current = Paths.get("").toAbsolutePath().normalize();
            while (current != null) {
                if (isBackendModuleRoot(current)) {
                    return current;
                }

                Path nestedFromCode = current.resolve("backend/DB-market").normalize();
                if (isBackendModuleRoot(nestedFromCode)) {
                    return nestedFromCode;
                }

                Path nestedFromWorkspace = current.resolve("code/backend/DB-market").normalize();
                if (isBackendModuleRoot(nestedFromWorkspace)) {
                    return nestedFromWorkspace;
                }

                current = current.getParent();
            }
            return null;
        }

        private boolean isBackendModuleRoot(Path path) {
            return path.resolve("pom.xml").toFile().isFile()
                    && path.resolve("src/main/resources/schema.sql").toFile().isFile();
        }
    }
}
