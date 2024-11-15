package com.udacity.webcrawler.profiler;

public class ExampleServiceImpl implements ExampleService {
    @Override
    public void doWork() {
        // 模拟一些耗时的操作
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void notProfiledMethod() {
        // 这个方法不会被性能分析
        System.out.println("This method is not profiled.");
    }
    @Profiled
    public void test(){
        System.out.println("this is test profiled");
    }
}