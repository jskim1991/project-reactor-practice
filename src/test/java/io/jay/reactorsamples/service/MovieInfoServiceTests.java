package io.jay.reactorsamples.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MovieInfoServiceTests {

    WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:8080/movies")
            .build();

    MovieInfoService movieInfoService = new MovieInfoService(webClient);

    @Test
    void retrieveAllMovieInfoUsingWebClient() {
        var movieInfoFlux = movieInfoService.retrieveAllMovieInfoUsingWebClient();

        StepVerifier.create(movieInfoFlux.log())
                .expectNextCount(7)
                .verifyComplete();
    }

    @Test
    void retrieveMovieInfoUsingWebClient() {
        var movieInfoMono = movieInfoService.retrieveMovieInfoUsingWebClient(1L);

        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> {
                    assertEquals(1, movieInfo.getMovieInfoId());
                    assertEquals("Batman Begins", movieInfo.getName());
                    assertEquals(2005, movieInfo.getYear());
                    assertEquals(2, movieInfo.getCast().size());
                    assertEquals(LocalDate.of(2005, 6, 15), movieInfo.getRelease_date());
                })
                .verifyComplete();
    }
}
