package net.larsan.ai.api;

import java.util.Base64;

public enum Encoding {

    PLAINTEXT("plaintext", (s) -> s.getBytes()),
    BASE64("base64", (s) -> Base64.getDecoder().decode(s));

    public static Encoding parse(String s) {
        for (int i = 0; i < values().length; i++) {
            if (s.toLowerCase().equals(values()[i].encoding)) {
                return values()[i];
            }
        }
        throw new IllegalStateException("Mime type not supported: " + s);
    }

    private final String encoding;
    private final Exec exec;

    private Encoding(String name, Exec exec) {
        this.encoding = name;
        this.exec = exec;
    }

    public String getEncoding() {
        return encoding;
    }

    public byte[] toCleartext(String val) {
        return exec.toClearText(val);
    }

    @FunctionalInterface
    private static interface Exec {

        public byte[] toClearText(String s);

    }
}
