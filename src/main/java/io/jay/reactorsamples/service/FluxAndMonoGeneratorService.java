package io.jay.reactorsamples.service;

import io.jay.reactorsamples.exception.ReactorException;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

@Log4j2
public class FluxAndMonoGeneratorService {

    public Flux<String> namesFlux() {
        return Flux.fromIterable(List.of("alex", "ben", "chloe")).log();
    }

    public Flux<String> namesFlux_map() {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .map(String::toUpperCase)
                .log();
    }

    public Flux<String> namesFlux_filter(int length) {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .filter(n -> n.length() > length)
                .map(n -> n.length() + "-" + n)
                .log();
    }

    public Flux<String> namesFlux_flatMap(int length) {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .filter(n -> n.length() > length)
                .map(String::toUpperCase)
                .flatMap(s -> splitString(s))
                .log();
    }

    public Flux<String> namesFlux_flatMapAsync(int length) {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .filter(n -> n.length() > length)
                .flatMap(s -> splitStringWithDelay(s))
                .log();
    }

    public Flux<String> namesFlux_concatMap(int length) {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .filter(n -> n.length() > length)
                .map(String::toUpperCase)
                .concatMap(s -> splitStringWithDelay(s))
                .log();
    }

    private Flux<String> splitString(String name) {
        return Flux.fromArray(name.split(""));
    }

    private Flux<String> splitStringWithDelay(String name) {
        return Flux.fromArray(name.split(""))
//                .delayElements(Duration.ofMillis(new Random().nextInt(1000)));
                .delayElements(Duration.ofMillis(100));
    }

    public Flux<String> namesFlux_transform(int length) {
        Function<Flux<String>, Flux<String>> filterFunction = name -> {
            return name.filter(n -> n.length() > length)
                    .map(String::toUpperCase)
                    .flatMap(s -> splitString(s));
        };

        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .transform(filterFunction)
                .log();
    }

    public Mono<String> nameMono() {
        return Mono.just("alex").log();
    }

