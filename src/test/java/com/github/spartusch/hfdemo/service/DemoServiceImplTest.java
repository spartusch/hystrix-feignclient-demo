package com.github.spartusch.hfdemo.service;

import com.github.spartusch.hfdemo.TestApplication;
import com.github.spartusch.hfdemo.exception.BusinessRuntimeException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import rx.observers.TestSubscriber;

import static com.github.spartusch.hfdemo.service.DemoServiceImplTest.HYSTRIX_PERCENTAGE;
import static com.github.spartusch.hfdemo.service.DemoServiceImplTest.HYSTRIX_ROLLINGWINDOW;
import static com.github.spartusch.hfdemo.service.DemoServiceImplTest.HYSTRIX_THRESHOLD;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.catchThrowable;

@SpringBootTest(classes = TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "clients.url=http://localhost:8080",
                "feign.hystrix.enabled=true",
                "hystrix.command.default.circuitBreaker.requestVolumeThreshold=" + HYSTRIX_THRESHOLD, // default: 20
                "hystrix.command.default.circuitBreaker.errorThresholdPercentage=" + HYSTRIX_PERCENTAGE, // default: 50
                "metrics.rollingStats.timeInMilliseconds=" + HYSTRIX_ROLLINGWINDOW // default: 10000
        }
)
@AutoConfigureWireMock
@RunWith(SpringRunner.class)
public class DemoServiceImplTest {

    static final int HYSTRIX_THRESHOLD = 10;
    static final int HYSTRIX_PERCENTAGE = 5;
    static final int HYSTRIX_ROLLINGWINDOW = 250;

    @Autowired
    private DemoService demoService;

    private void stubResponse(final HttpStatus httpStatus) {
        WireMock.reset();
        stubFor(get(urlEqualTo("/")).willReturn(aResponse()
                .withStatus(httpStatus.value())
                .withBody(httpStatus.getReasonPhrase())));
    }

    private void runRepeatedly(final Runnable runnable) {
        final int numberOfErrorRequests = HYSTRIX_THRESHOLD + 1;
        for (int i = numberOfErrorRequests; i > 0; i--) {
            try {
                runnable.run();
                Thread.sleep((HYSTRIX_ROLLINGWINDOW - 100 / (numberOfErrorRequests + 2)));
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    public void test_getData() {
        stubResponse(HttpStatus.OK);
        assertThat(demoService.getData()).isEqualTo(HttpStatus.OK.getReasonPhrase());

        stubResponse(HttpStatus.NOT_FOUND);
        runRepeatedly(() -> {
            final Throwable e = catchThrowable(() -> demoService.getData());
            assertThat(e).isInstanceOf(HystrixRuntimeException.class);
        });

        stubResponse(HttpStatus.OK);
        final Throwable e = catchThrowable(() -> demoService.getData());
        assertThat(e).isInstanceOf(HystrixRuntimeException.class);
    }

    @Test
    public void test_getDataWithDecode404() {
        stubResponse(HttpStatus.OK);
        assertThat(demoService.getDataWithDecode404()).isEqualTo(HttpStatus.OK.getReasonPhrase());

        stubResponse(HttpStatus.NOT_FOUND);
        runRepeatedly(() -> {
            assertThat(demoService.getDataWithDecode404()).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
        });

        stubResponse(HttpStatus.OK);
        assertThat(demoService.getDataWithDecode404()).isEqualTo(HttpStatus.OK.getReasonPhrase());
    }

    @Test
    public void test_getDataWithFallback() {
        stubResponse(HttpStatus.OK);
        assertThat(demoService.getDataWithFallback()).isEqualTo(HttpStatus.OK.getReasonPhrase());

        stubResponse(HttpStatus.NOT_MODIFIED);
        runRepeatedly(() -> {
            assertThat(demoService.getDataWithFallback()).isEqualTo("fallback");
        });

        stubResponse(HttpStatus.OK);
        assertThat(demoService.getDataWithFallback()).isEqualTo("fallback");
    }

    @Test
    public void test_getDataWithErrorDecoder_BusinessRuntimeException() {
        stubResponse(HttpStatus.OK);
        assertThat(demoService.getDataWithErrorDecoder()).isEqualTo(HttpStatus.OK.getReasonPhrase());

        stubResponse(HttpStatus.NOT_MODIFIED);
        runRepeatedly(() -> {
            final Throwable e = catchThrowable(() -> demoService.getDataWithErrorDecoder());
            assertThat(e).isInstanceOf(BusinessRuntimeException.class);
        });

        stubResponse(HttpStatus.OK);
        final String response = demoService.getDataWithErrorDecoder();
        assertThat(response).isEqualTo(HttpStatus.OK.getReasonPhrase());
    }

    @Test
    public void test_getDataWithErrorDecoder_TechnicalRuntimeException() {
        stubResponse(HttpStatus.OK);
        assertThat(demoService.getDataWithErrorDecoder()).isEqualTo(HttpStatus.OK.getReasonPhrase());

        stubResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        runRepeatedly(() -> {
            final Throwable e = catchThrowable(() -> demoService.getDataWithErrorDecoder());
            assertThat(e).isInstanceOf(HystrixRuntimeException.class);
        });

        stubResponse(HttpStatus.OK);
        final Throwable e = catchThrowable(() -> demoService.getDataWithErrorDecoder());
        assertThat(e).isInstanceOf(HystrixRuntimeException.class);
    }

    @Test
    public void test_getDataWithRx() {
        stubResponse(HttpStatus.OK);
        final TestSubscriber<String> subscriberSucc  = new TestSubscriber<>();
        demoService.getDataWithRx().subscribe(subscriberSucc);
        subscriberSucc.awaitTerminalEvent();
        subscriberSucc.assertNoErrors();
        subscriberSucc.assertValue(HttpStatus.OK.getReasonPhrase());

        stubResponse(HttpStatus.NOT_MODIFIED);
        runRepeatedly(() -> {
            final TestSubscriber<String> subscriber = new TestSubscriber<>();
            demoService.getDataWithRx().subscribe(subscriber);
            subscriber.awaitTerminalEvent();
            subscriber.assertError(HystrixRuntimeException.class);
        });

        stubResponse(HttpStatus.OK);
        final TestSubscriber<String> subscriberErr  = new TestSubscriber<>();
        demoService.getDataWithRx().subscribe(subscriberErr);
        subscriberErr.awaitTerminalEvent();
        subscriberErr.assertError(HystrixRuntimeException.class);
    }

}
