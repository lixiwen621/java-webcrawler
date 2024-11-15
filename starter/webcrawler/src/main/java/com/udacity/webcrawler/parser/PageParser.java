package com.udacity.webcrawler.parser;

import com.udacity.webcrawler.profiler.Profiled;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Parses and processes remote and local HTML pages to return a parse {@link Result}.
 * 接口 PageParser, 它的内部静态类 Result 和 Result.Builder。
 * 这个接口的功能是解析 HTML 页面并返回解析结果，Result 则是保存解析结果的数据类
 */
public interface PageParser {

  /**
   * Processes the HTML page and returns a {@link Result} for the page.
   * 这个方法的作用是解析 HTML 页面，并返回一个 Result 对象，该对象包含解析的结果，如词频统计和网页中的链接
   *
   * @Profiled 注解可能表示这个方法在执行时会被某个性能分析工具记录。
   * 也就是说，当 parse() 方法被调用时，可能会记录下执行的时间、调用次数等信息。这个注解是专门为性能分析设计的。
   */
  @Profiled
  Result parse();

  /**
   * A data class that represents the outcome of processing an HTML page.
   * Result 是一个静态内部类，代表解析结果的数据结构。它是一个不可变的类，用于存储解析后的结果
   */
  final class Result {
    /**
     * 用于存储词语及其在网页中的出现次数
     */
    private final Map<String, Integer> wordCounts;
    /**
     *  存储解析到的网页中的超链接（链接地址）
     */
    private final List<String> links;

    // 构造函数私有化，确保只能通过 Result.Builder 来创建 Result 实例
    private Result(Map<String, Integer> wordCounts, List<String> links) {
      this.wordCounts = Objects.requireNonNull(wordCounts);
      this.links = Objects.requireNonNull(links);
    }

    /**
     * Returns an unmodifiable {@link Map} containing the words and word frequencies encountered
     * when parsing the web page.
     * 返回不可修改的词频统计 Map
     */
    public Map<String, Integer> getWordCounts() {
      return wordCounts;
    }

    /**
     * Returns an unmodifiable {@link List} of the hyperlinks encountered when parsing the web page.
     * 返回不可修改的超链接 List
     */
    public List<String> getLinks() {
      return links;
    }

    /**
     * A builder class for the parse {@link Result}. This builder keeps track of word counts and
     * hyperlinks encountered while parsing a web page.
     */
    static final class Builder {
      // 用于存储解析时遇到的词语及其出现次数
      private final Map<String, Integer> wordCounts = new HashMap<>();
      //用于存储网页中的超链接，使用 Set 来避免重复链接
      private final Set<String> links = new HashSet<>();

      /**
       * Increments the frequency counter for the given word.
       * 这个方法用于增加某个词的出现次数。它使用了 compute 方法，如果该词第一次出现，将值设为 1；如果已经存在，则值加 1
       */
      void addWord(String word) {
        Objects.requireNonNull(word);
        wordCounts.compute(word, (k, v) -> (v == null) ? 1 : v + 1);
      }

      /**
       * Adds the given link, if it has not already been added.
       * 用于添加网页中遇到的超链接。使用 Set 来确保每个链接只会添加一次
       */
      void addLink(String link) {
        links.add(Objects.requireNonNull(link));
      }

      /**
       * Constructs a {@link Result} from this builder.
       */
      Result build() {
        return new Result(
            Collections.unmodifiableMap(wordCounts),
            links.stream().collect(Collectors.toUnmodifiableList()));
      }
    }
  }
}
