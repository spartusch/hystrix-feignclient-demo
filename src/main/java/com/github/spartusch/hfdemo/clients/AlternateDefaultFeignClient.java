package com.github.spartusch.hfdemo.clients;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * An alternate feign client with default configuration.
 * This client has its own Hystrix state, independent from {@link DefaultFeignClient}'s state.
 */
@FeignClient(
        name = "alternateDefaultFeignClient",
        url = "${clients.url}"
)
public interface AlternateDefaultFeignClient {

    @RequestMapping(method = RequestMethod.GET)
    String getData();

}
