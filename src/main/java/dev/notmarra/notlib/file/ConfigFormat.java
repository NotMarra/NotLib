package dev.notmarra.notlib.file;

/**
 * Supported configuration file formats.
 *
 * <p>Currently only YAML is supported ({@code .yml} / {@code .yaml}).
 */
public enum ConfigFormat {
    YAML;

    /**
     * Detects the format from the file name extension.
     *
     * @param fileName file name or relative path (e.g. {@code "config.yml"})
     * @return the detected format
     * @throws IllegalArgumentException for unsupported extensions
     */
    public static ConfigFormat detect(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".yml") || lower.endsWith(".yaml")) return YAML;
        throw new IllegalArgumentException(
                "[ConfigFileManager] Unsupported file format: '" + fileName +
                        "'. Supported extensions: .yml, .yaml");
    }
}
