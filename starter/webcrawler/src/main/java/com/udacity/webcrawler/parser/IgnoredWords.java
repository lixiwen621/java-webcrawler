package com.udacity.webcrawler.parser;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A binding annotation for the list of ignored word patterns.
 * 该注解是 list中忽略某些单词列表
 *
 * <p>This annotation has package-private visibility, which means it is not usable outside the
 * {@code com.udacity.webcrawler.parser} package, and it's only used so that the Guice module in
 * this package is able to inject all the dependencies of the HTML parser implementation.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@interface IgnoredWords {
}
