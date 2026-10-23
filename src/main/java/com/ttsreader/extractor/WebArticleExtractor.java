package com.ttsreader.extractor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebArticleExtractor implements TextExtractor<String> {

    @Override
    public String extractText(String url) throws Exception {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .timeout(10000)
                .get();

        // Removes noisy elements before extracting
        doc.select("nav, footer, header, script, style, aside, .ads").remove();

        Elements paragraphs = doc.select("h1, h2, h3, p");
        StringBuilder articleText = new StringBuilder();

        for (Element element : paragraphs) {
            String text = element.text().trim();
            if (!text.isEmpty()) {
                articleText.append(text).append("\n\n");
            }
        }

        return articleText.toString();
    }
}