package com.github.spartusch.hfdemo.clients;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * A feign client with decoding for HTTP status code 404 enabled.
 *
 * @author Stefan Partusch
 */
@FeignClient(
        name = "decode404FeignClient",
        url = "${clients.url}",
        decode404 = true
)
public interface Decode404FeignClient {

    @RequestMapping(method = RequestMethod.GET)
    String getData();

}
