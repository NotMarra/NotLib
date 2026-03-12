package dev.notmarra.notlib.file.updater;

import dev.notmarra.notlib.file.ConfigOptions;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Performs a "smart update" of a YAML configuration file on disk:
 *
 * <ol>
 *   <li>Parses the resource template (keys, values, comments, block scalars).</li>
 *   <li>Parses the existing user file the same way.</li>
 *   <li>Builds the output: template structure + user values where they exist +
 *       template defaults for any missing keys.</li>
 *   <li>Comments and block scalars from the template are preserved in the output.</li>
 * </ol>
 *
 * <h3>Supported value types:</h3>
 * <ul>
 *   <li>Inline values:             {@code key: value}</li>
 *   <li>Literal block scalar:      {@code key: |} (newlines preserved)</li>
 *   <li>Folded block scalar:       {@code key: >} (newlines folded to spaces)</li>
 *   <li>Block scalar modifiers:    {@code |-}, {@code |+}, {@code >-}, {@code >+}</li>
 *   <li>Sequence blocks (lists):   {@code - item}</li>
 *   <li>Nested mappings (sections)</li>
 * </ul>
 *
 * <h3>Implementation note:</h3>
 * SnakeYAML discards all comments during serialisation, so this updater operates purely
 * at the text level without delegating serialisation to SnakeYAML.
 */
public class YamlConfigUpdater {

    private final JavaPlugin    plugin;
    private final String        fileName;
    private final File          diskFile;
    private final ConfigOptions options;

