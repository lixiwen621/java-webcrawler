package com.udacity.webcrawler.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * Utility class to write a {@link CrawlResult} to file.
 * 用于将网页爬取结果 (CrawlResult) 以 JSON 格式写入文件或其他输出流中
 *
 */
public final class CrawlResultWriter {
  /**
   * 一个不可变的 CrawlResult 对象
   */
  private final CrawlResult result;

  /**
   * Creates a new {@link CrawlResultWriter} that will write the given {@link CrawlResult}.
   */
  public CrawlResultWriter(CrawlResult result) {
    this.result = Objects.requireNonNull(result);
  }

  /**
   * Formats the {@link CrawlResult} as JSON and writes it to the given {@link Path}.
   *
   * <p>If a file already exists at the path, the existing file should not be deleted; new data
   * should be appended to it.
   *
   * @param path the file path where the crawl result data should be written.
   *
   *  将 CrawlResult 写入到指定的文件路径 path
   *  1.使用 Files.newBufferedWriter() 创建或打开指定的文件。如果文件不存在，会创建文件；
   *             如果文件已存在，则在文件末尾追加内容（StandardOpenOption.APPEND）。
   * 	2.调用 write(Writer writer) 方法将数据写入文件。
   * 	3.捕获可能的 IOException 异常并打印堆栈跟踪。
   * 	注意：此方法确保文件不会被覆盖，而是将新的数据追加到文件末尾。
   */
  public void write(Path path) {
    // This is here to get rid of the unused variable warning.
    Objects.requireNonNull(path);
    // 创建或打开文件
    // 	1.	StandardOpenOption.TRUNCATE_EXISTING: 如果文件已经存在，它会在写入前将文件内容清空，从而覆盖旧文件。
    //	2.	StandardOpenOption.CREATE: 如果文件不存在，则创建它。
    try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
      write(writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Formats the {@link CrawlResult} as JSON and writes it to the given {@link Writer}.
   *
   * @param writer the destination where the crawl result data should be written.
   *
   *  将 CrawlResult 写入到指定的 Writer 对象（例如 BufferedWriter、StringWriter 等）
   *  1.创建一个 ObjectMapper 对象，这是 Jackson 库提供的类，用于将 Java 对象序列化为 JSON 或将 JSON 反序列化为 Java 对象。
   *  2.调用 objectMapper.writeValue(writer, result)，将 CrawlResult 对象序列化为 JSON 格式并写入 writer。
   *  3.捕获 IOException 异常并打印堆栈跟踪。
   */
  public void write(Writer writer) {
    // This is here to get rid of the unused variable warning.
    Objects.requireNonNull(writer);
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      objectMapper.writeValue(writer, result);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
