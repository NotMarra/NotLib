package dev.notmarra.notlib.chat;

import java.util.regex.Pattern;

public class LegacyPatterns {
    private LegacyPatterns() {}

    public static final Pattern LEGACY_HEX =
            Pattern.compile("&x(&[0-9a-f]){6}", Pattern.CASE_INSENSITIVE);

    public static final Pattern LEGACY_HEX_SHORT =
            Pattern.compile("&#([0-9a-f]{6})", Pattern.CASE_INSENSITIVE);

    public static final Pattern LEGACY_GRADIENT =
            Pattern.compile("&@#([0-9a-f]{6})-([^-]+)-([^&]+)&", Pattern.CASE_INSENSITIVE);
}
