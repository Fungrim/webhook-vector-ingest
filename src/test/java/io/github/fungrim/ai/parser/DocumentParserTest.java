package io.github.fungrim.ai.parser;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

import org.apache.tika.metadata.Metadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.fungrim.api.Encoding;
import io.github.fungrim.api.UpsertRequest;
import io.github.fungrim.api.UpsertRequest.Data;
import io.github.fungrim.parser.DocumentParser;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
public class DocumentParserTest {

    @Inject
    DocumentParser parser;

    @Test
    public void shouldExtractHelloWorldPdf() throws IOException {
        byte[] bytes = DocumentLoader.load("docs/helloWorld.pdf");
        String text = parser.parse(toReq(bytes), new Metadata()).text();
        Assertions.assertEquals("Hello World!", text);
    }

    @Test
    public void shouldPopulateMetadataWithContentType() throws IOException {
        Metadata meta = new Metadata();
        byte[] bytes = DocumentLoader.load("docs/helloWorld.pdf");
        parser.parse(toReq(bytes), meta).text();
        Assertions.assertEquals("application/pdf", meta.get(Metadata.CONTENT_TYPE));
    }

    @Test
    public void shouldExtractHelloWorldPdfWithContentType() throws IOException {
        Metadata meta = new Metadata();
        meta.set(Metadata.CONTENT_TYPE, "application/pdf");
        byte[] bytes = DocumentLoader.load("docs/helloWorld.pdf");
        String text = parser.parse(toReq(bytes), meta).text();
        Assertions.assertEquals("Hello World!", text);
    }

    @Test
    public void shouldExtractHelloWorldDocx() throws IOException {
        byte[] bytes = DocumentLoader.load("docs/helloWorld.docx");
        String text = parser.parse(toReq(bytes), new Metadata()).text();
        Assertions.assertEquals("Hello World!", text);
    }

    @Test
    public void shouldExtractHelloWorldDocxWithContentType() throws IOException {
        Metadata meta = new Metadata();
        meta.set(Metadata.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        byte[] bytes = DocumentLoader.load("docs/helloWorld.docx");
        String text = parser.parse(toReq(bytes), meta).text();
        Assertions.assertEquals("Hello World!", text);
    }

    @Test
    public void shouldExtractHelloWorldOdt() throws IOException {
        byte[] bytes = DocumentLoader.load("docs/helloWorld.odt");
        String text = parser.parse(toReq(bytes), new Metadata()).text();
        Assertions.assertEquals("Hello World!", text);
    }

    @Test
    public void shouldExtractHelloWorldOdtWithContentType() throws IOException {
        Metadata meta = new Metadata();
        meta.set(Metadata.CONTENT_TYPE, "application/vnd.oasis.opendocument.text");
        byte[] bytes = DocumentLoader.load("docs/helloWorld.odt");
        String text = parser.parse(toReq(bytes), meta).text();
        Assertions.assertEquals("Hello World!", text);
    }

    private UpsertRequest toReq(byte[] bytes) {
        return new UpsertRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                new Data(
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Base64.getEncoder().encodeToString(bytes),
                        Optional.empty(),
                        Optional.of(Encoding.BASE64)));
    }
}
