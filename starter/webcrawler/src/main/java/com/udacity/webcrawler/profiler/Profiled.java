package com.udacity.webcrawler.profiler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks which methods should have their running time profiled.
 * @Profiled 是一个自定义注解，用于标记哪些方法需要进行运行时间的性能分析
 */
@Target({ElementType.METHOD}) // 表示这个注解只能用于方法上
@Retention(RetentionPolicy.RUNTIME) // RUNTIME 表示这个注解在运行时仍然存在
public @interface Profiled {
}
