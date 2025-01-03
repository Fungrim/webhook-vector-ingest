package io.github.fungrim.ai.parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DocumentLoader {

    public static byte[] load(String path) throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream in = cl.getResourceAsStream(path);
        if (in == null) {
            throw new IllegalArgumentException("Resource not found: " + path);
        }
        ByteArrayOutputStream baot = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) != -1) {
            baot.write(buf, 0, len);
        }
        return baot.toByteArray();
    }

    public static String loadString(String path) throws IOException {
        return new String(load(path));
    }
}
