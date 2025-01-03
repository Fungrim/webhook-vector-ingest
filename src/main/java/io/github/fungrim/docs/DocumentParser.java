package io.github.fungrim.docs;

import dev.langchain4j.data.document.Document;
import io.github.fungrim.api.Encoding;

public interface DocumentParser {

    public Document parse(String content, Encoding enc);

}
