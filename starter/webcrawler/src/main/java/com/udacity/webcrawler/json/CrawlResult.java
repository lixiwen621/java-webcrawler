package com.udacity.webcrawler.json;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Data class representing the final result of a web crawl.
 * CrawlResult是一个不可变的类 它用于表示一次网页爬取操作的最终结果
 */
public final class CrawlResult {

  // 一个包含单词和对应出现次数的 Map，用于记录网页爬取过程中遇到的词频
  private final Map<String, Integer> wordCounts;
  // 一个整数，表示爬取期间访问的唯一 URL 的数量
  private final int urlsVisited;

  /**
   * Constructs a {@link CrawlResult} with the given word counts and visited URL count.
   */
  private CrawlResult(Map<String, Integer> wordCounts, int urlsVisited) {
    this.wordCounts = wordCounts;
    this.urlsVisited = urlsVisited;
  }

  /**
   * Returns an unmodifiable {@link Map}. Each key in the map is a word that was encountered
   * during the web crawl. Each value is the total number of times a word was seen.
   *
   * <p>When computing these counts for a given crawl, results from the same page are never
   * counted twice.
   *
   * <p>The size of the returned map is the same as the {@code "popularWordCount"} option in the
   * crawler configuration. For example,  if {@code "popularWordCount"} is 3, only the top 3 most
   * frequent words are returned.
   *
   * <p>If multiple words have the same frequency, prefer longer words rank higher. If multiple
   * words have the same frequency and length, use alphabetical order to break ties (the word that
   * comes first in the alphabet ranks higher).
   *
   *  返回一个不可修改的 Map，其中每个键代表在网页爬取过程中遇到的单词，值代表该单词出现的次数。
   *  该方法确保返回的数据是不可变的，防止外部修改结果
   *
   */
  public Map<String, Integer> getWordCounts() {
    return wordCounts;
  }

  /**
   * Returns the number of distinct URLs the web crawler visited.
   *
   * <p>A URL is considered "visited" if the web crawler attempted to crawl that URL, even if the
   * HTTP request to download the page returned an error.
   *
   * <p>When computing this value for a given crawl, the same URL is never counted twice.
   *
   * 返回访问过的唯一 URL 数量
   */
  public int getUrlsVisited() {
    return urlsVisited;
  }

  /**
   * A package-private builder class for constructing web crawl {@link CrawlResult}s.
   */
  public static final class Builder {
    private Map<String, Integer> wordFrequencies = new HashMap<>();
    private int pageCount;

    /**
     * Sets the word counts. See {@link #getWordCounts()}
     */
    public Builder setWordCounts(Map<String, Integer> wordCounts) {
      this.wordFrequencies = Objects.requireNonNull(wordCounts);
      return this;
    }

    /**
     * Sets the total number of URLs visited. See {@link #getUrlsVisited()}.
     */
    public Builder setUrlsVisited(int pageCount) {
      this.pageCount = pageCount;
      return this;
    }

    /**
     * Constructs a {@link CrawlResult} from this builder.
     * Collections.unmodifiableMap方法可以将Map包装为不可修改的Map
     */
    public CrawlResult build() {
      return new CrawlResult(Collections.unmodifiableMap(wordFrequencies), pageCount);
    }
  }
}