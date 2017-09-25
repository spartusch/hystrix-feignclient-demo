package com.github.spartusch.hfdemo.service;

import com.github.spartusch.hfdemo.TestApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "clients.url=http://localhost:8080",
                "feign.hystrix.enabled=true"
        }
)
@AutoConfigureWireMock
@RunWith(SpringRunner.class)
public class DemoServiceImplTest {

    @Autowired
    private DemoService demoService;

    private HttpStatus httpStatus = HttpStatus.NOT_FOUND;

    @Before
    public void setUp() {
        stubFor(get(urlEqualTo("/")).willReturn(aResponse()
                .withStatus(httpStatus.value())
                .withBody("Test data")));
    }

    @Test
    public void testDefaultFeignClient() {
        assertThat(demoService.getDataUsingDefaultFeignClient()).isEqualTo("Test data");
    }

    @Test
    public void testDefaultFeignClientWithFallback() {
        assertThat(demoService.getDataUsingDefaultFeignClientWithFallback()).isEqualTo("Test data");
    }

    @Test
    public void testDecode404FeignClient() {
        assertThat(demoService.getDataUsingDecode404FeignClient()).isEqualTo("Test data");
    }

}
