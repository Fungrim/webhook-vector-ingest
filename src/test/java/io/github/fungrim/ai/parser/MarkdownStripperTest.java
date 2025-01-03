package io.github.fungrim.ai.parser;

import java.io.IOException;

import org.apache.tika.metadata.Metadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import dev.langchain4j.data.document.Document;
import io.github.fungrim.parser.MarkdownStripper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
public class MarkdownStripperTest {

    @Inject
    MarkdownStripper stripper;

    @Test
    public void shouldExtractHelloWorld() throws IOException {
        String md = DocumentLoader.loadString("docs/helloWorld.md");
        String text = stripper.strip(Document.document(md), new Metadata()).text();
        Assertions.assertEquals("Hello World!", text);
    }
}
