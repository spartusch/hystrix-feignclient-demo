package com.github.spartusch.hfdemo.service;

import com.github.spartusch.hfdemo.clients.AlternateDefaultFeignClient;
import com.github.spartusch.hfdemo.clients.Decode404FeignClient;
import com.github.spartusch.hfdemo.clients.DefaultFeignClient;
import com.github.spartusch.hfdemo.clients.ErrorDecoderFeignClient;
import rx.Single;

/**
 * A simple service to demonstrate usage of the available feign clients.
 * The only implementation is {@link DemoServiceImpl}.
 *
 * @author Stefan Partusch
 */
public interface DemoService {

    /**
     * Calls {@link DefaultFeignClient#getData()}.
     * @return undefined data
     */
    String getData();

    /**
     * Calls {@link Decode404FeignClient#getData()}.
     * @return undefined data
     */
    String getDataWithDecode404();

    /**
     * Calls {@link AlternateDefaultFeignClient#getData()} and uses a fallback method for error handling.
     * @return undefined data or "fallback" in case of an error
     */
    String getDataWithFallback();

    /**
     * Calls {@link ErrorDecoderFeignClient#getData()}.
     * @return undefined data
     */
    String getDataWithErrorDecoder();

    /**
     * Calls {@link DefaultFeignClient#getRxData()}.
     * @return undefined data wrapped in a {@link Single}.
     */
    Single<String> getDataWithRx();

}
