package com.udacity.webcrawler.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Evaluator.Tag;
import org.jsoup.select.NodeVisitor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * An implementation of {@link PageParser} that works for both local and remote files.
 *
 * <p>HTML parsing is done using the JSoup library. This class is a thin adapter around JSoup's API,
 * since JSoup does not know how to correctly resolve relative hyperlinks when parsing HTML from
 * local files.
 * PageParserImpl 是 PageParser 接口的一个实现类，它使用 JSoup 库来解析本地和远程的 HTML 文件。
 * 通过这个类，网页内容可以被解析为单词频率和超链接，支持处理相对链接和超时控制
 *
 */
final class PageParserImpl implements PageParser {

  /**
   * Matches whitespace characters.
   * 正则表达式的匹配常量 WHITESPACE: 匹配空白字符的正则表达式 (\\s+)
   */
  private static final Pattern WHITESPACE = Pattern.compile("\\s+");

  /**
   * Matches non-word characters.
   * NON_WORD_CHARACTERS: 匹配非单词字符的正则表达式 (\\W)，用来去掉不属于单词的符号
   * 汉字也会去掉
   */
  private static final Pattern NON_WORD_CHARACTERS = Pattern.compile("\\W");
  // uri 表示需要解析的文件的 URI
  private final String uri;
  //timeout 表示下载超时时间
  private final Duration timeout;
  // ignoredWords 是需要忽略的单词模式列表
  private final List<Pattern> ignoredWords;

  /**
   * Constructs a page parser with the given parameters.
   *
   * @param uri          the URI of the file to parse.
   * @param timeout      the timeout to use when downloading the file, if it is remote.
   * @param ignoredWords patterns of which words should be ignored by the {@link #parse()} method.
   */
  PageParserImpl(String uri, Duration timeout, List<Pattern> ignoredWords) {
    this.uri = Objects.requireNonNull(uri);
    this.timeout = Objects.requireNonNull(timeout);
    this.ignoredWords = Objects.requireNonNull(ignoredWords);
  }

  /**
   *  这是实现的核心方法，它使用 JSoup 库解析 HTML 页面，并返回解析的结果，包括单词计数和超链接列表
   *  该方法首先尝试将 uri 转换为 URI 对象。如果无效，则返回一个空的 Result
   *  它通过 parseDocument 方法解析 HTML 文档，接着使用 NodeVisitor 遍历文档，提取超链接和文本，并进行单词频率统计
   * @return
   */
  @Override
  public Result parse() {
    // 将 URI 解析为 URI 对象
    URI parsedUri;
    try {
      parsedUri = new URI(uri);
    } catch (URISyntaxException e) {
      // Invalid link; ignore
      return new Result.Builder().build(); // 如果 URI 无效，返回空结果
    }
    // 使用 JSoup 解析文档
    Document document;
    try {
      document = parseDocument(parsedUri);
    } catch (Exception e) {
      // There are multiple exceptions that can be encountered due to invalid URIs or Mimetypes that
      // Jsoup does not handle. There is not much we can do here.
      return new Result.Builder().build(); // 处理各种解析异常
    }

    Result.Builder builder = new Result.Builder();
    // Do a single pass over the document to gather all hyperlinks and text.
    document.traverse(new NodeVisitor() {
      @Override
      public void head(Node node, int depth) {
        if (node instanceof TextNode) {
          // 解析文本节点，统计单词频率
          String text = ((TextNode) node).text().strip();
          //WHITESPACE.split(text) 将传入的 text 字符串按照空白字符进行拆分，返回一个字符串数组
          Arrays.stream(WHITESPACE.split(text))
              .filter(s -> !s.isBlank())
                  // 过滤掉那些与 ignoredWords 中任一模式匹配的字符串
              .filter(s -> ignoredWords.stream().noneMatch(p -> p.matcher(s).matches()))
              .map(s -> NON_WORD_CHARACTERS.matcher(s).replaceAll("")) // 去除非单词字符(包括汉字也会去掉), 并替换为 ""
              .filter(s -> !s.isBlank())
              .map(String::toLowerCase)
              .forEach(builder::addWord);
          return;
        }
        // 不是 Element节点 直接过滤
        if (!(node instanceof Element)) {
          return;
        }
        // 对于 HTML 标签（例如 <meta>, <link>, <title>, <html>, <head> 等），它们会被识别为 Element 节点
        Element element = (Element) node;
        // 如果 element 不是 <a> 标签 或者 a标签没有 href属性 ，直接 return，跳过这个节点
        if (!element.is(new Tag("a")) || !element.hasAttr("href")) {
          return;
        }
        if (isLocalFile(parsedUri)) {
          // If this is a local file, add the base path back in manually, since Jsoup only knows how
          // to resolve relative hrefs if the base URI is a "real" remote URI.
          String basePath = Path.of(parsedUri).getParent().toString();
          builder.addLink(Path.of(basePath, element.attr("href")).toUri().toString());
        } else {
          // Otherwise, let Jsoup resolve the absolute URL for us.
          // 添加<a 标签 带有 href的>到 builder中的 link集合中
          // 并且 builder中的link 后续也会进行解析，不过需要跟配置中的maxDepth来判断是否继续解析
          builder.addLink(element.attr("abs:href"));
        }
      }

      @Override
      public void tail(Node node, int depth) {
      }
    });
    return builder.build();
  }

  /**
   * Returns a Jsoup {@link Document} representation of the file at the given {@link URI}, which may
   * refer to a local document or a remote web page.
   *
   * 该方法根据 URI 解析 HTML 文件，可以处理本地文件和远程网页
   * 对于远程网页，直接使用 Jsoup.parse(uri.toURL(), timeout) 来解析
   * 对于本地文件，使用 Jsoup.parse(in, StandardCharsets.UTF_8.name(), "")，
   * 因为 JSoup 对本地文件的 URI 处理存在限制，需要手动解析相对路径。
   */
  private Document parseDocument(URI uri) throws IOException {
    // 判断是本地文件的解析 还是 解析网页
    // 解析网页地址
    if (!isLocalFile(uri)) {
      return Jsoup.parse(uri.toURL(), (int) timeout.toMillis());
    }

    // Unfortunately, Jsoup.parse() has a baseUri parameter that does not work with local
    // "file://" URIs. If we want the parser to support those URIs, which are very useful for
    // testing, the work-around is to pass in an empty baseUri and manually add the base back to
    // href attributes.
    try (InputStream in = Files.newInputStream(Path.of(uri))) {
      return Jsoup.parse(in, StandardCharsets.UTF_8.name(), "");
    }
  }

  /**
   * Returns true if and only if the given {@link URI} represents a local file.
   * 这段代码是用于判断当前 URL 是否指向本地文件，通过检查 URI 的 scheme 是否为 file
   *
   * uri.getScheme(): 获取 URI 的协议部分（例如 http, https, file, ftp 等）。
   * 检查 uri.getScheme() 是否不为 null，即确保 URI 有定义协议
   * 比较 uri.getScheme() 是否等于 "file"，表示当前 URI 是本地文件系统的路径（即以 file:// 开头的 URL）
   */
  private static boolean isLocalFile(URI uri) {
    return uri.getScheme() != null && uri.getScheme().equals("file");
  }
}