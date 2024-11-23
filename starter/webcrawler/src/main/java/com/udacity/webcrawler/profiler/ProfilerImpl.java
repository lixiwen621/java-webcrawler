package com.udacity.webcrawler.profiler;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 *  ProfilerImpl类是Profiler接口的一个具体实现，主要用于实现方法性能分析的功能
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {
  // 一个Clock对象，用于获取当前时间。这是一个不可变对象，用于确保时间的获取是可控和一致的
  private final Clock clock;
  // 一个ProfilingState对象，用于存储和管理性能分析的状态数据。这是一个内部状态，用来记录被分析的方法调用的信息
  private final ProfilingState state = new ProfilingState();
  // 一个ZonedDateTime对象，表示性能分析开始的时间。这是类初始化时的时间，用来标记性能分析的起点
  private final ZonedDateTime startTime;

  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }

  /**
   *  该方法负责将传入的对象进行包装，使其方法调用能够被记录和分析
   * @param klass    the class object representing the interface of the delegate.
   *                 klass 这是一个Class对象，表示要包装的对象的接口类型
   * @param delegate the object that should be profiled.
   *                 delegate 这是实际要进行性能分析的对象。该对象应该实现了klass参数代表的接口
   * @return
   * @param <T>
   */
  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
    Objects.requireNonNull(klass);
    Objects.requireNonNull(delegate);
    //  Use a dynamic proxy (java.lang.reflect.Proxy) to "wrap" the delegate in a
    //       ProfilingMethodInterceptor and return a dynamic proxy from this method.
    //       See https://docs.oracle.com/javase/10/docs/api/java/lang/reflect/Proxy.html.

    // 检查是否存在被 @Profiled 注解的方法
    boolean hasProfiledMethod = false;
    for (Method method : klass.getDeclaredMethods()) {
      if (method.isAnnotationPresent(Profiled.class)) {
        hasProfiledMethod = true;
        break;
      }
    }

    if (!hasProfiledMethod) {
      throw new IllegalArgumentException("The delegate does not have any methods annotated with @Profiled");
    }
    // 创建动态代理 基于接口的动态代理
    // Proxy.newProxyInstance方法用于创建一个动态代理对象，该对象会拦截对klass接口的所有方法调用
    // ProfilingMethodInterceptor是一个实现了InvocationHandler接口的类，它负责在方法调用时记录方法的执行时间并将调用委托给实际的delegate对象
    return (T) Proxy.newProxyInstance(
            klass.getClassLoader(),
            new Class<?>[]{klass},
            new ProfilingMethodInterceptor(delegate, clock, state)
    );

  }

  /**
   *  该方法用于将分析数据写入到指定的文件路径中
   * @param path the destination where the formatted data should be written.
   *             表示要写入数据的目标文件路径
   */
  @Override
  public void writeData(Path path) {
    //  Write the ProfilingState data to the given file path. If a file already exists at that
    //       path, the new data should be appended to the existing file.

    Objects.requireNonNull(path);
    // Files.newBufferedWriter方法用于创建一个BufferedWriter，
    // 这里使用了StandardOpenOption.CREATE和StandardOpenOption.APPEND选项，以确保文件存在时数据会被追加，而不存在时会创建新文件
    try (Writer writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
      writeData(writer);
    } catch (IOException e) {
      // 处理IO异常，比如记录日志或重新抛出异常
      e.printStackTrace();
    }
  }

  /**
   *  这个方法将分析数据写入到指定的Writer对象（比如输出流或文件写入器）中
   * @param writer the destination where the formatted data should be written.
   *               writer: 表示要写入数据的目标Writer对象
   *
   * @throws IOException
   */
  @Override
  public void writeData(Writer writer) throws IOException {
    // RFC 1123 是一个用于互联网协议的日期和时间格式标准 Tue, 3 Jun 2008 11:05:30 GMT
    writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
    // 写一个换行符
    writer.write(System.lineSeparator());
    state.write(writer);
    writer.write(System.lineSeparator());
  }
}
