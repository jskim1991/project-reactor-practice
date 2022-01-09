package io.jay.reactorsamples.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

public class MovieReactiveServiceWebClientTests {
    private WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:8080/movies")
            .build();
    private MovieInfoService movieInfoService = new MovieInfoService(webClient);
    private ReviewService reviewService = new ReviewService(webClient);
    private MovieReactiveService movieReactiveService = new MovieReactiveService(movieInfoService, reviewService);

    @Test
    void getMovieByIdUsingWebClient() {
        var movie = movieReactiveService.getMovieByIdUsingWebClient(1L);

        StepVerifier.create(movie)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getAllMoviesUsingWebClient() {
        var movies = movieReactiveService.getAllMoviesUsingWebClient();

        StepVerifier.create(movies)
                .expectNextCount(7)
                .verifyComplete();
    }
}
