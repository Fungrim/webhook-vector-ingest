package net.larsan.ai.api;

public enum MimeType {

    TEXT("text/plain"),
    // HTML("text/html"),
    // PDF("application/pdf"),
    // MARKDOWN("application/json")
    ;

    public static MimeType parse(String s) {
        for (int i = 0; i < values().length; i++) {
            if (s.toLowerCase().startsWith(values()[i].mimeType)) {
                return values()[i];
            }
        }
        throw new IllegalStateException("Mime type not supported: " + s);
    }

    private String mimeType;

    private MimeType(String name) {
        this.mimeType = name;
    }

    public String getMimeType() {
        return mimeType;
    }
}
