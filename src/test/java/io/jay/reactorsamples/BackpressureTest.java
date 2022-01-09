package io.jay.reactorsamples;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
public class BackpressureTest {

    @Test
    void test_backpressure() {
        var numbersFlux = Flux.range(1, 100).log();
        numbersFlux
                .subscribe(new BaseSubscriber<Integer>() {
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        request(2);
                    }

                    @Override
                    protected void hookOnNext(Integer value) {
                        // super.hookOnNext(value);
                        log.info("hookOnNext: {}", value);
                        if (value == 2) {
                            cancel();
                        }
                    }

                    @Override
                    protected void hookOnComplete() {
                        // super.hookOnComplete();
                    }

                    @Override
                    protected void hookOnError(Throwable throwable) {
                        // super.hookOnError(throwable);
                    }

                    @Override
                    protected void hookOnCancel() {
                        // super.hookOnCancel();
                        log.info("hookOnCancel");
                    }
                });
    }

    @Test
    void test_backpressure_usingLatch() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);

        var numbersFlux = Flux.range(1, 100).log();
        numbersFlux
                .subscribe(new BaseSubscriber<Integer>() {
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        request(2);
                    }

                    @Override
                    protected void hookOnNext(Integer value) {
                        // super.hookOnNext(value);
                        log.info("hookOnNext: {}", value);
                        if (value%2 == 0 || value < 50) {
                            request(2);
                        } else {
                            cancel();
                        }
                    }

                    @Override
                    protected void hookOnComplete() {
                        // super.hookOnComplete();
                    }

                    @Override
                    protected void hookOnError(Throwable throwable) {
                        // super.hookOnError(throwable);
                    }

                    @Override
                    protected void hookOnCancel() {
                        // super.hookOnCancel();
                        log.info("hookOnCancel");
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(5L,  TimeUnit.SECONDS));
    }

    @Test
    void test_backpressure_onBackPressureDrop() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);

        var numbersFlux = Flux.range(1, 100).log();
        numbersFlux
                .onBackpressureDrop(item -> {
                    log.info("Dropped item is {}", item);
                })
                .subscribe(new BaseSubscriber<Integer>() {
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        request(2);
                    }

                    @Override
                    protected void hookOnNext(Integer value) {
                        // super.hookOnNext(value);
                        log.info("hookOnNext: {}", value);
                        if (value == 2) {
                            hookOnCancel();
                        }
                    }

                    @Override
                    protected void hookOnComplete() {
                        // super.hookOnComplete();
                    }

                    @Override
                    protected void hookOnError(Throwable throwable) {
                        // super.hookOnError(throwable);
                    }

                    @Override
                    protected void hookOnCancel() {
                        // super.hookOnCancel();
                        log.info("hookOnCancel");
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(5L,  TimeUnit.SECONDS));
    }

    @Test
    void test_backpressure_onBackPressureBuffer() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);

        var numbersFlux = Flux.range(1, 100).log();
        numbersFlux
                .onBackpressureBuffer(10, i -> {
                    log.info("Last buffered element is: {}", i);
                })
                .subscribe(new BaseSubscriber<Integer>() {
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        request(1);
                    }

                    @Override
                    protected void hookOnNext(Integer value) {
                        // super.hookOnNext(value);
                        log.info("hookOnNext: {}", value);
                        if (value < 50) {
                            request(1);
                        } else {
                            hookOnCancel();
                        }
                    }

                    @Override
                    protected void hookOnComplete() {
                        // super.hookOnComplete();
                    }

                    @Override
                    protected void hookOnError(Throwable throwable) {
                        // super.hookOnError(throwable);
                    }

                    @Override
                    protected void hookOnCancel() {
                        // super.hookOnCancel();
                        log.info("hookOnCancel");
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(5L,  TimeUnit.SECONDS));
    }

    @Test
    void test_backpressure_onBackPressureError() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);

        var numbersFlux = Flux.range(1, 100).log();
        numbersFlux
                .onBackpressureError()
                .subscribe(new BaseSubscriber<Integer>() {
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        request(1);
                    }

                    @Override
                    protected void hookOnNext(Integer value) {
                        // super.hookOnNext(value);
                        log.info("hookOnNext: {}", value);
                        if (value < 50) {
                            request(1);
                        } else {
                            hookOnCancel();
                        }
                    }

                    @Override
                    protected void hookOnComplete() {
                        // super.hookOnComplete();
                    }

                    @Override
                    protected void hookOnError(Throwable throwable) {
                        // super.hookOnError(throwable);
                        log.error(throwable);
                    }

                    @Override
                    protected void hookOnCancel() {
                        // super.hookOnCancel();
                        log.info("hookOnCancel");
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(5L,  TimeUnit.SECONDS));
    }
}
