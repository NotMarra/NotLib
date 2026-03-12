package dev.notmarra.notlib.file;

/**
 * Per-file options controlling how a {@link ManagedConfig} is managed.
 *
 * <h3>Example:</h3>
 * <pre>{@code
 * ConfigOptions opts = ConfigOptions.builder()
 *     .autoUpdate(true)
 *     .preserveComments(true)
 *     .seedFile("languages/en_US.yml")
 *     .build();
 * cfm.watchDirectory("languages", opts);
 * }</pre>
 */
public class ConfigOptions {

    /**
     * Whether missing keys should be added from the resource template on load/reload.
     * Existing user values are always preserved.
     */
    private final boolean autoUpdate;

    /**
     * Whether YAML comments from the resource template are preserved in the output file
     * during an auto-update pass. Has no effect when {@code autoUpdate} is {@code false}.
     */
    private final boolean preserveComments;

    /**
     * Whether the file should be copied from plugin resources if it does not exist on disk.
     * For watched directories this doubles as the "seed file" path option.
     */
    private final boolean copyFromResources;

    /**
     * Whether added keys are logged at INFO level during auto-update.
     */
    private final boolean logAddedKeys;

    /**
     * Optional resource path of a seed file to copy into a watched directory on first run.
     * Gives users an example file to copy and customise.
     * {@code null} means no seed file.
     *
     * <p>Example: if the directory is {@code "languages"} and the seed is
     * {@code "languages/en_US.yml"}, that resource is copied once on startup.
     */
    private final String seedFile;

    private ConfigOptions(Builder builder) {
        this.autoUpdate        = builder.autoUpdate;
        this.preserveComments  = builder.preserveComments;
        this.copyFromResources = builder.copyFromResources;
        this.logAddedKeys      = builder.logAddedKeys;
        this.seedFile          = builder.seedFile;
    }

    // -----------------------------------------------------------------------
    // Factories
    // -----------------------------------------------------------------------

    /**
     * Default options: {@code autoUpdate=true}, {@code preserveComments=true},
     * {@code copyFromResources=true}, {@code logAddedKeys=true}, no seed file.
     */
    public static ConfigOptions defaults() {
        return builder().build();
    }

    /**
     * Options suitable for user-managed files: auto-update disabled, no resource copy.
     * Files are loaded as-is without any template merging.
     */
    public static ConfigOptions userManaged() {
        return builder().autoUpdate(false).copyFromResources(false).build();
    }

    /**
     * Options without auto-update; the file is only copied from resources if missing.
     */
    public static ConfigOptions noAutoUpdate() {
        return builder().autoUpdate(false).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    /** @see Builder#autoUpdate(boolean) */
    public boolean isAutoUpdate() { return autoUpdate; }

    /** @see Builder#preserveComments(boolean) */
    public boolean isPreserveComments() { return preserveComments; }

    /** @see Builder#copyFromResources(boolean) */
    public boolean isCopyFromResources() { return copyFromResources; }

    /** @see Builder#logAddedKeys(boolean) */
    public boolean isLogAddedKeys() { return logAddedKeys; }

    /** @see Builder#seedFile(String) */
    public String getSeedFile() { return seedFile; }

    // -----------------------------------------------------------------------
    // Builder
    // -----------------------------------------------------------------------

    public static class Builder {
        private boolean autoUpdate        = true;
        private boolean preserveComments  = true;
        private boolean copyFromResources = true;
        private boolean logAddedKeys      = true;
        private String  seedFile          = null;

        private Builder() {}

        /**
         * When {@code true} (default), missing keys are added from the resource template
         * on every load and reload while preserving the user's own values.
         */
        public Builder autoUpdate(boolean autoUpdate) {
            this.autoUpdate = autoUpdate;
            return this;
        }

        /**
         * When {@code true} (default), YAML comments from the resource template are
         * written to the output file during an auto-update pass.
         */
        public Builder preserveComments(boolean preserveComments) {
            this.preserveComments = preserveComments;
            return this;
        }

        /**
         * When {@code true} (default), the file is copied from plugin resources the first
         * time it is loaded if it does not yet exist on disk.
         */
        public Builder copyFromResources(boolean copyFromResources) {
            this.copyFromResources = copyFromResources;
            return this;
        }

        /**
         * When {@code true} (default), each key added during auto-update is logged at INFO level.
         */
        public Builder logAddedKeys(boolean logAddedKeys) {
            this.logAddedKeys = logAddedKeys;
            return this;
        }

        /**
         * Resource path of an example file to copy into a watched directory on first startup.
         * Has no effect on explicitly registered files.
         *
         * <p>Example: {@code .seedFile("languages/en_US.yml")} copies that resource
         * into the {@code languages/} directory so users have a template to start from.
         */
        public Builder seedFile(String resourcePath) {
            this.seedFile = resourcePath;
            return this;
        }

        public ConfigOptions build() {
            return new ConfigOptions(this);
        }
    }
}