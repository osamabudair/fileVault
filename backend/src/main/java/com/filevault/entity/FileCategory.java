package com.filevault.entity;

import java.util.Locale;
import java.util.Set;

public enum FileCategory {
    DOCUMENTS,
    MEDIA,
    ARCHIVES,
    CODE,
    OTHER;

    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of(
        "pdf", "doc", "docx", "txt", "rtf", "odt", "csv", "xls", "xlsx", "ppt", "pptx", "md"
    );

    private static final Set<String> MEDIA_EXTENSIONS = Set.of(
        "png", "jpg", "jpeg", "webp", "svg", "gif", "bmp", "ico", "mp4", "mov", "avi", "mkv", "webm", "mp3", "wav", "ogg", "flac"
    );

    private static final Set<String> ARCHIVE_EXTENSIONS = Set.of(
        "zip", "tar", "gz", "tgz", "rar", "7z", "bz2", "xz"
    );

    private static final Set<String> CODE_EXTENSIONS = Set.of(
        "js", "jsx", "ts", "tsx", "json", "yaml", "yml", "sql", "html", "css", "scss",
        "py", "java", "go", "rs", "sh", "c", "cpp", "h", "hpp", "cs", "php", "rb", "kt", "swift", "xml", "toml"
    );

    public static FileCategory detect(String filename, String contentType) {
        String ext = extractExtension(filename);
        String mime = contentType != null ? contentType.toLowerCase(Locale.ROOT) : "";

        if (CODE_EXTENSIONS.contains(ext)) {
            return CODE;
        }
        if (ARCHIVE_EXTENSIONS.contains(ext) || mime.contains("zip") || mime.contains("tar")
            || mime.contains("rar") || mime.contains("7z") || mime.contains("compressed")) {
            return ARCHIVES;
        }
        if (MEDIA_EXTENSIONS.contains(ext) || mime.startsWith("image/")
            || mime.startsWith("video/") || mime.startsWith("audio/")) {
            return MEDIA;
        }
        if (DOCUMENT_EXTENSIONS.contains(ext) || mime.contains("pdf")
            || mime.contains("document") || mime.contains("msword")
            || mime.contains("spreadsheet") || mime.contains("presentation")
            || mime.startsWith("text/")) {
            return DOCUMENTS;
        }
        return OTHER;
    }

    public static FileCategory fromQueryParam(String value) {
        if (value == null || value.isBlank() || "all".equalsIgnoreCase(value.trim())) {
            return null;
        }
        try {
            return FileCategory.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid file category: " + value);
        }
    }

    private static String extractExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }
}
