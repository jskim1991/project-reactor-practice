package io.jay.reactorsamples.service;

import io.jay.reactorsamples.exception.ReactorException;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;
import reactor.tools.agent.ReactorDebugAgent;

import java.time.Duration;
import java.util.List;

public class FluxAndMonoGeneratorServiceTests {

    FluxAndMonoGeneratorService generator = new FluxAndMonoGeneratorService();

    @Test
    void namesFlux() {
        StepVerifier.create(generator.namesFlux())
                .expectNext("alex", "ben", "chloe")
                .verifyComplete();
    }

    @Test
    void nameMono() {
        StepVerifier.create(generator.nameMono())
                .expectNext("alex")
                .verifyComplete();
    }

    @Test
    void namesFlux_map() {
        StepVerifier.create(generator.namesFlux_map())
                .expectNext("ALEX", "BEN", "CHLOE")
                .verifyComplete();
    }

    @Test
    void namesFlux_filter() {
        StepVerifier.create(generator.namesFlux_filter(3))
                .expectNext("4-alex", "5-chloe")
                .verifyComplete();
    }

    @Test
    void namesFlux_flatMap() {
        StepVerifier.create(generator.namesFlux_flatMap(3))
                .expectNext("A", "L", "E", "X", "C", "H", "L", "O", "E")
                .verifyComplete();
    }

    /**
     * flatMap() is used for asynchronous transformations
     * Subscribes to Mono or Flux then flattens and sends it downstream
     */
    @Test
    void namesFlux_flatMapAsync() {
         StepVerifier.create(generator.namesFlux_flatMapAsync(3))
                .expectNextCount(9)
                .verifyComplete();
    }

    /**
     * concatMap preserves order
     * Takes longer than flatMap because it waits
     */
    @Test
    void namesFlux_concatMap() {
        StepVerifier.create(generator.namesFlux_concatMap(3))
                .expectNext("A", "L", "E", "X", "C", "H", "L", "O", "E")
                .verifyComplete();
    }

    @Test
    void namesFlux_concatMap_virtualTimer() {
        VirtualTimeScheduler.getOrSet();

        StepVerifier.withVirtualTime(() -> generator.namesFlux_concatMap(3))
                .thenAwait(Duration.ofSeconds(1))
                .expectNext("A", "L", "E", "X", "C", "H", "L", "O", "E")
                .verifyComplete();
    }

    /**
     * flatMap is used to take Mono and transform into another Mono
     * Using map() would result in Mono<Mono<T>>
     */
    @Test
    void nameMono_flatMap() {

        StepVerifier.create(generator.nameMono_flatMap(3))
                .expectNext(List.of("A", "L", "E", "X"))
                .verifyComplete();
    }

    /**
     * flatMapMany takes a Mono and returns Flux
     */
    @Test
    void nameMono_flatMapMany() {
        StepVerifier.create(generator.nameMono_flatMapMany(3))
                .expectNext("A", "L", "E", "X")
                .verifyComplete();
    }

    /**
     * transform allows extracting a common functionality for reuse
     */
    @Test
    void namesFlux_transform() {
        StepVerifier.create(generator.namesFlux_transform(3))
                .expectNext("A", "L", "E", "X", "C", "H", "L", "O", "E")
                .verifyComplete();
    }

    @Test
    void namesFlux_transform_defaultIfEmpty() {
        StepVerifier.create(generator.namesFlux_transform(6)
                        .defaultIfEmpty("default value"))
                .expectNext("default value")
                .verifyComplete();
    }

    @Test
    void namesFlux_transform_switchIfEmpty() {
        Flux<String> defaultFlux = Flux.just("default")
                .flatMap(s -> Flux.fromArray(s.split(""))).log();
        StepVerifier.create(generator.namesFlux_transform(6)
                        .switchIfEmpty(defaultFlux))
                .expectNext("d", "e", "f" ,"a", "u", "l", "t")
                .verifyComplete();
    }

    @Test
    void explore_concat() {
        StepVerifier.create(generator.explore_concat())
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();
    }

    @Test
    void explore_concatWith() {
        StepVerifier.create(generator.explore_concatWith())
                .expectNext("A", "B")
                .verifyComplete();
    }

    /**
     * merge vs concat
     * merge: both publishers are subscribed at the same time eagerly
     * concat: subscribes to the publishers in sequence
     */
    @Test
    void explore_merge() {
        StepVerifier.create(generator.explore_merge())
                .expectNext("A", "D", "B", "E", "C", "F")
                .verifyComplete();
    }

