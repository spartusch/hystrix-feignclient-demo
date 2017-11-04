package com.github.spartusch.hfdemo.clients;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import rx.Single;

/**
 * A feign client with default configuration.
 *
 * @author Stefan Partusch
 */
@FeignClient(
        name = "defaultFeignClient",
        url = "${clients.url}")
public interface DefaultFeignClient {

    @RequestMapping(method = RequestMethod.GET)
    String getData();

    @RequestMapping(method = RequestMethod.GET)
    Single<String> getRxData();

}