    public Mono<String> nameMono_map_filter(int length) {
        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > length);
    }

    public Mono<List<String>> nameMono_flatMap(int length) {
        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > length)
                .flatMap(this::splitStringMono).log();
    }

    private Mono<List<String>> splitStringMono(String s) {
        var list = List.of(s.split(""));
        return Mono.just(list);
    }

    public Flux<String> nameMono_flatMapMany(int length) {
        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > length)
                .flatMapMany(this::splitString).log();
    }

    public Flux<String> explore_concat() {
        var abc = Flux.just("A", "B", "C");
        var def = Flux.just("D", "E", "F");
        return Flux.concat(abc, def).log();
    }

    public Flux<String> explore_concatWith() {
        var a = Mono.just("A");
        var b = Mono.just("B");
        return a.concatWith(b).log();
    }

    public Flux<String> explore_merge() {
        var abc = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));
        var def = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));
        return Flux.merge(abc, def).log();
    }

    public Flux<String> explore_mergeWith() {
        var abc = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));
        var def = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));
        return abc.mergeWith(def).log();
    }

    public Flux<String> explore_mergeWith_mono() {
        var a = Flux.just("A");
        var b = Flux.just("B");
        return a.mergeWith(b).log();
    }

    public Flux<String> explore_mergeWithSequential() {
        var abc = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));
        var def = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));
        return Flux.mergeSequential(abc, def).log();
    }

    public Flux<String> explore_zip() {
        var abc = Flux.just("A", "B", "C");
        var def = Flux.just("D", "E", "F");
        return Flux.zip(abc, def, (first, second) -> first + second).log();
    }

    public Flux<String> explore_zipWith() {
        var abc = Flux.just("A", "B", "C");
        var def = Flux.just("D", "E", "F");
        return abc.zipWith(def, (first, second) -> first + second).log();
    }

    public Flux<String> explore_zip_moreThanTwoElements() {
        var abc = Flux.just("A", "B", "C");
        var def = Flux.just("D", "E", "F");
        var oneToThree = Flux.just("1", "2", "3");
        var fourFiveSix = Flux.just("4", "5", "6");
        return Flux.zip(abc, def, oneToThree, fourFiveSix)
                .map(tuple -> tuple.getT1() + tuple.getT2() + tuple.getT3() + tuple.getT4()).log();
    }

    public Mono<String> explore_zip_mono() {
        var a = Mono.just("A");
        var b = Mono.just("B");
        return Mono.zip(a, b)
                .map(t2 -> t2.getT1() + t2.getT2())
                .log();
    }

    public Mono<String> explore_zipWith_mono() {
        var a = Mono.just("A");
        var b = Mono.just("B");
        return a.zipWith(b)
                .map(t2 -> t2.getT1() + t2.getT2())
                .log();
    }

    public Flux<String> exception_flux() {
        return Flux.just("A", "B", "C")
                .concatWith(Flux.error(new RuntimeException("exception occurred")))
                .concatWith(Flux.just("D")).log();
    }

    public Flux<String> explore_onErrorReturn() {
        return Flux.just("A", "B", "C")
                .concatWith(Flux.error(new IllegalStateException("error")))
                .onErrorReturn("D").log();
    }

    public Flux<String> explore_doOnError() {
        return Flux.just("A", "B", "C")
                .concatWith(Flux.error(new IllegalStateException("error")))
                .doOnError(log::error).log();
    }

    public Flux<String> explore_onErrorResume(Exception exception) {

        var recoveryFlux = Flux.just("D", "E", "F");

        return Flux.just("A", "B", "C")
                .concatWith(Flux.error(exception))
                .onErrorResume(e -> {
                    log.error(e.getMessage());
                    if (e instanceof IllegalStateException) {
                        return recoveryFlux;
                    } else {
                        return Flux.error(e);
                    }
                }).log();
    }

    public Flux<String> explore_onErrorContinue() {
        return Flux.just("A", "B", "C")
                .map(name -> {
                    if ("B".equals(name)) {
                        throw new IllegalStateException("exception");
                    }
                    return name;
                })
                .concatWith(Flux.just("D"))
                .onErrorContinue((ex, value) -> {
                    log.error(ex.getMessage());
                    log.warn("The element ran into an exception was: " + value);
                }).log();
    }

    public Flux<String> explore_onErrorMap() {
        return Flux.just("A", "B", "C")
                .map(name -> {
                    if ("B".equals(name)) {
                        throw new IllegalStateException("exception");
                    }
                    return name;
                })
                .concatWith(Flux.just("D"))
                .onErrorMap((e) -> {
                    log.error(e);
                    return new ReactorException(e, e.getMessage());
                }).log();
    }

    public Flux<String> explore_onErrorMap_debug(Exception exception) {
        return Flux.just("A", "B", "C")
                .concatWith(Flux.error(exception))
                .onErrorMap((e) -> {
                    log.error(e);
                    return new ReactorException(e, e.getMessage());
                }).log();
    }

    public Flux<String> explore_onErrorMap_debug_checkpoint(Exception exception) {
        return Flux.just("A", "B", "C")
                .concatWith(Flux.error(exception))
                .checkpoint("error spot here")
                .onErrorMap((e) -> {
                    log.error(e);
                    return new ReactorException(e, e.getMessage());
                }).log();
    }

    public Mono<Object> exception_mono_onErrorMap(Exception e) {
        return Mono.just("B")
                .map(value -> {
                    throw new RuntimeException("Exception Occurred");
                })
                .onErrorMap(ex -> {
                    System.out.println("Exception is " + ex);
                    return new ReactorException(ex, ex.getMessage());
                });
    }

    public Mono<String> exception_mono_onErrorContinue(String input) {
        return Mono.just(input)
                .map(value -> {
                    if ("abc".equals(value)) {
                        throw new RuntimeException("error");
                    } else {
                        return value;
                    }
                })
                .onErrorContinue((ex, s) -> {
                    log.info(ex.getMessage());
                    log.info(s);
                })
                .log();
    }
}
