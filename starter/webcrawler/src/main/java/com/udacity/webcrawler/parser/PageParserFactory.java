package com.udacity.webcrawler.parser;

/**
 * A factory interface that supplies instances of {@link PageParser} that have common parameters
 * (such as the timeout and ignored words) preset from injected values.
 * 这个类定义了一个名为 PageParserFactory 的工厂接口，它的作用是提供 PageParser 实例，
 * 且这些实例的某些参数（例如超时设置和需要忽略的词汇）已经通过依赖注入预先设置好
 */
public interface PageParserFactory {

  /**
   * Returns a {@link PageParser} that parses the given {@link url}.
   * 工厂的职责是创建 PageParser 实例，这些实例会根据给定的 url 进行页面解析
   * 通过不同的 url, 可以解析为不同的PageParser的子类, 并且不同的 url会 new出不同的PageParser的子类
   */
  PageParser get(String url);
}
