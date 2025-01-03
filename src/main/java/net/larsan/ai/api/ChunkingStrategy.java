package net.larsan.ai.api;

import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentByCharacterSplitter;
import dev.langchain4j.data.document.splitter.DocumentByLineSplitter;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.data.document.splitter.DocumentByWordSplitter;

public enum ChunkingStrategy {

    PARAGRAPH((max, maxOverlap) -> new DocumentByParagraphSplitter(max, maxOverlap)),
    LINE((max, maxOverlap) -> new DocumentByLineSplitter(max, maxOverlap)),
    SENTENCE((max, maxOverlap) -> new DocumentBySentenceSplitter(max, maxOverlap)),
    WORD((max, maxOverlap) -> new DocumentByWordSplitter(max, maxOverlap)),
    CHARCTER((max, maxOverlap) -> new DocumentByCharacterSplitter(max, maxOverlap));

    private final Factory supplier;

    private ChunkingStrategy(Factory supplier) {
        this.supplier = supplier;
    }

    public DocumentSplitter create(int max, int maxOverlap) {
        return supplier.create(max, maxOverlap);
    }

    @FunctionalInterface
    private static interface Factory {

        DocumentSplitter create(int max, int maxOverlap);
    }
}
