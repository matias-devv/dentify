package com.dentify.utils;
import java.util.regex.Pattern;

/**
 * Sanitizes user-supplied filenames before they're used in a storage key or
 * persisted, to avoid path traversal and invalid S3/B2 keys (NFR sección 8).
 * <p>
 * Shared by {@code ComplementaryExamService} (step 5/6 — building the object
 * key) and {@code ComplementaryExamMapper} (persisted {@code filename}), so
 * both end up with the exact same sanitized value for a given upload.
 * </p>
 */
public final class FilenameSanitizer {

    private static final Pattern UNSAFE_CHARS = Pattern.compile("[\\\\/\\p{Cntrl}]");
    private static final Pattern REPEATED_WHITESPACE = Pattern.compile("\\s+");

    private FilenameSanitizer() {}

    public static String sanitize(String originalFilename) {

        if ( originalFilename == null || originalFilename.isBlank() ) {
            return "file";
        }

        String withoutPathSeparatorsOrControlChars = UNSAFE_CHARS.matcher(originalFilename)
                                                                 .replaceAll("");

        String withoutRepeatedWhitespace = REPEATED_WHITESPACE.matcher( withoutPathSeparatorsOrControlChars )
                                                              .replaceAll("_");

        return withoutRepeatedWhitespace.isBlank() ? "file" : withoutRepeatedWhitespace;
    }
}