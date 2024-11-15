package com.udacity.webcrawler.profiler;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import java.time.Clock;

/**
 * Guice dependency injection module that installs a {@link Profiler} singleton.
 *  ProfilerModule 类是一个使用 Google Guice 实现依赖注入的模块类。
 *  它的作用是配置依赖注入框架，为项目中的某些类和接口提供具体的实现
 * <p>Requires a {@link java.time.Clock} to already be bound.
 */
public final class ProfilerModule extends AbstractModule {
  // @Provides 注解的方法用于告诉 Guice 如何提供某个依赖的具体实例。在这个例子中，它用于告诉 Guice 如何提供 Profiler 的实例。
  // @Singleton 注解表示这个方法返回的 Profiler 实例在应用程序的整个生命周期中都是单例的。
  // 也就是说，无论在哪里注入 Profiler，都将获得相同的实例。

  // ProfilerModule 类通过 Guice 依赖注入框架将 Profiler 接口与其实现 ProfilerImpl 绑定为单例，并提供给应用程序中的其他类使用。
  // 这样做的好处是可以通过依赖注入减少代码耦合，同时提高代码的可测试性和维护性
  @Provides
  @Singleton
  Profiler provideProfiler(Clock clock) {
    return new ProfilerImpl(clock);
  }
}
