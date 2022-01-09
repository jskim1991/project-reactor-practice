package io.jay.reactorsamples;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static io.jay.reactorsamples.util.CommonUtil.delay;

@Log4j2
public class ColdAndHotPublisherTest {

    @Test
    void coldPublisherTest() {
        var flux = Flux.range(1, 10);

        flux.subscribe(i -> log.info("Subscriber 1: {}", i));
        flux.subscribe(i -> log.info("Subscriber 2: {}", i));
    }

    @Test
    void hotPublisherTest() {
        var flux = Flux.range(1, 10)
                .delayElements(Duration.ofMillis(100));

        ConnectableFlux<Integer> connectableFlux = flux.publish();
        connectableFlux.connect();

        connectableFlux.subscribe(i -> log.info("Subscriber 1: {}", i));

        delay(500);
        connectableFlux.subscribe(i -> log.info("Subscriber 2: {}", i));

        delay(1000);
    }

    /**
     * autoConnect: waits for minimum number of subscribers (tracks incoming subscriptions)
     * refCount: tracks incoming subscriptions & stop emits when subscriptions are cancelled
     */
    @Test
    void hotPublisherTest_autoConnect() {
        var flux = Flux.range(1, 10)
                .delayElements(Duration.ofMillis(100));

        Flux<Integer> hotFlux = flux.publish().autoConnect(2);

        var firstDisposable = hotFlux.subscribe(i -> log.info("Subscriber 1: {}", i));

        delay(200);
        var secondDisposable = hotFlux.subscribe(i -> log.info("Subscriber 2: {}", i));

        log.info("Two subscribers are connected");

        delay(200);
        hotFlux.subscribe(i -> log.info("Subscriber 3: {}", i));

        delay(200);
        firstDisposable.dispose();
        secondDisposable.dispose();


        delay(1000);
    }

    @Test
    void hotPublisherTest_refCount() {
        var flux = Flux.range(1, 10)
                .delayElements(Duration.ofMillis(100))
                .doOnCancel(() -> {
                    log.info("Received cancel()");
                });

        Flux<Integer> hotFlux = flux.publish().refCount(2);

        var firstDisposable = hotFlux.subscribe(i -> log.info("Subscriber 1: {}", i));

        delay(200);
        var secondDisposable = hotFlux.subscribe(i -> log.info("Subscriber 2: {}", i));

        log.info("Two subscribers are connected");

        delay(200);
        firstDisposable.dispose();
        secondDisposable.dispose();
        hotFlux.subscribe(i -> log.info("Subscriber 3: {}", i));

        delay(200);
        hotFlux.subscribe(i -> log.info("Subscriber 4: {}", i));

        delay(1000);
    }
}
