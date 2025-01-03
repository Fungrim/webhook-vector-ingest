package net.larsan.ai.parser;

import java.io.IOException;

import org.apache.tika.metadata.Metadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import dev.langchain4j.data.document.Document;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
public class HtmlStripperTest {

    @Inject
    HtmlStripper stripper;

    @Test
    public void shouldExtractHelloWorld() throws IOException {
        String html = DocumentLoader.loadString("docs/helloWorld.html");
        String text = stripper.strip(Document.document(html), new Metadata()).text();
        Assertions.assertEquals("The world is awesome!", text);
    }

    @Test
    public void shouldIncludeTitle() throws IOException {
        Metadata meta = new Metadata();
        String html = DocumentLoader.loadString("docs/helloWorld.html");
        String text = stripper.strip(Document.document(html), meta).text();
        Assertions.assertEquals("The world is awesome!", text);
        Assertions.assertEquals("Hello World", meta.get("title"));
    }
}
