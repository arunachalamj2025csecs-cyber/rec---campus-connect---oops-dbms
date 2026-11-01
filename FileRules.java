package edu.rec.campusconnect.rules;

import java.util.List;

/**
 * Pure rules for uploaded images (event posters and profile photos).
 */
public final class FileRules {

    public static final List<String> IMAGE_TYPES = List.of("image/png", "image/jpeg", "image/webp");

    private FileRules() {
    }

    /** Maps "image/jpg" to "image/jpeg". Returns null when the type is not an allowed image type. */
    public static String normalizeImageType(String contentType) {
        String type = contentType == null ? "" : contentType.trim().toLowerCase(java.util.Locale.ROOT);
        if ("image/jpg".equals(type)) {
            type = "image/jpeg";
        }
        return IMAGE_TYPES.contains(type) ? type : null;
    }

    /**
     * Checks the first bytes of the file (its "magic number") so that, for example,
     * an HTML file renamed to .png is rejected. Returns true when the bytes match the type.
     */
    public static boolean matchesSignature(String type, byte[] data) {
        if (data == null) {
            return false;
        }
        switch (type) {
            case "image/png":
                return data.length >= 4 && (data[0] & 0xFF) == 0x89
                        && data[1] == 'P' && data[2] == 'N' && data[3] == 'G';
            case "image/jpeg":
                return data.length >= 3 && (data[0] & 0xFF) == 0xFF
                        && (data[1] & 0xFF) == 0xD8 && (data[2] & 0xFF) == 0xFF;
            case "image/webp":
                return data.length >= 12 && data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F'
                        && data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P';
            default:
                return false;
        }
    }
}
