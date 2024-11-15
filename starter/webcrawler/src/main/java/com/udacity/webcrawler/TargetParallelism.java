package com.udacity.webcrawler;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A binding annotation for the number of CPU cores specified in the crawler configuration.
 *
 * <p>The value bound to this annotation is a Java duration based on the {@code "timeoutSeconds"}
 * option from the crawler configuration JSON.
 *
 * 这段注释说明了 @TargetParallelism 用于从爬虫配置文件中读取一个与并行度相关的值（通过 "timeoutSeconds" 选项定义），
 * 并将其绑定到代码中的某个字段或参数
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface TargetParallelism {
}