    public YamlConfigUpdater(JavaPlugin plugin, String fileName, File diskFile, ConfigOptions options) {
        this.plugin   = plugin;
        this.fileName = fileName;
        this.diskFile = diskFile;
        this.options  = options;
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Runs the update. Adds missing keys from the template, preserves user values and comments.
     *
     * @param resourceStream template stream from plugin resources (closed by the caller)
     */
    public void update(InputStream resourceStream) throws IOException {
        List<String> templateLines = readLines(resourceStream);
        List<String> diskLines     = readLines(new FileInputStream(diskFile));

        List<YamlEntry> templateEntries = parse(templateLines);
        List<YamlEntry> diskEntries     = parse(diskLines);

        // Index disk values by full dot-notation key for fast lookup
        Map<String, YamlEntry> diskIndex = new LinkedHashMap<>();
        for (YamlEntry e : diskEntries) {
            if (e.fullKey != null && e.hasValue()) diskIndex.put(e.fullKey, e);
        }

        // Collect missing keys for the log message
        List<String> missingKeys = new ArrayList<>();
        for (YamlEntry e : templateEntries) {
            if (e.fullKey != null && e.hasValue() && !diskIndex.containsKey(e.fullKey))
                missingKeys.add(e.fullKey);
        }

        if (missingKeys.isEmpty()) return;

        if (options.isLogAddedKeys()) {
            plugin.getLogger().info("[ConfigFileManager] Auto-update '" + fileName
                    + "': adding " + missingKeys.size() + " missing key(s): " + missingKeys);
        }

        writeLines(buildOutput(templateEntries, diskIndex));
    }

    // -----------------------------------------------------------------------
    // Parser  →  List<YamlEntry>
    // -----------------------------------------------------------------------

    /**
     * Parses a list of raw lines into structured {@link YamlEntry} blocks.
     *
     * <p>Each entry represents one logical YAML element: a blank line, a comment,
     * an inline key-value pair, a block scalar (key line + all content lines),
     * a section header (nested mapping), or a raw line (list item, etc.).
     */
    private List<YamlEntry> parse(List<String> lines) {
        List<YamlEntry> entries    = new ArrayList<>();
        Deque<String>   secStack   = new ArrayDeque<>();
        int prevKeyIndent = 0;
        int i = 0;

        while (i < lines.size()) {
            String line    = lines.get(i);
            String trimmed = line.trim();

            // Blank line
            if (trimmed.isEmpty()) {
                entries.add(YamlEntry.blank(line));
                i++;
                continue;
            }

            // Comment
            if (trimmed.startsWith("#")) {
                entries.add(YamlEntry.comment(line));
                i++;
                continue;
            }

            // Sequence item – not parsed as a key, belongs to the parent mapping
            if (trimmed.startsWith("- ") || trimmed.equals("-")) {
                entries.add(YamlEntry.raw(line));
                i++;
                continue;
            }

            // Key: value  or  section header
            if (trimmed.contains(":")) {
                int    indent = getIndent(line);
                String[] kv  = trimmed.split(":", 2);
                String  key  = kv[0].trim();
                String  rest = (kv.length > 1 ? kv[1] : "").trim();

                updateSectionStack(secStack, key, indent, prevKeyIndent);
                prevKeyIndent = indent;
                String fullKey = buildFullKey(secStack);

                // Block scalar indicator: | or > with optional modifier and/or inline comment
                if (rest.matches("[|>][\\-+]?(?:\\s*#.*)?")) {
                    List<String> blockLines = new ArrayList<>();
                    blockLines.add(line);

                    int j = i + 1;
                    while (j < lines.size()) {
                        String next = lines.get(j);
                        // Block content: blank lines or lines indented deeper than the key
                        if (next.trim().isEmpty() || getIndent(next) > indent) {
                            blockLines.add(next);
                            j++;
                        } else {
                            break;
                        }
                    }

                    // Strip trailing blank lines – they belong to the next key's separation
                    while (blockLines.size() > 1 && blockLines.get(blockLines.size() - 1).trim().isEmpty())
                        blockLines.remove(blockLines.size() - 1);

                    entries.add(YamlEntry.blockScalar(fullKey, key, indent, blockLines));
                    i = j;
                    continue;
                }

                // Section header (mapping with no inline value)
                if (stripInlineComment(rest).isEmpty()) {
                    entries.add(YamlEntry.section(fullKey, key, indent, line));
                    i++;
                    continue;
                }

                // Plain inline value
                entries.add(YamlEntry.inline(fullKey, key, indent, line));
                i++;
                continue;
            }

            // Fallthrough – unrecognised line (pass through unchanged)
            entries.add(YamlEntry.raw(line));
            i++;
        }

        return entries;
    }

    // -----------------------------------------------------------------------
    // Output builder
    // -----------------------------------------------------------------------

    /**
     * Walks the template entries and decides for each one what to emit:
     *
     * <ul>
     *   <li>BLANK / RAW → copy from template.</li>
     *   <li>COMMENT → copy if {@link ConfigOptions#isPreserveComments()}.</li>
     *   <li>SECTION → always copy (structural).</li>
     *   <li>INLINE – user has inline → substitute user value, keep template's inline comment.</li>
     *   <li>INLINE – user has block scalar → emit the user's whole block.</li>
     *   <li>INLINE – missing → emit from template.</li>
     *   <li>BLOCK_SCALAR – user has block → emit the user's whole block.</li>
     *   <li>BLOCK_SCALAR – user has inline → emit user's inline.</li>
     *   <li>BLOCK_SCALAR – missing → emit the whole template block.</li>
     * </ul>
     */
    private List<String> buildOutput(List<YamlEntry> template, Map<String, YamlEntry> diskIndex) {
        List<String> out = new ArrayList<>();

        for (YamlEntry te : template) {
            switch (te.type) {

                case BLANK:
                case RAW:
                    out.add(te.lines.get(0));
                    break;

                case COMMENT:
                    if (options.isPreserveComments()) out.add(te.lines.get(0));
                    break;

                case SECTION:
                    out.add(te.lines.get(0));
                    break;

                case INLINE: {
                    YamlEntry disk = diskIndex.get(te.fullKey);
                    if (disk == null) {
                        out.add(te.lines.get(0));                    // missing → template default
                    } else if (disk.type == EntryType.BLOCK_SCALAR) {
                        out.addAll(disk.lines);                      // user converted to block scalar
                    } else {
                        // User has an inline value → substitute it, keep template's inline comment
                        String userValue     = extractInlineValue(disk.lines.get(0));
                        String inlineComment = extractInlineComment(te.lines.get(0));
                        out.add(rebuildInlineLine(te.lines.get(0), te.key, userValue, inlineComment));
                    }
                    break;
                }

                case BLOCK_SCALAR: {
                    YamlEntry disk = diskIndex.get(te.fullKey);
                    if (disk == null) {
                        out.addAll(te.lines);                        // missing → whole template block
                    } else if (disk.type == EntryType.INLINE) {
                        out.add(disk.lines.get(0));                  // user converted to inline
                    } else {
                        out.addAll(disk.lines);                      // user's own block scalar
                    }
                    break;
                }
            }
        }

        return out;
    }

    // -----------------------------------------------------------------------
    // Line helpers
    // -----------------------------------------------------------------------

    /** Returns the value portion of an inline line (everything after the first {@code :}). */
    private String extractInlineValue(String line) {
        int colon = line.indexOf(':');
        return colon < 0 ? "" : line.substring(colon + 1).trim();
    }

    /**
     * Rebuilds an inline line preserving the original indentation and key,
     * substituting {@code userValue} and appending the template's inline comment.
     */
    private String rebuildInlineLine(String templateLine, String key, String userValue, String inlineComment) {
        String prefix      = " ".repeat(getIndent(templateLine));
        String commentPart = inlineComment.isEmpty() ? "" : "  " + inlineComment;
        return prefix + key + ": " + userValue + commentPart;
    }

    // -----------------------------------------------------------------------
    // Section-stack helpers
    // -----------------------------------------------------------------------

    private void updateSectionStack(Deque<String> stack, String key, int indent, int prevIndent) {
        if (stack.isEmpty()) { stack.push(key); return; }

        if (indent > prevIndent) {
            stack.push(key);                                         // descend into sub-mapping
        } else if (indent < prevIndent) {
            int step  = Math.max(1, prevIndent - indent);
            int steps = (prevIndent - indent) / step + 1;
            for (int i = 0; i < steps && !stack.isEmpty(); i++) stack.pop();
            stack.push(key);
        } else {
            stack.pop();
            stack.push(key);                                         // sibling key – replace top
        }
    }

    private String buildFullKey(Deque<String> stack) {
        List<String> parts = new ArrayList<>(stack);
        Collections.reverse(parts);
        return String.join(".", parts);
    }

    private int getIndent(String line) {
        int n = 0;
        for (char c : line.toCharArray()) { if (c == ' ') n++; else break; }
        return n;
    }

    // -----------------------------------------------------------------------
    // Comment helpers
    // -----------------------------------------------------------------------

    /** Returns the inline comment from a line (from the first {@code ' #'} outside quotes), or {@code ""}. */
    private String extractInlineComment(String line) {
        boolean inQ = false; char qc = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (!inQ && (c == '\'' || c == '"'))                         { inQ = true;  qc = c; }
            else if (inQ && c == qc)                                     { inQ = false; }
            else if (!inQ && c == '#' && i > 0 && line.charAt(i-1)==' '){ return line.substring(i); }
        }
        return "";
    }

