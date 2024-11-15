package com.udacity.webcrawler;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.ProvisionException;
import com.google.inject.multibindings.Multibinder;
import com.udacity.webcrawler.json.CrawlerConfiguration;
import com.udacity.webcrawler.parser.ParserModule;
import com.udacity.webcrawler.profiler.Profiler;

import javax.inject.Qualifier;
import javax.inject.Singleton;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Guice dependency injection module that installs all the required dependencies to run the web
 * crawler application. Callers should use it like this:
 *
 * 定义了一个基于 Guice 框架的依赖注入模块 WebCrawlerModule，用于配置和注入 WebCrawler 实例及其相关的依赖。
 * 这是一个典型的依赖注入模块，主要用于配置应用中的各个组件，并通过 Guice 自动进行依赖的绑定和注入
 *
 * <pre>{@code
 *   CrawlerConfiguration config = ...;
 *   WebCrawler crawler =
 *       Guice.createInjector(new WebCrawlerModule(config))
 *           .getInstance(WebCrawler.class);
 * }</pre>
 */
public final class WebCrawlerModule extends AbstractModule {

  private final CrawlerConfiguration config;

  /**
   * Installs a web crawler that conforms to the given {@link CrawlerConfiguration}.
   */
  public WebCrawlerModule(CrawlerConfiguration config) {
    this.config = Objects.requireNonNull(config);
  }

  @Override
  protected void configure() {
    // Multibinder provides a way to implement the strategy pattern through dependency injection.
    // Guice 的 Multibinder 用来将多个实现类绑定到一个接口上，这个方式非常适合实现策略模式
    // 这里的 Multibinder 创建了一个带有 @Internal 注解的 Set<WebCrawler>, 当使用 Set<WebCrawler>需要带上注解
    Multibinder<WebCrawler> multibinder =
        Multibinder.newSetBinder(binder(), WebCrawler.class, Internal.class);
    multibinder.addBinding().to(SequentialWebCrawler.class);
    multibinder.addBinding().to(ParallelWebCrawler.class);
    // 表示将 Clock 类绑定到 Clock.systemUTC()，这意味着每当注入 Clock 时，都会使用 UTC 时间
    bind(Clock.class).toInstance(Clock.systemUTC());
    // Key.get(Integer.class, MaxDepth.class) 这里是 MaxDepth注解绑定 Integer类型
    // 然后把 CrawlerConfiguration 的 maxDepth字段值 赋值给 MaxDepth.class 注解
    bind(Key.get(Integer.class, MaxDepth.class)).toInstance(config.getMaxDepth());
    bind(Key.get(Integer.class, PopularWordCount.class)).toInstance(config.getPopularWordCount());
    bind(Key.get(Duration.class, Timeout.class)).toInstance(config.getTimeout());
    //这里使用了 Key<List<Pattern>> 来为带注解的 IgnoredUrls 绑定一个正则表达式的列表
    bind(new Key<List<Pattern>>(IgnoredUrls.class) {
    }).toInstance(config.getIgnoredUrls());
    // 这里通过 install() 安装了另一个模块 ParserModule, install 会将 ParserModule 中的绑定加入到当前的依赖注入上下文中
    install(
        new ParserModule.Builder()
            .setTimeout(config.getTimeout())
            .setIgnoredWords(config.getIgnoredWords())
            .build());
  }

  /**
   *  下面的实现中 从Set<WebCrawler>里，挑选一个 符合条件的来使用 WebCrawler接口的实现类
   * @param implementations
   * @param targetParallelism
   * @return
   */
  @Provides
  @Singleton
  @Internal
  WebCrawler provideRawWebCrawler(
      @Internal Set<WebCrawler> implementations,
      @TargetParallelism int targetParallelism) {
    String override = config.getImplementationOverride();
    if (!override.isEmpty()) {
      return implementations
          .stream()
          .filter(impl -> impl.getClass().getName().equals(override))
          .findFirst()
          .orElseThrow(() -> new ProvisionException("Implementation not found: " + override));
    }
    return implementations
        .stream()
            // 找出满足配置里的 Parallelism <= impl.getMaxParallelism()
            // 如果是 SequentialWebCrawler类,那么impl.getMaxParallelism()为1, 如果是 ParallelWebCrawler类,
            // 那么impl.getMaxParallelism()为 机器本身的 cpu数量
        .filter(impl -> targetParallelism <= impl.getMaxParallelism())
        .findFirst()
        .orElseThrow(
            () -> new ProvisionException(
                "No implementation able to handle parallelism = \"" +
                    config.getParallelism() + "\"."));
  }

  /**
   *  也可以再 configure 方法下实现下面的代码
   *  <pre>{@code
   *  int parallelism;
   *      if (config.getParallelism() >= 0) {
   *        parallelism = config.getParallelism();
   *        }else {
   *          parallelism = Runtime.getRuntime().availableProcessors();
   *         }
   *        bind(Key.get(Integer.class, TargetParallelism.class)).toInstance(parallelism);
   *  }</pre>
   *
   *
   * @return
   */
  @Provides
  @Singleton
  @TargetParallelism
  int provideTargetParallelism() {
    if (config.getParallelism() >= 0) {
      return config.getParallelism();
    }
    return Runtime.getRuntime().availableProcessors();
  }

  @Provides
  @Singleton
  WebCrawler provideWebCrawlerProxy(Profiler wrapper, @Internal WebCrawler delegate) {
    return wrapper.wrap(WebCrawler.class, delegate);
  }

  @Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  private @interface Internal {
  }
}
