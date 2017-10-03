package com.github.spartusch.hfdemo.service;

import com.github.spartusch.hfdemo.clients.Decode404FeignClient;
import com.github.spartusch.hfdemo.clients.DefaultFeignClient;
import com.github.spartusch.hfdemo.clients.ErrorDecoderFeignClient;
import com.github.spartusch.hfdemo.clients.AlternateDefaultFeignClient;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Single;

@Service
public class DemoServiceImpl implements DemoService {

    private DefaultFeignClient defaultFeignClient;
    private Decode404FeignClient decode404FeignClient;
    private AlternateDefaultFeignClient alternateDefaultFeignClient;
    private ErrorDecoderFeignClient errorDecoderFeignClient;

    @Autowired
    public DemoServiceImpl(final DefaultFeignClient defaultFeignClient,
                           final Decode404FeignClient decode404FeignClient,
                           final AlternateDefaultFeignClient alternateDefaultFeignClient,
                           final ErrorDecoderFeignClient errorDecoderFeignClient) {
        this.defaultFeignClient = defaultFeignClient;
        this.decode404FeignClient = decode404FeignClient;
        this.alternateDefaultFeignClient = alternateDefaultFeignClient;
        this.errorDecoderFeignClient = errorDecoderFeignClient;
    }

    @Override
    public String getData() {
        return defaultFeignClient.getData();
    }

    @Override
    public String getDataWithDecode404() {
        return decode404FeignClient.getData();
    }

    @Override
    @HystrixCommand(fallbackMethod = "fallbackMethod")
    public String getDataWithFallback() {
        return alternateDefaultFeignClient.getData();
    }

    public String fallbackMethod(final Throwable e) {
        return "fallback";
    }

    @Override
    public String getDataWithErrorDecoder() {
        return errorDecoderFeignClient.getData();
    }

    @Override
    public Single<String> getDataWithRx() {
        return defaultFeignClient.getRxData();
    }

}
