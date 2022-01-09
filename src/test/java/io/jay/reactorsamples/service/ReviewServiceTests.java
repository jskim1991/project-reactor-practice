package io.jay.reactorsamples.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

public class ReviewServiceTests {

    private WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:8080/movies")
            .build();
    private ReviewService reviewService = new ReviewService(webClient);

    @Test
    void retrieveReviewsUsingWebClient() {
        var flux = reviewService.retrieveReviewsUsingWebClient(1L);

        StepVerifier.create(flux)
                .expectNextCount(1)
                .verifyComplete();
    }
}
