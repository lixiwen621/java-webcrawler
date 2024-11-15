package com.udacity.webcrawler.parser;

import com.google.inject.AbstractModule;
import com.google.inject.Key;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Guice dependency injection module that installs a {@link PageParserFactory} that can be used to
 * create page parsers.
 * Guice 依赖注入模块，安装 {@link PageParserFactory}，可用于创建页面解析器
 * 在使用 Guice 时，用户可以创建并安装 ParserModule，以便将超时、忽略词语等注入到 PageParserFactory 或相关组件中：
 * <blockquote><pre>{@code
 * Injector injector = Guice.createInjector(new ParserModule.Builder()
 *                          .setTimeout(Duration.ofSeconds(30))
 *                          .setIgnoredWords(List.of(Pattern.compile("ignoredWord")))
 *                          .build());
 * PageParserFactory factory = injector.getInstance(PageParserFactory.class);
 * }</pre></blockquote>
 *
 */
public final class ParserModule extends AbstractModule {
  // 表示网页解析的超时时间
  private final Duration timeout;
  // 表示在网页解析过程中需要忽略的词语的正则表达式模式列表
  private final List<Pattern> ignoredWords;

  /**
   * Creates a {@link ParserModule} from the given timeout and ignored word patterns.
   * 这个类 ParserModule 是一个用于依赖注入的 Guice 模块，它负责配置和绑定与网页解析相关的依赖，
   * 包括解析超时时间（timeout）、需要忽略的词语模式（ignoredWords），
   * 以及用于创建 PageParser 实例的工厂类（PageParserFactory）。该模块的主要目的是通过依赖注入来管理这些对象的生命周期和依赖关系
   */
  private ParserModule(Duration timeout, List<Pattern> ignoredWords) {
    this.timeout = timeout;
    this.ignoredWords = ignoredWords;
  }

  /**
   * 这个方法是 Guice 的抽象方法，负责配置依赖的绑定关系
   */
  @Override
  protected void configure() {
    // 这行代码使用 Key 类将 Duration 类型的对象与 @ParseDeadline 注解 绑定，表示注入的超时时间是与解析截止时间相关的
    bind(Key.get(Duration.class, ParseDeadline.class)).toInstance(timeout);
    // 这行代码绑定了一个带有 @IgnoredWords 注解的 List<Pattern> 对象，用于指示哪些词语应该在解析过程中被忽略
    bind(new Key<List<Pattern>>(IgnoredWords.class) {}).toInstance(ignoredWords);
    // 这行代码将 PageParserFactory 接口与它的实现 PageParserFactoryImpl 绑定，
    // 表示每次需要 PageParserFactory 时，都会返回 PageParserFactoryImpl 的实例
    bind(PageParserFactory.class).to(PageParserFactoryImpl.class);
  }

  /**
   * A builder class for {@link ParserModule}.
   */
  public static final class Builder {
    private Duration timeout;
    private List<Pattern> ignoredWords;

    /**
     * Sets the timeout that will be used by the page parser.
     */
    public Builder setTimeout(Duration timeout) {
      this.timeout = Objects.requireNonNull(timeout);
      return this;
    }

    /**
     * Sets the ignored word patterns that will be used by the page parser.
     */
    public Builder setIgnoredWords(List<Pattern> ignoredWords) {
      this.ignoredWords = Objects.requireNonNull(ignoredWords);
      return this;
    }

    /**
     * Builds a {@link ParserModule} from this {@link Builder}.
     */
    public ParserModule build() {
      return new ParserModule(timeout, ignoredWords);
    }
  }
}
