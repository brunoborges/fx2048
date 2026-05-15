package io.github.brunoborges.fx2048.app;


import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Properties;

final class AppMetadata {

    private static final String METADATA_RESOURCE = "/io/github/brunoborges/fx2048/app.properties";
    private static final String VERSION_PROPERTY = "app.version";

    static final String VERSION = loadVersion();

    private AppMetadata() {
    }

    private static String loadVersion() {
        var properties = new Properties();
        try (var in = AppMetadata.class.getResourceAsStream(METADATA_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing application metadata resource: " + METADATA_RESOURCE);
            }
            properties.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to load application metadata", e);
        }

        var version = properties.getProperty(VERSION_PROPERTY);
        if (version == null || version.isBlank()) {
            throw new IllegalStateException("Missing application version in " + METADATA_RESOURCE);
        }
        return version;
    }
}
