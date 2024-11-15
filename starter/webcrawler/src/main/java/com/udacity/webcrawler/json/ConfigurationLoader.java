package com.udacity.webcrawler.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * A static utility class that loads a JSON configuration file.
 * 这个 ConfigurationLoader 类的主要功能是从 JSON 配置文件中加载爬虫配置 (CrawlerConfiguration)。
 * 它提供了两个方法：一个用于从路径 (Path) 加载配置，另一个用于从 Reader 对象加载配置
 */
@Slf4j
public final class ConfigurationLoader {

    // 表示配置文件所在的路径。通过 path 字段，该类可以定位并加载配置文件
    private final Path path;

    /**
     * Create a {@link ConfigurationLoader} that loads configuration from the given {@link Path}.
     * 构造函数接受一个 Path 对象，指定要加载配置文件的路径，并通过 Objects.requireNonNull(path) 确保该路径不为 null
     */
    public ConfigurationLoader(Path path) {
        this.path = Objects.requireNonNull(path);
    }

    /**
     * Loads configuration from this {@link ConfigurationLoader}'s path
     * 该方法将从构造函数中传入的路径加载配置文件。
     * 虽然目前该方法还没有被实现，但它应该包括打开文件、解析 JSON 文件内容并将其转换为 CrawlerConfiguration 对象
     *  load() 需要完成从 path 中读取 JSON 数据并解析为 CrawlerConfiguration。
     *  可以借助库（如 Jackson 或 Gson）解析 JSON 数据。
     * @return the loaded {@link CrawlerConfiguration}.
     */
    public CrawlerConfiguration load() {
        // 使用 Files.newBufferedReader 打开指定路径的文件，并调用 read() 方法解析 JSON 数据
        try (Reader reader = Files.newBufferedReader(path)) {
            // 从JSON文件解析内容并生成CrawlerConfiguration 配置对象
            return read(reader);
        } catch (IOException e) {
            log.error("Failed to load configuration from file",e);
            // 如果加载失败，可以抛出运行时异常，或者根据需求返回一个默认的配置
            throw new RuntimeException("Failed to load configuration from file", e);
        }
    }

    /**
     * Loads crawler configuration from the given reader.
     * 它可以直接通过类名调用。该方法接受一个 Reader 对象作为参数，这个 Reader 指向包含爬虫配置的 JSON 字符串
     * 但通常它应该读取并解析由 Reader 提供的 JSON 数据，将其转换为 CrawlerConfiguration 对象
     *
     * @param reader a Reader pointing to a JSON string that contains crawler configuration.
     * @return a crawler configuration
     *
     */
    public static CrawlerConfiguration read(Reader reader){
        Objects.requireNonNull(reader);
        try {
            return parseJsonToCrawlerConfiguration(reader);
        }catch (Exception e){
            CrawlerConfiguration.Builder builder = new CrawlerConfiguration.Builder();
            return builder.build();
        }
    }

    /**
     *  通过Reader解析 json文件并生成 CrawlerConfiguration 配置对象
     * @param reader a Reader pointing to a JSON string that contains crawler configuration
     * @return
     */
    private static CrawlerConfiguration parseJsonToCrawlerConfiguration(Reader reader) throws IOException {
        Objects.requireNonNull(reader);
        // 创建 ObjectMapper 实例
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.readValue(reader, CrawlerConfiguration.class);
    }
}
