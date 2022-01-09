package io.jay.reactorsamples.service;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class FluxAndMonoProgrammaticGeneratorServiceTests {

    private FluxAndMonoProgrammaticGeneratorService generator = new FluxAndMonoProgrammaticGeneratorService();

    @Test
    void explore_generator() {
        var flux = generator.explore_generator().log();

        StepVerifier.create(flux)
                .expectNext(2, 4, 6, 8, 10, 12, 14, 16, 18, 20)
                .verifyComplete();
    }

    @Test
    void explore_create() {
        var flux = generator.explore_create().log();

        StepVerifier.create(flux)
                .expectNextCount(9)
                .verifyComplete();
    }

    @Test
    void explore_create_mono() {
        var mono = generator.explore_create_mono().log();
        StepVerifier.create(mono)
                .expectNext("success")
                .verifyComplete();
    }

    @Test
    void explore_handle() {
        var flux = generator.explore_handle().log();

        StepVerifier.create(flux)
                .expectNext("ALEX", "CHLOE")
                .verifyComplete();
    }
}
