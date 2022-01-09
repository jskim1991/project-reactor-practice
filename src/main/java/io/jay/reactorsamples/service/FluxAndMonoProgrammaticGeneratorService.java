package io.jay.reactorsamples.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.jay.reactorsamples.util.CommonUtil.delay;

public class FluxAndMonoProgrammaticGeneratorService {

    public static List<String> names() {
        delay(1000);
        return List.of("alex", "ben", "chloe");
    }

    public Flux<Integer> explore_generator() {
        return Flux.generate(() -> 1, (state, sink) -> {
            sink.next(state * 2);

            if (state == 10) {
                sink.complete();
            }
            return state + 1;
        });
    }

    /**
     * async and multi-threaded
     */
    public Flux<String> explore_create() {
        return Flux.create(sink -> {
//            names().forEach(sink::next);
//            sink.complete();
            CompletableFuture
                    .supplyAsync(() -> names())
                    .thenAccept(names -> {
                        names.forEach(name -> {
                            /* multiple emits allowed in create()*/
                            sink.next(name);
                            sink.next(name);
                        });
                    })
                    .thenRun(() -> sendEvents(sink));
        });
    }

    public void sendEvents(FluxSink<String> sink) {
        CompletableFuture
                .supplyAsync(() -> names())
                .thenAccept(names -> {
                    names.forEach(sink::next);
                })
                .thenRun(sink::complete);
    }

    public Mono<String> explore_create_mono() {
        return Mono.create(monoSink -> {
            monoSink.success("success");
        });
    }

    public Flux<String> explore_handle() {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .handle((name, sink) -> {
                    if (name.length() > 3) {
                        sink.next(name.toUpperCase());
                    }
                });
    }
}
