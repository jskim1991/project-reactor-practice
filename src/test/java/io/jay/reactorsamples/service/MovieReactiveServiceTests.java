package io.jay.reactorsamples.service;

import io.jay.reactorsamples.domain.Movie;
import io.jay.reactorsamples.exception.MovieException;
import io.jay.reactorsamples.exception.NetworkException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class MovieReactiveServiceTests {

    private MovieInfoService movieInfoService;
    private ReviewService reviewService;
    private RevenueService revenueService;
    private MovieReactiveService movieReactiveService;

    @BeforeEach
    void setup() {
        movieInfoService = new MovieInfoService();
        reviewService = new ReviewService();
        revenueService = new RevenueService();
        movieReactiveService = new MovieReactiveService(movieInfoService, reviewService, revenueService);
    }

    @Test
    void getAllMovies() {
        Flux<Movie> moviesFlux = movieReactiveService.getAllMovies().log();


        StepVerifier.create(moviesFlux)
                .assertNext(movie -> {
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                    assertEquals(2, movie.getReviewList().size());
                })
                .assertNext(movie -> {
                    assertEquals("The Dark Knight", movie.getMovieInfo().getName());
                    assertEquals(2, movie.getReviewList().size());
                })
                .assertNext(movie -> {
                    assertEquals("Dark Knight Rises", movie.getMovieInfo().getName());
                    assertEquals(2, movie.getReviewList().size());
                })
                .verifyComplete();
    }

    @Test
    void getAllMovies_throwsMovieException() {
        MovieInfoService mockMovieInfoService = mock(MovieInfoService.class);
        ReviewService mockReviewService = mock(ReviewService.class);
        RevenueService mockRevenueService = mock(RevenueService.class);
        MovieReactiveService service = new MovieReactiveService(mockMovieInfoService, mockReviewService, mockRevenueService);

        when(mockMovieInfoService.retrieveMoviesFlux())
                .thenCallRealMethod();
        when(mockReviewService.retrieveReviewsFlux(anyLong()))
                .thenThrow(new RuntimeException("error"));


        var moviesFlux = service.getAllMovies();


        StepVerifier.create(moviesFlux)
                .expectError(MovieException.class)
                .verify();
    }

    @Test
    void getMovieById() {
        Mono<Movie> movieMono = movieReactiveService.getMovieById(100L).log();


        StepVerifier.create(movieMono)
                .assertNext(movie -> {
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                    assertEquals(2, movie.getReviewList().size());
                })
                .verifyComplete();
    }

    @Test
    void getMovieByIdWithRevenue() {
        Mono<Movie> movieMono = movieReactiveService.getMovieByIdWithRevenue(100L).log();


        StepVerifier.create(movieMono)
                .assertNext(movie -> {
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                    assertEquals(2, movie.getReviewList().size());
                    assertNotNull(movie.getRevenue());
                })
                .verifyComplete();
    }

    @Test
    void getAllMoviesRetry() {
        MovieInfoService mockMovieInfoService = mock(MovieInfoService.class);
        ReviewService mockReviewService = mock(ReviewService.class);
        RevenueService mockRevenueService = mock(RevenueService.class);
        MovieReactiveService service = new MovieReactiveService(mockMovieInfoService, mockReviewService, mockRevenueService);

        when(mockMovieInfoService.retrieveMoviesFlux())
                .thenCallRealMethod();
        when(mockReviewService.retrieveReviewsFlux(anyLong()))
                .thenThrow(new RuntimeException("error"));
        when(mockRevenueService.getRevenue(anyLong()))
                .thenCallRealMethod();


        Flux<Movie> movieFlux = service.getAllMoviesRetry();


        StepVerifier.create(movieFlux)
                .expectErrorMessage("error")
                .verify();
        verify(mockReviewService, times(4)).retrieveReviewsFlux(isA(Long.class));
    }

    @Test
    void getAllMoviesRetryWhen() {
        MovieInfoService mockMovieInfoService = mock(MovieInfoService.class);
        ReviewService mockReviewService = mock(ReviewService.class);
        RevenueService mockRevenueService = mock(RevenueService.class);
        MovieReactiveService service = new MovieReactiveService(mockMovieInfoService, mockReviewService, mockRevenueService);

        when(mockMovieInfoService.retrieveMoviesFlux())
                .thenCallRealMethod();
        when(mockReviewService.retrieveReviewsFlux(anyLong()))
                .thenThrow(new NetworkException("error"));
        when(mockRevenueService.getRevenue(anyLong()))
                .thenCallRealMethod();

        Flux<Movie> movieFlux = service.getAllMoviesRetryWhen();


        StepVerifier.create(movieFlux)
                .expectErrorMessage("error")
                .verify();
        verify(mockReviewService, times(4)).retrieveReviewsFlux(isA(Long.class));
    }

    @Test
    void getAllMoviesRetryWhen_noRetry() {
        MovieInfoService mockMovieInfoService = mock(MovieInfoService.class);
        ReviewService mockReviewService = mock(ReviewService.class);
        RevenueService mockRevenueService = mock(RevenueService.class);
        MovieReactiveService service = new MovieReactiveService(mockMovieInfoService, mockReviewService, mockRevenueService);

        when(mockMovieInfoService.retrieveMoviesFlux())
                .thenCallRealMethod();
        when(mockReviewService.retrieveReviewsFlux(anyLong()))
                .thenThrow(new RuntimeException("error"));
        when(mockRevenueService.getRevenue(anyLong()))
                .thenCallRealMethod();

        Flux<Movie> movieFlux = service.getAllMoviesRetryWhen();


        StepVerifier.create(movieFlux)
                .expectErrorMessage("error")
                .verify();
        verify(mockReviewService, times(1)).retrieveReviewsFlux(isA(Long.class));
    }

    @Test
    void getAllMoviesRepeat() {
        MovieInfoService mockMovieInfoService = mock(MovieInfoService.class);
        ReviewService mockReviewService = mock(ReviewService.class);
        RevenueService mockRevenueService = mock(RevenueService.class);
        MovieReactiveService service = new MovieReactiveService(mockMovieInfoService, mockReviewService, mockRevenueService);

        when(mockMovieInfoService.retrieveMoviesFlux())
                .thenCallRealMethod();
        when(mockReviewService.retrieveReviewsFlux(anyLong()))
                .thenCallRealMethod();
        when(mockRevenueService.getRevenue(anyLong()))
                .thenCallRealMethod();

        Flux<Movie> movieFlux = service.getAllMoviesRepeat();


        StepVerifier.create(movieFlux)
                .expectNextCount(6)
                .thenCancel()
                .verify();
        verify(mockReviewService, times(6)).retrieveReviewsFlux(isA(Long.class));
    }
}