    @Test
    void explore_mergeWith() {
        StepVerifier.create(generator.explore_mergeWith())
                .expectNext("A", "D", "B", "E", "C", "F")
                .verifyComplete();
        StepVerifier.create(generator.explore_mergeWith_mono())
                .expectNext("A", "B")
                .verifyComplete();
    }

    @Test
    void explore_mergeWithSequential() {
        StepVerifier.create(generator.explore_mergeWithSequential())
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();
    }

    /**
     * zip: zips 2~8 publishers into one
     * publishers are subscribed eagerly
     * zipWith: zips two publishers into one
     */
    @Test
    void explore_zip() {
        StepVerifier.create(generator.explore_zip())
                .expectNext("AD", "BE", "CF")
                .verifyComplete();
    }

    @Test
    void explore_zipWith() {
        StepVerifier.create(generator.explore_zipWith())
                .expectNext("AD", "BE", "CF")
                .verifyComplete();
    }

    @Test
    void explore_zip_moreThanTwoElements() {
        StepVerifier.create(generator.explore_zip_moreThanTwoElements())
                .expectNext("AD14", "BE25", "CF36")
                .verifyComplete();
    }

    @Test
    void explore_zip_mono() {
        StepVerifier.create(generator.explore_zip_mono())
                .expectNext("AB")
                .verifyComplete();
    }

    @Test
    void explore_zipWith_mono() {
        StepVerifier.create(generator.explore_zipWith_mono())
                .expectNext("AB")
                .verifyComplete();
    }

    @Test
    void exception_flux() {
        StepVerifier.create(generator.exception_flux())
                .expectNext("A", "B", "C")
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void exception_flux_1() {
        StepVerifier.create(generator.exception_flux())
                .expectNext("A", "B", "C")
                .expectError()
                .verify();
    }

    @Test
    void exception_flux_2() {
        StepVerifier.create(generator.exception_flux())
                .expectNext("A", "B", "C")
                .expectErrorMessage("exception occurred")
                .verify();
    }

    @Test
    void explore_onErrorReturn() {
        StepVerifier.create(generator.explore_onErrorReturn())
                .expectNext("A", "B", "C", "D")
                .verifyComplete();
    }

    @Test
    void explore_doOnError() {
        StepVerifier.create(generator.explore_doOnError())
                .expectNext("A", "B", "C")
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void explore_onErrorResume() {
        StepVerifier.create(generator.explore_onErrorResume(new IllegalStateException("not valid")))
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();


        StepVerifier.create(generator.explore_onErrorResume(new RuntimeException("error")))
                .expectNext("A", "B", "C")
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void explore_onErrorContinue() {
        StepVerifier.create(generator.explore_onErrorContinue())
                .expectNext("A", "C", "D")
                .verifyComplete();
    }

    @Test
    void explore_onErrorMap() {
        StepVerifier.create(generator.explore_onErrorMap())
                .expectNext("A")
                .expectError(ReactorException.class)
                .verify();
    }

    @Test
    void explore_onErrorMap_onOperatorDebug() {
        /* not recommended for prod due to performance overhead */
        Hooks.onOperatorDebug();
        StepVerifier.create(generator.explore_onErrorMap_debug(new RuntimeException("error message")))
                .expectNext("A", "B", "C")
                .expectError(ReactorException.class)
                .verify();
    }
    @Test
    void explore_onErrorMap_checkpointDebug() {
        StepVerifier.create(generator.explore_onErrorMap_debug_checkpoint(new RuntimeException("error message")))
                .expectNext("A", "B", "C")
                .expectError(ReactorException.class)
                .verify();
    }
    @Test
    void explore_onErrorMap_reactorDebugAgent() {
        /* no dependency overhead */
        ReactorDebugAgent.init();
        ReactorDebugAgent.processExistingClasses();
        StepVerifier.create(generator.explore_onErrorMap_debug(new RuntimeException("error message")))
                .expectNext("A", "B", "C")
                .expectError(ReactorException.class)
                .verify();
    }

    @Test
    void exception_mono_onErrorContinue() {
        StepVerifier.create(generator.exception_mono_onErrorContinue("abc"))
                .verifyComplete();

        StepVerifier.create(generator.exception_mono_onErrorContinue("reactor"))
                .expectNext("reactor")
                .verifyComplete();
    }
}
