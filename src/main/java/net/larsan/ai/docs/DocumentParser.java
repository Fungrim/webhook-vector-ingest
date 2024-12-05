package net.larsan.ai.docs;

import dev.langchain4j.data.document.Document;
import net.larsan.ai.api.Encoding;

public interface DocumentParser {

    public Document parse(String content, Encoding enc);

}
