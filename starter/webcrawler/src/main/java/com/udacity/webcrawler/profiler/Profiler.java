package com.udacity.webcrawler.profiler;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

/**
 * A utility that wraps an object that should be performance profiled.
 * Profiler接口的作用是提供一种性能分析工具，用于对某个对象的方法调用进行性能分析
 *
 * <p>The profiler aggregates information about profiled method calls, and how long they took. The
 * aggregate information can then be written to a file with {@link #writeData(Writer) writeData}.
 */
public interface Profiler {

  /**
   * Wraps the given delegate to have its methods profiled.
   * wrap方法用于将传入的对象（delegate）进行包装，使其方法可以被性能分析
   *
   * @param klass    the class object representing the interface of the delegate.
   *                 klass 这是一个Class对象，表示要包装的对象的接口类型
   * @param delegate the object that should be profiled.
   *                 delegate 这是实际要进行性能分析的对象。该对象应该实现了klass参数代表的接口
   * @param <T>      type of the delegate object, which must be an interface type. The interface
   *                 must have at least one of its methods annotated with the {@link Profiled}
   *                 annotation.
   *                  泛型参数，表示delegate对象的类型，要求是一个接口类型
   *
   * @return A wrapped version of the delegate that
   *         返回一个经过包装的delegate对象，这个包装对象的所有方法调用都将被记录以便进行性能分析
   * @throws IllegalArgumentException if the given delegate does not have any methods annotated with
   *                                  the {@link Profiled} annotation.
   *                                  如果传入的delegate对象中没有任何方法被@Profiled注解标注，
   *                                  则抛出IllegalArgumentException异常。这意味着必须至少有一个方法被标注为需要性能分析
   */
  <T> T wrap(Class<T> klass, T delegate);

  /**
   * Formats the profile data as a string and writes it to the given {@link Path}.
   *
   * <p>If a file already exists at the path, the existing file should not be deleted; new data
   * should be appended to it.
   *
   * 将收集到的性能分析数据格式化为字符串并写入指定的Path（文件路径）中。
   * 如果该路径已有文件存在，新数据会被追加到现有文件末尾，而不是覆盖或删除已有文件
   *
   * @param path the destination where the formatted data should be written.
   *             表示要写入数据的目标文件路径
   * @throws IOException if there was a problem writing the data to file.
   *          如果在写入数据到文件时发生问题，会抛出IOException异常
   *
   */
  void writeData(Path path) throws IOException;

  /**
   * Formats the profile data as a string and writes it to the given {@link Writer}.
   * 将收集到的性能分析数据格式化为字符串，并写入指定的Writer对象（通常是一个输出流或文件写入器）
   *
   * @param writer the destination where the formatted data should be written.
   *               writer: 表示要写入数据的目标Writer对象
   *
   * @throws IOException if there was a problem writing the data.
   *                如果在写入数据时发生问题，同样会抛出IOException异常
   */
  void writeData(Writer writer) throws IOException;
}
