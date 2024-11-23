package com.udacity.webcrawler.profiler;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 * ProfilingMethodInterceptor 类是一个实现了 InvocationHandler 接口的类，用于拦截方法调用，并检查方法是否带有 @Profiled 注解。
 * 如果有，则记录该方法的执行时间。这个类的主要目的是通过动态代理来监控被代理对象的方法执行情况，尤其是执行时间。
 *
 */
@Slf4j
final class ProfilingMethodInterceptor implements InvocationHandler {

  // Clock clock 字段：用于获取当前时间，以便测量方法的执行时间
  private final Clock clock;
  private final ProfilingState state;
  private final Object delegate;

  // TODO: You will need to add more instance fields and constructor arguments to this class.
  <T> ProfilingMethodInterceptor(T delegate,Clock clock,ProfilingState state) {
    this.clock = Objects.requireNonNull(clock);
    this.state = Objects.requireNonNull(state);
    this.delegate = Objects.requireNonNull(delegate);
  }

  /**
   *  invoke 方法：invoke 方法是 InvocationHandler 接口中的核心方法，每当代理对象的方法被调用时，都会触发这个方法
   * @param proxy the proxy instance that the method was invoked on
   *
   * @param method the {@code Method} instance corresponding to
   * the interface method invoked on the proxy instance.  The declaring
   * class of the {@code Method} object will be the interface that
   * the method was declared in, which may be a superinterface of the
   * proxy interface that the proxy class inherits the method through.
   *
   * @param args an array of objects containing the values of the
   * arguments passed in the method invocation on the proxy instance,
   * or {@code null} if interface method takes no arguments.
   * Arguments of primitive types are wrapped in instances of the
   * appropriate primitive wrapper class, such as
   * {@code java.lang.Integer} or {@code java.lang.Boolean}.
   *
   * @return
   */
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // 检查方法是否有 @Profiled 注解
    if (method.isAnnotationPresent(Profiled.class)) {
      Instant start = clock.instant(); // 记录方法开始时间
      try {
        return method.invoke(delegate, args); // 执行方法
      }catch (InvocationTargetException e){
        log.error("ProfilingMethodInterceptor invoke Exception",e);
        throw e.getTargetException();
      } catch (IllegalAccessException e){
        log.error("ProfilingMethodInterceptor IllegalAccessException",e);
        throw new RuntimeException(e);
      }
      finally {
        Instant end = clock.instant(); // 记录方法结束时间
        Duration duration = Duration.between(start, end);
        state.record(delegate.getClass(), method, duration); // 记录方法的执行,并输出日志
      }
    } else {
      return method.invoke(delegate, args); // 如果没有 @Profiled 注解，直接执行方法
    }
  }
}
