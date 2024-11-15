package com.udacity.webcrawler.profiler;

public interface ExampleService {

    @Profiled
    void doWork();
    void notProfiledMethod();
}