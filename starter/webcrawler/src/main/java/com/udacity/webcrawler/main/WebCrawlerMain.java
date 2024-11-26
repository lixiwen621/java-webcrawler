package com.udacity.webcrawler.main;

import com.google.inject.Guice;
import com.udacity.webcrawler.WebCrawler;
import com.udacity.webcrawler.WebCrawlerModule;
import com.udacity.webcrawler.json.ConfigurationLoader;
import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.json.CrawlResultWriter;
import com.udacity.webcrawler.json.CrawlerConfiguration;
import com.udacity.webcrawler.profiler.Profiler;
import com.udacity.webcrawler.profiler.ProfilerModule;

import javax.inject.Inject;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Objects;

public final class WebCrawlerMain {

  private final CrawlerConfiguration config;

  private WebCrawlerMain(CrawlerConfiguration config) {
    this.config = Objects.requireNonNull(config);
  }

  @Inject
  private WebCrawler crawler;

  @Inject
  private Profiler profiler;

  private void run() throws Exception {
    Guice.createInjector(new WebCrawlerModule(config), new ProfilerModule()).injectMembers(this);
    CrawlResult result = crawler.crawl(config.getStartPages());
    CrawlResultWriter resultWriter = new CrawlResultWriter(result);
    if (config.getResultPath().isEmpty()) {
      // Use StringWriter to write data to the cache stream. Avoid using System.out
      // 使用 StringWriter 把数据写入到缓存流中 避免使用 System.out
      StringWriter stringWriter = new StringWriter(); // 写入到缓冲流
      resultWriter.write(stringWriter); // 写入数据stringWriter缓冲流
      System.out.println(stringWriter); // 输出缓冲内容到控制台
    } else {
      resultWriter.write(Path.of(config.getResultPath()));
    }
    if (config.getProfileOutputPath().isEmpty()) {
      // Use StringWriter to write data to the cache stream. Avoid using System.out
      // 使用 StringWriter 把数据写入到缓存流中 避免使用 System.out
      StringWriter stringWriter = new StringWriter();
      profiler.writeData(stringWriter);
      System.out.println(stringWriter); // 输出缓冲内容到控制台
    } else {
      profiler.writeData(Path.of(config.getProfileOutputPath()));
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: WebCrawlerMain [starting-url]");
      return;
    }
    CrawlerConfiguration config = new ConfigurationLoader(Path.of(args[0])).load();
    new WebCrawlerMain(config).run();
  }
}
