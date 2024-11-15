package com.udacity.webcrawler.profiler;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;

/**
 *  测试 @profiled 注解的 类
 */
public class ProfilerExample {

    public static void main(String[] args) throws IOException {
        String path = "/Users/lixiwen/JavaProjects/cd0381-advanced-java-programming-techniques-projectstarter/starter/webcrawler/src/main/java/com/udacity/webcrawler/profiler";
        // 创建 Clock 实例
        Clock clock = Clock.systemDefaultZone();
        Clock clock2 = Clock.systemUTC();


        // 创建 Profiler 实例
        Profiler profiler = new ProfilerImpl(clock2);

        ExampleService exampleService = new ExampleServiceImpl();

        // 使用 Profiler 包装 ExampleService 实例
        ExampleService service = profiler.wrap(ExampleService.class, exampleService);
        
        // 调用标记为 @Profiled 的方法
        service.doWork();
        
        // 调用未标记为 @Profiled 的方法
        service.notProfiledMethod();
        // 输出性能分析结果到控制台
        try (Writer consoleWriter = new OutputStreamWriter(System.out, StandardCharsets.UTF_8)) {
            profiler.writeData(consoleWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 将性能分析结果写入到文件
        Path outputPath = Paths.get("/Users/lixiwen/JavaProjects/cd0381-advanced-java-programming-techniques-projectstarter/starter/webcrawler/src/main/java/com/udacity/webcrawler/profiler/profile-data.txt");
        try {
            profiler.writeData(outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}