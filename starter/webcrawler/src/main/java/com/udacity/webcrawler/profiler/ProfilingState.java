package com.udacity.webcrawler.profiler;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 *
 * Helper class that records method performance data from the method interceptor.
 * ProfilingState类是一个辅助类，用于记录和管理方法调用的性能数据。它的主要职责是记录每个方法调用的持续时间，并在需要时将这些数据输出
 */
@Slf4j
final class ProfilingState {
  private final Map<String, DurationData> data = new ConcurrentHashMap<>();

  private static class DurationData {
    private Duration totalDuration = Duration.ZERO;
    private int callCount = 0;
    private final Map<Long, Integer> threadCallCounts = new ConcurrentHashMap<>(); // 新增线程 ID 记录

    // 增加方法调用的时长，并计数
    void addDuration(Duration duration) {
      this.totalDuration = this.totalDuration.plus(duration);
      this.callCount++;
      // 记录当前线程的调用次数
      long threadId = Thread.currentThread().getId();
      threadCallCounts.merge(threadId, 1, Integer::sum);
    }

    // 计算平均调用时长
    Duration getAverageDuration() {
      return (callCount == 0) ? Duration.ZERO : totalDuration.dividedBy(callCount);
    }

    // 获取总调用次数
    int getCallCount() {
      return callCount;
    }

    // 获取每个线程的调用次数
    Map<Long, Integer> getThreadCallCounts() {
      return threadCallCounts;
    }
  }

  /**
   * Records the given method invocation data.
   *  记录方法调用的性能数据
   *
   * @param callingClass the Java class of the object that called the method.  调用方法的类对象
   * @param method       the method that was called.  被调用的方法对象
   * @param elapsed      the amount of time that passed while the method was called.
   *                     方法调用经过的时间（Duration对象）
   */
  void record(Class<?> callingClass, Method method, Duration elapsed) {
    Objects.requireNonNull(callingClass);
    Objects.requireNonNull(method);
    Objects.requireNonNull(elapsed);
    if (elapsed.isNegative()) {
      throw new IllegalArgumentException("negative elapsed time");
    }
    String key = formatMethodCall(callingClass, method);
    //data.compute(key, (k, v) -> (v == null) ? elapsed : v.plus(elapsed));
    data.computeIfAbsent(key, k -> new DurationData()).addDuration(elapsed);
  }

  /**
   * Writes the method invocation data to the given {@link Writer}.
   *  将记录的数据写入到提供的Writer对象中
   * <p>Recorded data is aggregated across calls to the same method. For example, suppose
   * {@link #record(Class, Method, Duration) record} is called three times for the same method
   * {@code M()}, with each invocation taking 1 second. The total {@link Duration} reported by
   * this {@code write()} method for {@code M()} should be 3 seconds.
   */
  void write(Writer writer) throws IOException {
    List<String> entries = data.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> formatLogEntry(entry.getKey(), entry.getValue()))
            .toList();

    try {
      for (String entry : entries) {
        writer.write(entry);
        writer.write(System.lineSeparator());
      }
    } catch (IOException e) {
      // 记录日志以捕获 IOException
      log.error("Failed to write profiling data to writer", e);
    }
  }

  /**
   * Formats the given method call for writing to a text file.
   * 将类名和方法名格式化为一个字符串，表示方法调用
   *
   * @param callingClass the Java class of the object whose method was invoked.
   * @param method       the Java method that was invoked.
   * @return a string representation of the method call.
   */
  private static String formatMethodCall(Class<?> callingClass, Method method) {
    return String.format("%s#%s", callingClass.getName(), method.getName());
  }

  /**
   * 格式化日志条目，用于表示方法的执行情况。
   *
   * @param key 方法的标识（类名#方法名）
   * @param durationData 包含方法总时长和调用次数的数据
   * @return 格式化的日志条目字符串
   */
  private static String formatLogEntry(String key, DurationData durationData) {
    String averageDuration = formatDuration(durationData.getAverageDuration());
    String threadStats = durationData.getThreadCallCounts().entrySet()
            .stream()
            .map(e -> String.format("Thread %d: %d calls", e.getKey(), e.getValue()))
            .collect(Collectors.joining(", "));
    return String.format("%s took %s on average over %d calls. Thread call counts: [%s]",
            key, averageDuration, durationData.getCallCount(), threadStats);
  }

  /**
   * Formats the given {@link Duration} for writing to a text file.
   * 将Duration对象格式化为字符串，用于输出
   *
   */
  private static String formatDuration(Duration duration) {
    return String.format(
        "%sm %ss %sms", duration.toMinutes(), duration.toSecondsPart(), duration.toMillisPart());
  }
}
