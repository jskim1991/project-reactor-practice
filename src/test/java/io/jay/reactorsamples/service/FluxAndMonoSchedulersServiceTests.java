package io.jay.reactorsamples.service;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

public class FluxAndMonoSchedulersServiceTests {

    FluxAndMonoSchedulersService service = new FluxAndMonoSchedulersService();

    @Test
    void explore_publishOn() {
        var flux = service.explore_publishOn();

        StepVerifier.create(flux)
                .expectNextCount(6)
                .verifyComplete();
    }

    @Test
    void explore_subscribeOn() {
        var flux = service.explore_subscribeOn();

        StepVerifier.create(flux)
                .expectNextCount(6)
                .verifyComplete();
    }

    /**
     * parallelism can be achieved by
     * 1. use .parallel() and runOn() -> returns ParallelFlux
     * 2. flatMap() -> can use more library functions than ParallelFlux
     * 3. flatMapSequential() -> returns elements in order that they were constructed
     */
    @Test
    void explore_parallel() {
        var flux = service.explore_parallel();

        StepVerifier.create(flux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void explore_parallel_flatMap() {
        var flux = service.explore_parallel_flatMap();

        StepVerifier.create(flux)
                .expectNextCount(6)
                .verifyComplete();
    }

    @Test
    void explore_parallel_flatMapSequential() {
        var flux = service.explore_parallel_flatMapSequential();

        StepVerifier.create(flux)
                .expectNext("ALEX", "BEN", "CHLOE")
                .verifyComplete();
    }
}
