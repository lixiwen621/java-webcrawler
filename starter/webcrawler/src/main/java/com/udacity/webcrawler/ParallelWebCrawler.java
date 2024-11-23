package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A concrete implementation of {@link WebCrawler} that runs multiple threads on a
 * {@link ForkJoinPool} to fetch and process multiple web pages in parallel.
 *
 * 该类利用多线程并行爬取网页，特别是使用了 Java 的 ForkJoinPool 来并发处理多个网页
 */
final class ParallelWebCrawler implements WebCrawler {
  // 用于获取当前时间。这个 Clock 对象允许在测试时进行模拟时间操作（比如，使用 Clock.fixed）
  private final Clock clock;
  // 爬取操作的超时时间。它表示爬虫在特定时间内没有完成任务时，会停止爬取
  private final PageParserFactory parserFactory;
  private final Duration timeout;
  // 指定在爬取结果中应输出的最受欢迎词汇数量。爬虫统计网页上最常见的词并记录
  private final int popularWordCount;
  private final int maxDepth;
  private final List<Pattern> ignoredUrls;
  // 用于并行执行任务的线程池
  private final ForkJoinPool pool;

  @Inject
  ParallelWebCrawler(
          Clock clock,
          PageParserFactory parserFactory,
          @Timeout Duration timeout,
          @PopularWordCount int popularWordCount,
          @MaxDepth int maxDepth,
          @IgnoredUrls List<Pattern> ignoredUrls,
          @TargetParallelism int threadCount) {
    this.clock = clock;
    this.parserFactory = parserFactory;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.maxDepth = maxDepth;
    this.ignoredUrls = ignoredUrls;
    // 初始化 ForkJoinPool，线程数不超过机器可用核心数
    this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
  }

  /**
   *
   * @param startingUrls the starting points of the crawl.
   *                     表示爬虫的起始 URL。爬虫将从这些 URL 开始递归爬取网页内容
   * @return
   *
   * 该方法是爬虫的主要方法
   */
  @Override
  public CrawlResult crawl(List<String> startingUrls) {
    //Calculate the timeout time point of the crawler task.
    // If the current time exceeds this time point, the task will be aborted
    //计算爬虫任务的超时时间点。如果当前时间超过这个时间点，任务将中止
    Instant deadline = clock.instant().plus(timeout);
    // Concurrent data structures to store word counts and visited URLs.
    // 使用并发集合存储词汇统计结果
    Map<String, Integer> counts = new ConcurrentHashMap<>();
    Set<String> visitedUrls = new ConcurrentSkipListSet<>();

    // 创建并行任务，并用 ForkJoinPool 执行
    for (String url : startingUrls) {
      pool.invoke(new CrawlTask(url, deadline, maxDepth, counts, visitedUrls));
    }

    // Build and return the crawl result.
    if (counts.isEmpty()) {
      return new CrawlResult.Builder()
              .setWordCounts(counts)
              .setUrlsVisited(visitedUrls.size())
              .build();
    }

    return new CrawlResult.Builder()
            .setWordCounts(WordCounts.sort(counts, popularWordCount))
            .setUrlsVisited(visitedUrls.size())
            .build();
  }

  @Override
  public int getMaxParallelism() {
    // 获取当前机器上可用的 CPU 核心数量，作为爬虫的并行度上限
    return Runtime.getRuntime().availableProcessors();
  }

  // Inner class representing a recursive task to crawl a URL.
  private class CrawlTask extends RecursiveAction {
    private final String url;
    private final Instant deadline;
    private final int maxDepth;
    private final Map<String, Integer> counts;
    private final Set<String> visitedUrls;

    CrawlTask(String url, Instant deadline, int maxDepth, Map<String, Integer> counts, Set<String> visitedUrls) {
      this.url = url;
      this.deadline = deadline;
      this.maxDepth = maxDepth;
      this.counts = counts;
      this.visitedUrls = visitedUrls;
    }

    @Override
    protected void compute() {
      if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
        return;
      }
      // Check if the URL matches any ignored pattern.
      // 如果 url 已经存在于 visitedUrls 集合中，说明该 URL 已经被爬取过，为了避免重复爬取，需要返回
      // 如果 url 匹配到 ignoredUrls 列表中的任意正则表达式，则该 URL 会被忽略，不再爬取
      if (!visitedUrls.add(url)) {
        return; // 已经访问过
      }
      if (ignoredUrls.stream().anyMatch(pattern -> pattern.matcher(url).matches())) {
        return; // 匹配到忽略规则
      }

      // Parse the page.
      PageParser.Result result = parserFactory.get(url).parse();
      // 将解析结果中的单词和计数合并到共享的 counts 集合中，使用 merge() 方法累加出现次数
      result.getWordCounts().forEach((word, count) ->
              counts.merge(word, count, Integer::sum));

      // Create subtasks for each link and invoke them in parallel.
      // 对于解析出的页面链接，创建新的 CrawlTask 子任务，这些任务将递归地爬取链接
      List<CrawlTask> subtasks = result.getLinks().stream()
              .map(link -> new CrawlTask(link, deadline, maxDepth - 1, counts, visitedUrls))
              .collect(Collectors.toList());
      // 使用 invokeAll(subtasks) 并行执行所有子任务。invokeAll 会将任务提交给 ForkJoinPool 并等待它们完成，
      // 这样可以实现并行递归爬取
      invokeAll(subtasks);
    }
  }
}
