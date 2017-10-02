package com.github.spartusch.hfdemo.service;

import com.github.spartusch.hfdemo.clients.Decode404FeignClient;
import com.github.spartusch.hfdemo.clients.ErrorDecoderFeignClient;
import com.github.spartusch.hfdemo.clients.FallbackFeignClient;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Single;

@Service
public class DemoServiceImpl implements DemoService {

    private Decode404FeignClient decode404FeignClient;
    private FallbackFeignClient fallbackFeignClient;
    private ErrorDecoderFeignClient errorDecoderFeignClient;

    @Autowired
    public DemoServiceImpl(final Decode404FeignClient decode404FeignClient,
                           final FallbackFeignClient fallbackFeignClient,
                           final ErrorDecoderFeignClient errorDecoderFeignClient) {
        this.decode404FeignClient = decode404FeignClient;
        this.fallbackFeignClient = fallbackFeignClient;
        this.errorDecoderFeignClient = errorDecoderFeignClient;
    }

    @Override
    public String getData() {
        return decode404FeignClient.getData();
    }

    @Override
    @HystrixCommand(fallbackMethod = "fallbackMethod")
    public String getDataWithFallback() {
        return fallbackFeignClient.getData();
    }

    public String fallbackMethod(final Throwable e) {
        return "fallback";
    }

    @Override
    public String getDataWithErrorDecoder() {
        return errorDecoderFeignClient.getData();
    }

    @Override
    public Single<String> getRxData() {
        return decode404FeignClient.getRxData();
    }

}
