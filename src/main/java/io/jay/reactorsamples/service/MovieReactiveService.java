package io.jay.reactorsamples.service;

import io.jay.reactorsamples.domain.Movie;
import io.jay.reactorsamples.domain.MovieInfo;
import io.jay.reactorsamples.domain.Revenue;
import io.jay.reactorsamples.domain.Review;
import io.jay.reactorsamples.exception.MovieException;
import io.jay.reactorsamples.exception.NetworkException;
import io.jay.reactorsamples.exception.ServiceException;
import lombok.extern.log4j.Log4j2;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Log4j2
public class MovieReactiveService {

    private MovieInfoService movieInfoService;
    private ReviewService reviewService;
    private RevenueService revenueService;

    public MovieReactiveService(MovieInfoService movieInfoService, ReviewService reviewService) {
        this.movieInfoService = movieInfoService;
        this.reviewService = reviewService;
    }

    public MovieReactiveService(MovieInfoService movieInfoService, ReviewService reviewService, RevenueService revenueService) {
        this.movieInfoService = movieInfoService;
        this.reviewService = reviewService;
        this.revenueService = revenueService;
    }

    public Flux<Movie> getAllMovies() {
        Flux<MovieInfo> moviesInfoFlux = movieInfoService.retrieveMoviesFlux();
        return moviesInfoFlux
                .flatMap(movieInfo -> {
                    Mono<List<Review>> reviewsMono = reviewService.retrieveReviewsFlux(movieInfo.getMovieInfoId())
                            .collectList();
                    return reviewsMono.map(list -> new Movie(movieInfo, list));
                })
                .onErrorMap(ex -> {
                    log.error(ex.getMessage());
                    throw new MovieException(ex.getMessage());
                })
                .log();
    }

    public Mono<Movie> getMovieByIdUsingWebClient(Long movieId) {
        Mono<MovieInfo> movieInfoMono = movieInfoService.retrieveMovieInfoUsingWebClient(movieId);
        Mono<List<Review>> reviewList = reviewService.retrieveReviewsUsingWebClient(movieId)
                .collectList();
        return movieInfoMono.zipWith(reviewList, (movieInfo, reviews) -> new Movie(movieInfo, reviews));
    }

    public Flux<Movie> getAllMoviesUsingWebClient() {
        Flux<MovieInfo> moviesInfoFlux = movieInfoService.retrieveAllMovieInfoUsingWebClient();
        return moviesInfoFlux
                .flatMap(movieInfo -> {
                    Mono<List<Review>> reviewsMono = reviewService.retrieveReviewsUsingWebClient(movieInfo.getMovieInfoId())
                            .collectList();
                    return reviewsMono.map(list -> new Movie(movieInfo, list));
                })
                .onErrorMap(ex -> {
                    log.error(ex.getMessage());
                    throw new MovieException(ex.getMessage());
                })
                .log();
    }

    public Mono<Movie> getMovieById(Long movieId) {
        Mono<MovieInfo> movieInfoMono = movieInfoService.retrieveMovieInfoMonoUsingId(movieId);

        // Mono<List<Review>> reviewList = reviewService.retrieveReviewsFlux(movieId)
        //        .collectList();
        // return movieInfoMono.zipWith(reviewList, (movieInfo, reviews) -> new Movie(movieInfo, reviews));

        return movieInfoMono.flatMap(movieInfo -> {
            Mono<List<Review>> reviewsMono = reviewService.retrieveReviewsFlux(movieId).collectList();
            return reviewsMono.map(list -> new Movie(movieInfo, list));
        });
    }

    public Mono<Movie> getMovieByIdWithRevenue(Long movieId) {
        Mono<MovieInfo> movieInfoMono = movieInfoService.retrieveMovieInfoMonoUsingId(movieId);
        Mono<List<Review>> reviewList = reviewService.retrieveReviewsFlux(movieId)
                .collectList();

        /* integrate a blocking call into reactive */
        Mono<Revenue> revenueMono = Mono.fromCallable(() -> revenueService.getRevenue(movieId))
                .subscribeOn(Schedulers.boundedElastic());

        return movieInfoMono.zipWith(reviewList, (movieInfo, reviews) -> new Movie(movieInfo, reviews))
                .zipWith(revenueMono, (movie, revenue) -> {
                    movie.setRevenue(revenue);
                    return movie;
                });
    }


    public Flux<Movie> getAllMoviesRetry() {
        Flux<MovieInfo> moviesInfoFlux = movieInfoService.retrieveMoviesFlux();
        return moviesInfoFlux
                .flatMap(movieInfo -> {
                    Mono<List<Review>> reviewsMono = reviewService.retrieveReviewsFlux(movieInfo.getMovieInfoId())
                            .collectList();
                    return reviewsMono.map(list -> new Movie(movieInfo, list));
                })
                .onErrorMap(ex -> {
                    log.error(ex.getMessage());
                    throw new MovieException(ex.getMessage());
                })
                .retry(3)
                .log();
    }

    public Flux<Movie> getAllMoviesRetryWhen() {
        var retrySpec = Retry.backoff(3, Duration.ofMillis(100))
                .filter(ex -> ex instanceof MovieException)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> Exceptions.propagate(retrySignal.failure()));

        Flux<MovieInfo> moviesInfoFlux = movieInfoService.retrieveMoviesFlux();
        return moviesInfoFlux
                .flatMap(movieInfo -> {
                    Mono<List<Review>> reviewsMono = reviewService.retrieveReviewsFlux(movieInfo.getMovieInfoId())
                            .collectList();
                    return reviewsMono.map(list -> new Movie(movieInfo, list));
                })
                .onErrorMap(ex -> {
                    log.error(ex.getMessage());
                    if (ex instanceof NetworkException) {
                        throw new MovieException(ex.getMessage());
                    } else {
                        throw new ServiceException(ex.getMessage());
                    }
                })
                .retryWhen(retrySpec)
                .log();
    }

    public Flux<Movie> getAllMoviesRepeat() {
        Flux<MovieInfo> moviesInfoFlux = movieInfoService.retrieveMoviesFlux();
        return moviesInfoFlux
                .flatMap(movieInfo -> {
                    Mono<List<Review>> reviewsMono = reviewService.retrieveReviewsFlux(movieInfo.getMovieInfoId())
                            .collectList();
                    return reviewsMono.map(list -> new Movie(movieInfo, list));
                })
                .repeat(1)
                .log();
    }
}