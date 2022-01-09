package io.jay.reactorsamples.service;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;

import java.util.List;

import static io.jay.reactorsamples.util.CommonUtil.delay;

@Log4j2
public class FluxAndMonoSchedulersService {

    static List<String> namesList = List.of("alex", "ben", "chloe");
    static List<String> namesList1 = List.of("adam", "jill", "jack");

    public Flux<String> explore_publishOn() {
        var namesFlux = Flux.fromIterable(namesList)
                .publishOn(Schedulers.parallel())
                .map(this::upperCase)
                .log();
        var namesFlux1 = Flux.fromIterable(namesList1)
                .publishOn(Schedulers.boundedElastic())
                .map(this::upperCase)
                .map(s -> {
                    log.info("Name is : {}", s);
                    return s;
                })
                .log();

        return namesFlux.mergeWith(namesFlux1);
    }

    public Flux<String> explore_subscribeOn() {
        var namesFlux = blockingLibraryCode(namesList)
                .subscribeOn(Schedulers.boundedElastic())
                .log();
        var namesFlux1 = blockingLibraryCode(namesList1)
                .subscribeOn(Schedulers.boundedElastic())
                .map(s -> {
                    log.info("Name is : {}", s);
                    return s;
                })
                .log();

        return namesFlux.mergeWith(namesFlux1);
    }

    private Flux<String> blockingLibraryCode(List<String> namesList) {
        return Flux.fromIterable(namesList)
                .map(this::upperCase);
    }

    private String upperCase(String name) {
        delay(1000);
        return name.toUpperCase();
    }

    public ParallelFlux<String> explore_parallel() {
        return Flux.fromIterable(namesList)
                .parallel()
                .runOn(Schedulers.parallel())
                .map(this::upperCase)
                .log();
    }

    public Flux<String> explore_parallel_flatMap() {
        var namesFlux = Flux.fromIterable(namesList)
                .flatMap(names -> {
                    return Mono.just(names)
                            .map(this::upperCase)
                            .subscribeOn(Schedulers.parallel());
                })
                .log();

        var namesFlux1 = Flux.fromIterable(namesList1)
                .flatMap(names -> {
                    return Mono.just(names)
                            .map(this::upperCase)
                            .subscribeOn(Schedulers.parallel());
                })
                .log();

        return namesFlux.mergeWith(namesFlux1);
    }

    public Flux<String> explore_parallel_flatMapSequential() {
        var namesFlux = Flux.fromIterable(namesList)
                .flatMapSequential(names -> {
                    return Mono.just(names)
                            .map(this::upperCase)
                            .subscribeOn(Schedulers.parallel());
                })
                .log();

        return namesFlux;
    }

}
