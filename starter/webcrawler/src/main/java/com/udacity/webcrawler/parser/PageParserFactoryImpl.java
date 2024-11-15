package com.udacity.webcrawler.parser;

import com.udacity.webcrawler.Timeout;
import com.udacity.webcrawler.profiler.Profiler;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A {@link PageParserFactory} that wraps its returned instances using a {@link Profiler}.
 * PageParserFactoryImpl 是一个实现了 PageParserFactory 接口的类，
 * 它的主要功能是创建 PageParser 实例，并通过 Profiler 对返回的实例进行包装，以便对页面解析过程进行性能分析
 */
@Slf4j
final class PageParserFactoryImpl implements PageParserFactory {
  private final Profiler profiler;
  private final List<Pattern> ignoredWords;
  private final Duration timeout;

  /**
   *  通过使用 依赖注入（DI），该类的构造函数注入了三个关键依赖：
   * 	•	Profiler：用于性能分析的工具类，提供了 wrap 方法，可以将任意对象包装起来，并监控其性能。
   * 	•	ignoredWords：使用了 @IgnoredWords 注解，表明这个 List<Pattern> 是一组需要在解析过程中忽略的词汇模式。
   *    	Pattern 表示正则表达式，可以用来匹配需要忽略的词。
   * 	•	timeout：注入了带有 @Timeout 注解的 Duration 对象，表示解析过程中用于处理页面的超时时间。
   * @param profiler
   * @param ignoredWords
   * @param timeout
   */
  @Inject
  PageParserFactoryImpl(
      Profiler profiler, @IgnoredWords List<Pattern> ignoredWords, @Timeout Duration timeout) {
    this.profiler = profiler;
    this.ignoredWords = ignoredWords;
    this.timeout = timeout;
  }

  /**
   * •	这个方法首先创建了一个 PageParserImpl 实例，传入了 url, timeout, 和 ignoredWords 作为参数。
   * 	•	PageParserImpl 是 PageParser 的一个具体实现，负责实际的页面解析逻辑。
   * 	•	timeout 用于控制解析过程中最大允许的时间，ignoredWords 用于忽略解析过程中不关心的词语。
   * 	•	然后，它使用 Profiler 的 wrap 方法对 PageParserImpl 实例进行包装。
   *    	这种包装是为了监控和记录解析器在运行时的性能数据，以便分析和调试。
   * @param url
   * @return
   */
  @Override
  public PageParser get(String url) {
    // Here, parse the page with the initial timeout (instead of just the time remaining), to make
    // the download less likely to fail. Deadline enforcement should happen at a higher level.
    PageParser delegate = new PageParserImpl(url, timeout, ignoredWords);
    log.info("url ="+ url);
    return profiler.wrap(PageParser.class, delegate);
  }
}
