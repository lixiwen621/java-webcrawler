package com.udacity.webcrawler.json;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CrawlerConfigDeserializer extends JsonDeserializer<CrawlerConfiguration> {
    @Override
    public CrawlerConfiguration deserialize(JsonParser p, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode node = p.getCodec().readTree(p);
        // 使用流式处理，简化解析
        List<String> startPages = convertJsonNode(node.get("startPages"), JsonNode::asText);
        List<Pattern> ignoredUrls = convertJsonNode(node.get("ignoredUrls"), url -> Pattern.compile(url.asText()));
        List<Pattern> ignoredWords = convertJsonNode(node.get("ignoredWords"), word -> Pattern.compile(word.asText()));

        String implementationOverride = getAsTextOrEmpty(node, "implementationOverride");
        int maxDepth = getAsIntOrDefault(node, "maxDepth", 0);
        int timeoutSeconds = getAsIntOrDefault(node, "timeoutSeconds", 0);
        int popularWordCount = getAsIntOrDefault(node, "popularWordCount", 0);
        int parallelism = getAsIntOrDefault(node, "parallelism", -1);
        String resultPath = getAsTextOrEmpty(node, "resultPath");
        String profileOutputPath = getAsTextOrEmpty(node, "profileOutputPath");
        // 创建 CrawlerConfiguration 对象
        return new CrawlerConfiguration.Builder()
                .addStartPages(startPages.toArray(new String[0]))
                .addIgnoredUrls(ignoredUrls.stream().map(Pattern::pattern).toArray(String[]::new))
                .addIgnoredWords(ignoredWords.stream().map(Pattern::pattern).toArray(String[]::new))
                .setImplementationOverride(implementationOverride)
                .setMaxDepth(maxDepth)
                .setTimeoutSeconds(timeoutSeconds)
                .setPopularWordCount(popularWordCount)
                .setParallelism(parallelism)
                .setResultPath(resultPath)
                .setProfileOutputPath(profileOutputPath)
                .build();
    }

    // 将 JsonNode 转换为泛型列表，使用流式处理
    private <T> List<T> convertJsonNode(JsonNode node, java.util.function.Function<JsonNode, T> mapper) {
        return node != null && node.isArray()
                ? StreamSupport.stream(node.spliterator(), false).map(mapper).collect(Collectors.toList())
                : List.of();  // 返回空列表
    }

    // 获取文本字段，若为空返回空字符串
    private String getAsTextOrEmpty(JsonNode node, String field) {
        String string = node.hasNonNull(field) ? node.get(field).asText() : "";
        return string;
    }

    // 获取 int 字段，若为空返回默认值
    private int getAsIntOrDefault(JsonNode node, String field, int defaultValue) {
        return node.hasNonNull(field) ? node.get(field).asInt() : defaultValue;
    }
}
