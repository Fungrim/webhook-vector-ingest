package net.larsan.ai.parser;

import org.apache.tika.metadata.Metadata;
import org.jboss.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import dev.langchain4j.data.document.Document;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.larsan.ai.conf.MetadataConfig;

@Singleton
public class HtmlStripper {

    @Inject
    MetadataConfig metadataConfig;

    @Inject
    Logger log;

    public Document strip(Document d, Metadata parseMetadata) {
        org.jsoup.nodes.Document html = Jsoup.parse(d.text());
        if (metadataConfig.html().includeHeaders().isPresent()) {
            metadataConfig.html().includeHeaders().get().stream().forEach(tag -> {
                Elements elements = html.head().getElementsByTag(tag);
                if (elements.size() > 1) {
                    log.debugf("Found multiple elements for tag %s, will only use the first", tag);
                }
                if (elements.size() > 0) {
                    parseMetadata.set(tag, elements.get(0).text());
                }
            });
        }
        return new Document(html.body().text().trim());
    }
}
