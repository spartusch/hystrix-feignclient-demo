package com.github.spartusch.hfdemo.service;

import com.github.spartusch.hfdemo.clients.Decode404FeignClient;
import com.github.spartusch.hfdemo.clients.DefaultFeignClient;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DemoServiceImpl implements DemoService {

    private DefaultFeignClient defaultFeignClient;
    private Decode404FeignClient decode404FeignClient;

    @Autowired
    public DemoServiceImpl(final DefaultFeignClient defaultFeignClient,
                           final Decode404FeignClient decode404FeignClient) {
        this.defaultFeignClient = defaultFeignClient;
        this.decode404FeignClient = decode404FeignClient;
    }

    @Override
    public String getDataUsingDefaultFeignClient() {
        return defaultFeignClient.getData();
    }

    @Override
    @HystrixCommand(fallbackMethod = "getDataUsingDefaultFeignClientFallback")
    public String getDataUsingDefaultFeignClientWithFallback() {
        return defaultFeignClient.getData();
    }

    public String getDataUsingDefaultFeignClientFallback(final Throwable e) {
        return "fallback";
    }

    @Override
    public String getDataUsingDecode404FeignClient() {
        return decode404FeignClient.getData();
    }

}
