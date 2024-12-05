package net.larsan.ai.docs;

import java.nio.charset.Charset;

import dev.langchain4j.data.document.Document;
import net.larsan.ai.api.Encoding;

public class TextDocumentParser implements DocumentParser {

    @Override
    public Document parse(String content, Encoding enc) {
        if (enc == Encoding.BASE64) {
            content = new String(enc.toCleartext(content), Charset.forName("UTF-8"));
        }
        return Document.document(content);
    }
}
