package com.github.spartusch.hfdemo.service;

public interface DemoService {

    String getDataUsingDefaultFeignClient();

    String getDataUsingDefaultFeignClientWithFallback();

    String getDataUsingDecode404FeignClient();

}