    /** Strips the inline comment from a value string. */
    private String stripInlineComment(String value) {
        boolean inQ = false; char qc = 0;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!inQ && (c == '\'' || c == '"'))                         { inQ = true;  qc = c; }
            else if (inQ && c == qc)                                     { inQ = false; }
            else if (!inQ && c == '#' && i > 0 && value.charAt(i-1)==' '){ return value.substring(0, i).trim(); }
        }
        return value;
    }

    // -----------------------------------------------------------------------
    // I/O
    // -----------------------------------------------------------------------

    private List<String> readLines(InputStream stream) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) lines.add(line);
        }
        return lines;
    }

    private void writeLines(List<String> lines) throws IOException {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(diskFile), StandardCharsets.UTF_8)) {
            for (String line : lines) { w.write(line); w.write(System.lineSeparator()); }
        }
    }

    // -----------------------------------------------------------------------
    // Internal data types
    // -----------------------------------------------------------------------

    private enum EntryType { BLANK, COMMENT, SECTION, INLINE, BLOCK_SCALAR, RAW }

    /**
     * One logical element of a YAML file.
     *
     * <p>For {@code BLOCK_SCALAR}: {@code lines} holds the key line plus all content lines.
     * For all other types: {@code lines} holds exactly one line.
     */
    private static class YamlEntry {
        final EntryType    type;
        final String       fullKey;   // dot-notation; null for BLANK / COMMENT / RAW
        final String       key;       // bare key without section prefix
        final int          indent;
        final List<String> lines;     // BLOCK_SCALAR: whole block; others: single line

        private YamlEntry(EntryType type, String fullKey, String key, int indent, List<String> lines) {
            this.type    = type;
            this.fullKey = fullKey;
            this.key     = key;
            this.indent  = indent;
            this.lines   = Collections.unmodifiableList(lines);
        }

        static YamlEntry blank(String line)   { return new YamlEntry(EntryType.BLANK,   null, null, 0, List.of(line)); }
        static YamlEntry comment(String line) { return new YamlEntry(EntryType.COMMENT, null, null, 0, List.of(line)); }
        static YamlEntry raw(String line)     { return new YamlEntry(EntryType.RAW,     null, null, 0, List.of(line)); }

        static YamlEntry section(String fk, String key, int indent, String line) {
            return new YamlEntry(EntryType.SECTION, fk, key, indent, List.of(line));
        }
        static YamlEntry inline(String fk, String key, int indent, String line) {
            return new YamlEntry(EntryType.INLINE, fk, key, indent, List.of(line));
        }
        static YamlEntry blockScalar(String fk, String key, int indent, List<String> blockLines) {
            return new YamlEntry(EntryType.BLOCK_SCALAR, fk, key, indent, new ArrayList<>(blockLines));
        }

        /** {@code true} if this entry carries a real value (inline or block scalar). */
        boolean hasValue() {
            return type == EntryType.INLINE || type == EntryType.BLOCK_SCALAR;
        }
    }
}