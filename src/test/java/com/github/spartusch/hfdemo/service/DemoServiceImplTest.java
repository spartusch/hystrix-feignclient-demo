package com.github.spartusch.hfdemo.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.github.spartusch.hfdemo.TestApplication;
import com.github.spartusch.hfdemo.exception.BusinessRuntimeException;
import com.github.spartusch.hfdemo.exception.TechnicalRuntimeException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import rx.observers.TestSubscriber;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static de.interhyp.spartusch.hfdemo.service.DemoServiceImplTest.HYSTRIX_PERCENTAGE;
import static de.interhyp.spartusch.hfdemo.service.DemoServiceImplTest.HYSTRIX_ROLLINGWINDOW;
import static de.interhyp.spartusch.hfdemo.service.DemoServiceImplTest.HYSTRIX_THRESHOLD;
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

    private HttpStatus stubResponse(final HttpStatus httpStatus) {
        WireMock.reset();
        stubFor(get(urlEqualTo("/")).willReturn(aResponse()
                .withStatus(httpStatus.value())
                .withBody(httpStatus.getReasonPhrase())));
        return httpStatus;
    }

    private void triggerHystrixCircuitOpen(final HttpStatus errorStatus, final Runnable runnable) {
        stubResponse(errorStatus);
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
    public void test_NoFallback() {
        final HttpStatus httpStatus = stubResponse(HttpStatus.OK);
        assertThat(demoService.getData()).isEqualTo(httpStatus.getReasonPhrase());

        triggerHystrixCircuitOpen(HttpStatus.NOT_MODIFIED, () -> {
            final Throwable e = catchThrowable(() -> demoService.getData());
            assertThat(e).isInstanceOf(HystrixRuntimeException.class);
        });

        stubResponse(HttpStatus.OK);
        final Throwable e = catchThrowable(() -> demoService.getData());
        assertThat(e).isInstanceOf(HystrixRuntimeException.class);
    }

    @Test
    public void test_WithFallback() {
        final HttpStatus httpStatus = stubResponse(HttpStatus.OK);
        assertThat(demoService.getDataWithFallback()).isEqualTo(httpStatus.getReasonPhrase());

        triggerHystrixCircuitOpen(HttpStatus.NOT_MODIFIED, () -> {
            assertThat(demoService.getDataWithFallback()).isEqualTo("fallback");
        });

        stubResponse(HttpStatus.OK);
        assertThat(demoService.getDataWithFallback()).isEqualTo("fallback");
    }

    @Test
    public void test_WithErrorDecoder() {
        HttpStatus httpStatus = stubResponse(HttpStatus.OK);
        assertThat(demoService.getDataWithErrorDecoder()).isEqualTo(httpStatus.getReasonPhrase());

        triggerHystrixCircuitOpen(HttpStatus.NOT_MODIFIED, () -> {
            final Throwable e = catchThrowable(() -> demoService.getDataWithErrorDecoder());
            assertThat(e).isInstanceOfAny(BusinessRuntimeException.class, TechnicalRuntimeException.class);
        });

        httpStatus = stubResponse(HttpStatus.OK);
        final String response = demoService.getDataWithErrorDecoder();
        assertThat(response).isEqualTo(httpStatus.getReasonPhrase());
    }

    @Test
    public void test_WithRx() {
        final HttpStatus httpStatus = stubResponse(HttpStatus.OK);
        final TestSubscriber<String> subscriberSucc  = new TestSubscriber<>();
        demoService.getRxData().subscribe(subscriberSucc);
        subscriberSucc.awaitTerminalEvent();
        subscriberSucc.assertNoErrors();
        subscriberSucc.assertValue(httpStatus.getReasonPhrase());

        triggerHystrixCircuitOpen(HttpStatus.NOT_MODIFIED, () -> {
            final TestSubscriber<String> subscriber = new TestSubscriber<>();
            demoService.getRxData().subscribe(subscriber);
            subscriber.awaitTerminalEvent();
            subscriber.assertError(HystrixRuntimeException.class);
        });

        stubResponse(HttpStatus.OK);
        final TestSubscriber<String> subscriberErr  = new TestSubscriber<>();
        demoService.getRxData().subscribe(subscriberErr);
        subscriberErr.awaitTerminalEvent();
        subscriberErr.assertError(HystrixRuntimeException.class);
    }

}
