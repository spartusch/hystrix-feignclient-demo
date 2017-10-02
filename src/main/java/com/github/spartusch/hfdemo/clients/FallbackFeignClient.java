package com.github.spartusch.hfdemo.clients;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(
        name = "defaultFeignClient",
        url = "${clients.url}"
)
public interface FallbackFeignClient {

    @RequestMapping(method = RequestMethod.GET)
    String getData();

}
