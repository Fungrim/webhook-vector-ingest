package net.larsan.ai.parser;

import org.apache.tika.metadata.Metadata;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;

import dev.langchain4j.data.document.Document;
import jakarta.inject.Singleton;

@Singleton
public class MarkdownStripper {

    public Document strip(Document d, Metadata metadata) {
        Node node = Parser.builder().build().parse(d.text());
        String text = TextContentRenderer.builder().build().render(node);
        return new Document(text.trim());
    }
}
