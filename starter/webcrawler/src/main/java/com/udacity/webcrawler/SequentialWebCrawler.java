package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A {@link WebCrawler} that downloads and processes one page at a time.
 *
 * 是一个顺序执行的网页爬虫，它在爬取网页时不使用并行处理，而是逐个页面进行处理，直到达到指定的最大爬取深度或超时
 */
final class SequentialWebCrawler implements WebCrawler {
  // 用于获取当前时间。这个类允许进行时间相关操作，如计算超时
  private final Clock clock;
  // 用于创建 PageParser 的工厂类对象。每个 URL 都会通过 PageParserFactory 创建一个 PageParser 实例，解析页面内容
  private final PageParserFactory parserFactory;
  // 用于限制爬取网页的最大持续时间。如果超过这个时间，爬虫将停止继续爬取
  private final Duration timeout;
  // 爬虫返回结果时，要输出的最流行词汇的数量
  private final int popularWordCount;
  private final int maxDepth;
  private final List<Pattern> ignoredUrls;

  @Inject
  SequentialWebCrawler(
      Clock clock,
      PageParserFactory parserFactory,
      @Timeout Duration timeout,
      @PopularWordCount int popularWordCount,
      @MaxDepth int maxDepth,
      @IgnoredUrls List<Pattern> ignoredUrls) {
    this.clock = clock;
    this.parserFactory = parserFactory;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.maxDepth = maxDepth;
    this.ignoredUrls = ignoredUrls;
  }

  /**
   * 开始爬取网页的方法
   * @param startingUrls the starting points of the crawl.
   *                     表示爬虫的起始 URL。爬虫将从这些 URL 开始递归爬取网页内容
   * @return
   */
  @Override
  public CrawlResult crawl(List<String> startingUrls) {
    //计算爬虫任务的超时时间点。如果当前时间超过这个时间点，任务将中止
    Instant deadline = clock.instant().plus(timeout);
    // 用于存储爬取过程中统计到的单词及其出现次数
    Map<String, Integer> counts = new HashMap<>();
    // 存储已经访问过的 URL，避免重复爬取相同的网页
    Set<String> visitedUrls = new HashSet<>();
    // 遍历 startingUrls 列表，对每个 URL 调用 crawlInternal 方法进行递归爬取
    for (String url : startingUrls) {
      crawlInternal(url, deadline, maxDepth, counts, visitedUrls);
    }

    // 如果没有找到任何词汇，返回一个包含空 counts 和访问的 URL 数量的 CrawlResult
    if (counts.isEmpty()) {
      return new CrawlResult.Builder()
          .setWordCounts(counts)
          .setUrlsVisited(visitedUrls.size())
          .build();
    }

    // 如果统计到了词汇，调用 WordCounts.sort(counts, popularWordCount) 对词汇按流行度排序，
    // 并返回包含排序后的词汇表和访问 URL 数量的 CrawlResult
    return new CrawlResult.Builder()
        .setWordCounts(WordCounts.sort(counts, popularWordCount))
        .setUrlsVisited(visitedUrls.size())
        .build();
  }

  /**
   *  是实际执行爬取的递归方法。它通过递归方式深度爬取网页，直到达到最大深度或超时时间
   * @param url
   * @param deadline
   * @param maxDepth
   * @param counts
   * @param visitedUrls
   */
  private void crawlInternal(
      String url,
      Instant deadline,
      int maxDepth,
      Map<String, Integer> counts,
      Set<String> visitedUrls) {
    // 如果 maxDepth == 0，即已经达到最大爬取深度，终止递归, 或者当前时间超过了超时时间
    if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
      return;
    }
    // 遍历 ignoredUrls, 检查当前 url 是否匹配任意一个被忽略的正则表达式，如果匹配则不爬取该 URL
    for (Pattern pattern : ignoredUrls) {
      if (pattern.matcher(url).matches()) {
        return;
      }
    }
    // 检查重复访问：如果 url 已经在 visitedUrls 集合中，说明该 URL 已经爬取过，避免重复爬取
    if (visitedUrls.contains(url)) {
      return;
    }
    visitedUrls.add(url);
    // 解析页面内容。返回的 result 包含该页面的词汇统计（getWordCounts()）和页面中的其他链接（getLinks()）
    PageParser.Result result = parserFactory.get(url).parse();
    // 将当前页面的词汇统计结果合并到 counts 中
    for (Map.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
      counts.compute(e.getKey(), (key, value) -> (value == null) ? e.getValue() : e.getValue() + value);
    }
    // 对解析出的每个链接，递归调用 crawlInternal，继续爬取，深度递减 1
    for (String link : result.getLinks()) {
      crawlInternal(link, deadline, maxDepth - 1, counts, visitedUrls);
    }
  }
}
