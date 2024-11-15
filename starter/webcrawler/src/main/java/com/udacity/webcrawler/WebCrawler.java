package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.profiler.Profiled;

import java.util.List;

/**
 * The main interface that defines the web crawler API.
 * 该接口代表网络爬虫的主要 API。通过这个接口，可以实现启动网络爬虫、获取爬取结果，以及获取爬虫支持的最大并行度等功能
 *
 */
public interface WebCrawler {

  /**
   * Starts a crawl at the given URLs.
   *
   * @param startingUrls the starting points of the crawl.
   *                     表示爬虫的起始 URL。爬虫将从这些 URL 开始递归爬取网页内容
   * @return the {@link CrawlResult} of the crawl.
   *                这个返回值应该包含爬虫运行过程中获取的相关信息，比如爬取到的网页内容或统计结果等
   *
   * 这是 WebCrawler 的主要方法，用于启动网络爬虫
   *
   */
  @Profiled
  CrawlResult crawl(List<String> startingUrls);

  /**
   * Returns the maximum amount of parallelism (number of CPU cores) supported by this web crawler.
   *
   * 这是一个默认方法，返回爬虫支持的最大并行度，也就是同时能够执行的最大线程数或 CPU 核心数
   */
  default int getMaxParallelism() {
    return 1;
  }
}
