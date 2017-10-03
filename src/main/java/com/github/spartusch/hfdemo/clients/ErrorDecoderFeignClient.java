package com.github.spartusch.hfdemo.clients;

import com.github.spartusch.hfdemo.exception.DemoErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * A feign client configured to use {@link DemoErrorDecoder}.
 */
@FeignClient(
        name = "errorDecoderFeignClient",
        url = "${clients.url}",
        configuration = ErrorDecoderFeignClient.Configuration.class
)
public interface ErrorDecoderFeignClient {

    class Configuration {
        @Bean
        public ErrorDecoder errorDecoder() {
            return new DemoErrorDecoder();
        }
    }

    @RequestMapping(method = RequestMethod.GET)
    String getData();

}
